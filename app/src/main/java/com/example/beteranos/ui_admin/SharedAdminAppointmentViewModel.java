package com.example.beteranos.ui_admin;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedAdminAppointmentViewModel extends ViewModel {

    public final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private long lastFetchedDateInMillis;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // This method is correct and fetches appointments for the calendar
    public void fetchAppointmentsForDate(long dateInMillis) {

        this.lastFetchedDateInMillis = dateInMillis;
        isLoading.postValue(true);

        executor.execute(() -> {
            List<Appointment> fetchedAppointments = new ArrayList<>();
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("DB Connection Failed");

                // This query is correct (fixes the "Out of sort memory" error)
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
                        "WHERE DATE(r.reservation_time) = ? " +
                        // Group by all non-aggregate, non-BLOB columns
                        "GROUP BY r.reservation_id, customer_name, barber_name, r.reservation_time, r.status " +
                        "ORDER BY r.reservation_time ASC";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, new java.sql.Date(dateInMillis));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    fetchedAppointments.add(new Appointment(
                            rs.getInt("reservation_id"),
                            rs.getString("customer_name"),
                            rs.getString("service_names"),
                            rs.getString("barber_name"),
                            rs.getTimestamp("reservation_time"),
                            rs.getString("status"),
                            rs.getBytes("payment_receipt")
                    ));
                }
                appointments.postValue(fetchedAppointments);

            } catch (Exception e) {
                Log.e("SharedAdminApptVM", "DB Error: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (Exception ignored) {}
                }
                isLoading.postValue(false);
            }
        });
    }

    // --- ⭐️ THIS IS THE NEW METHOD (STEP 2 of our plan) ⭐️ ---
    // This method is called when the admin confirms a "Pending" booking
    // and enters the down payment amount.
    public void confirmAppointmentWithDownPayment(int reservationId, double downPaymentAmount) {
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                // This is the SQL command from your plan
                String query = "UPDATE reservations SET status = 'Confirmed', down_payment_amount = ? WHERE reservation_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setDouble(1, downPaymentAmount);
                    stmt.setInt(2, reservationId);
                    int updated = stmt.executeUpdate();

                    if (updated > 0) {
                        // Refresh the list to show the status change
                        fetchAppointmentsForDate(lastFetchedDateInMillis);
                    }
                }
            } catch (Exception e) {
                Log.e("SharedAdminApptVM", "DB Error confirming payment: " + e.getMessage());
            }
        });
    }


    // This method is still used for "Cancel" and "Mark as Completed"
    public void updateAppointmentStatus(int reservationId, String newStatus) {
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("DB Connection Failed");

                String query = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newStatus);
                stmt.setInt(2, reservationId);
                int updated = stmt.executeUpdate();

                if (updated > 0) {
                    // --- ⭐️ UPDATED LOGIC ⭐️ ---
                    // Instead of manually updating the list, just re-fetch.
                    // This is more reliable and ensures all data is fresh.
                    fetchAppointmentsForDate(lastFetchedDateInMillis);
                }

            } catch (Exception e) {
                Log.e("SharedAdminApptVM", "DB Error updating status: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (Exception ignored) {}
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}