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

    public void fetchAppointmentsForDate(long dateInMillis) {

        this.lastFetchedDateInMillis = dateInMillis;
        isLoading.postValue(true);

        executor.execute(() -> {
            List<Appointment> fetchedAppointments = new ArrayList<>();
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("DB Connection Failed");

                // --- START OF QUERY FIX ---
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

                        // --- 🔑 CRITICAL FIX: DO NOT group by the BLOB column. ---
                        // Group by all other non-aggregate columns.
                        "GROUP BY r.reservation_id, customer_name, barber_name, r.reservation_time, r.status " +

                        "ORDER BY r.reservation_time ASC";
                // --- END OF QUERY FIX ---

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, new java.sql.Date(dateInMillis));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // This constructor call is correct
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
                    List<Appointment> currentList = appointments.getValue();
                    if (currentList != null) {
                        List<Appointment> updatedList = new ArrayList<>(currentList.size());
                        for (Appointment appt : currentList) {
                            if (appt.getReservationId() == reservationId) {
                                // --- START OF COMPILE ERROR FIX ---
                                Appointment updatedAppt = new Appointment(
                                        appt.getReservationId(),
                                        appt.getCustomerName(),
                                        appt.getServiceName(),
                                        appt.getBarberName(),
                                        appt.getReservationTime(),
                                        newStatus,
                                        appt.getPaymentReceiptBytes() // <-- 🔑 Pass the existing byte[]
                                );
                                // --- END OF COMPILE ERROR FIX ---
                                updatedList.add(updatedAppt);
                            } else {
                                updatedList.add(appt);
                            }
                        }
                        appointments.postValue(updatedList);
                    }
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