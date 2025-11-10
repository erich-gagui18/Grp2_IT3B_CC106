package com.example.beteranos.ui_admin.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminHomeViewModel extends ViewModel {

    private static final String TAG = "AdminHomeViewModel";

    // LiveData for the "At a Glance" Stats
    private final MutableLiveData<AdminHomeStats> _stats = new MutableLiveData<>();
    public LiveData<AdminHomeStats> stats = _stats;

    // LiveData for the "Pending Appointments" list
    private final MutableLiveData<List<Appointment>> _pendingAppointments = new MutableLiveData<>();
    public LiveData<List<Appointment>> pendingAppointments = _pendingAppointments;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public void fetchHomeDashboardDataIfNeeded() {
        if (stats.getValue() == null) {
            fetchHomeDashboardData();
        }
    }

    public void fetchHomeDashboardData() {
        _isLoading.postValue(true);
        fetchStats();
        fetchPendingAppointments();
    }

    public void refreshData() {
        fetchHomeDashboardData();
    }

    private void fetchStats() {
        executor.execute(() -> {

            // ⭐️ UPDATED SQL QUERY FOR ACCURATE REVENUE ⭐️
            String query = "SELECT " +
                    "    (SELECT COUNT(*) FROM reservations WHERE DATE(reservation_time) = CURDATE()) AS totalBookings, " +
                    "    (SELECT COUNT(*) FROM reservations WHERE status = 'Pending') AS pendingCount, " +
                    " " +
                    "    (SELECT IFNULL( " +
                    "        SUM(CASE WHEN r.status = 'Completed' THEN r.final_price ELSE 0 END) + " +
                    "        SUM(CASE WHEN r.status IN ('Confirmed', 'Scheduled') THEN r.down_payment_amount ELSE 0 END) " +
                    "     , 0) " +
                    "     FROM reservations r " +
                    "     WHERE DATE(r.reservation_time) = CURDATE() " +
                    "     AND r.status IN ('Completed', 'Confirmed', 'Scheduled')) AS estimatedRevenue";
            // ⭐️ END OF SQL UPDATE ⭐️

            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    int totalBookings = rs.getInt("totalBookings");
                    int pendingCount = rs.getInt("pendingCount");
                    double estimatedRevenue = rs.getDouble("estimatedRevenue");

                    _stats.postValue(new AdminHomeStats(totalBookings, pendingCount, estimatedRevenue));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching stats: " + e.getMessage(), e);
                _stats.postValue(new AdminHomeStats(0, 0, 0));
            }
        });
    }

    /**
     * Fetches appointments with 'Pending' status, now including the payment receipt BLOB data.
     */
    private void fetchPendingAppointments() {
        executor.execute(() -> {
            List<Appointment> pending = new ArrayList<>();

            String query = "SELECT r.reservation_id, " +
                    "CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                    "GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_names, " +
                    "b.name AS barber_name, " +
                    "r.reservation_time, r.status, " +
                    "r.payment_receipt " + // Select the BLOB column
                    "FROM reservations r " +
                    "JOIN customers c ON r.customer_id = c.customer_id " +
                    "JOIN barbers b ON r.barber_id = b.barber_id " +
                    "JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                    "JOIN services s ON rs.service_id = s.service_id " +
                    "WHERE r.status = 'Pending' " +
                    "GROUP BY r.reservation_id, customer_name, barber_name, r.reservation_time, r.status " +
                    "ORDER BY r.reservation_time ASC";

            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    pending.add(new Appointment(
                            rs.getInt("reservation_id"),
                            rs.getString("customer_name"),
                            rs.getString("service_names"),
                            rs.getString("barber_name"),
                            rs.getTimestamp("reservation_time"),
                            rs.getString("status"),
                            rs.getBytes("payment_receipt")
                    ));
                }
                _pendingAppointments.postValue(pending);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching pending appointments: " + e.getMessage(), e);
            } finally {
                // Ensure loading state is turned off after all data is fetched or attempted.
                _isLoading.postValue(false);
            }
        });
    }

    public void updateAppointmentStatus(int reservationId, String newStatus) {
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                String query = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, reservationId);
                    int updated = stmt.executeUpdate();
                    if (updated > 0) {
                        // After an update, re-fetch the data to refresh the UI list and stats
                        fetchHomeDashboardData();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "DB Error updating status: " + e.getMessage());
            }
        });
    }

    // Helper model class for the stats (Unchanged)
    public static class AdminHomeStats {
        public final int totalBookings;
        public final int pendingCount;
        public final double estimatedRevenue;

        AdminHomeStats(int totalBookings, int pendingCount, double estimatedRevenue) {
            this.totalBookings = totalBookings;
            this.pendingCount = pendingCount;
            this.estimatedRevenue = estimatedRevenue;
        }

        public String getFormattedRevenue() {
            if (estimatedRevenue >= 1000) {
                return String.format(Locale.US, "₱%.1fk", estimatedRevenue / 1000.0);
            } else {
                return String.format(Locale.US, "₱%.0f", estimatedRevenue);
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}