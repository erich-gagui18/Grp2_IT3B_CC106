package com.example.beteranos.ui_admin;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; // Added
import java.text.SimpleDateFormat; // Added
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Added
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

                // ⭐️ FIXED QUERY:
                // 1. Used MAX(r.payment_receipt) to prevent "Out of sort memory" error.
                // 2. Added missing comma before r.service_location.
                String query = "SELECT r.reservation_id, " +
                        "CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                        "GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_names, " +
                        "b.name AS barber_name, " +
                        "r.reservation_time, r.status, " +
                        "MAX(r.payment_receipt) AS payment_receipt, " + // <-- Aggregate BLOB
                        "r.service_location, r.home_address " +         // <-- Added columns
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN barbers b ON r.barber_id = b.barber_id " +
                        "JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                        "JOIN services s ON rs.service_id = s.service_id " +
                        "WHERE DATE(r.reservation_time) = ? " +
                        // ⭐️ Group by all columns EXCEPT the BLOB
                        "GROUP BY r.reservation_id, customer_name, barber_name, r.reservation_time, r.status, r.service_location, r.home_address " +
                        "ORDER BY r.reservation_time ASC";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, new java.sql.Date(dateInMillis));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // ⭐️ Updated Constructor with 9 arguments
                    fetchedAppointments.add(new Appointment(
                            rs.getInt("reservation_id"),
                            rs.getString("customer_name"),
                            rs.getString("service_names"),
                            rs.getString("barber_name"),
                            rs.getTimestamp("reservation_time"),
                            rs.getString("status"),
                            rs.getBytes("payment_receipt"), // Gets aggregated BLOB
                            rs.getString("service_location"),
                            rs.getString("home_address")
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

    // --- ⭐️ CONFIRMATION LOGIC WITH NOTIFICATION ⭐️ ---
    public void confirmAppointmentWithDownPayment(int reservationId, double downPaymentAmount) {
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("DB Connection Failed");

                // Start Transaction
                conn.setAutoCommit(false);

                // 1. Update Status
                String query = "UPDATE reservations SET status = 'Confirmed', down_payment_amount = ? WHERE reservation_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setDouble(1, downPaymentAmount);
                    stmt.setInt(2, reservationId);
                    stmt.executeUpdate();
                }

                // 2. Create Notification
                createNotificationForUser(conn, reservationId, "Confirmed");

                // Commit Transaction
                conn.commit();

                fetchAppointmentsForDate(lastFetchedDateInMillis);

            } catch (Exception e) {
                Log.e("SharedAdminApptVM", "DB Error confirming payment: " + e.getMessage());
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                }
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
                }
            }
        });
    }

    // --- ⭐️ STATUS UPDATE LOGIC WITH NOTIFICATION ⭐️ ---
    public void updateAppointmentStatus(int reservationId, String newStatus) {
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("DB Connection Failed");

                // Start Transaction
                conn.setAutoCommit(false);

                String query = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, reservationId);
                    stmt.executeUpdate();
                }

                // Create notification if status is Cancelled or Completed
                if (newStatus.equals("Cancelled") || newStatus.equals("Completed")) {
                    createNotificationForUser(conn, reservationId, newStatus);
                }

                // Commit Transaction
                conn.commit();

                fetchAppointmentsForDate(lastFetchedDateInMillis);

            } catch (Exception e) {
                Log.e("SharedAdminApptVM", "DB Error updating status: " + e.getMessage());
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                }
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
                }
            }
        });
    }

    // --- ⭐️ NOTIFICATION HELPER ⭐️ ---
    private void createNotificationForUser(Connection conn, int reservationId, String newStatus) throws SQLException {
        String title = "";
        String body = "";
        int customerId = -1;

        String query = "SELECT r.customer_id, r.reservation_time, b.name AS barber_name, " +
                "GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_names " +
                "FROM reservations r " +
                "JOIN barbers b ON r.barber_id = b.barber_id " +
                "JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                "JOIN services s ON rs.service_id = s.service_id " +
                "WHERE r.reservation_id = ? " +
                "GROUP BY r.customer_id, r.reservation_time, b.name";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                    String services = rs.getString("service_names");
                    String barber = rs.getString("barber_name");
                    String time = new SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.US)
                            .format(rs.getTimestamp("reservation_time"));

                    switch (newStatus) {
                        case "Confirmed":
                            title = "Appointment Confirmed!";
                            body = "Your booking for " + services + " with " + barber + " on " + time + " is confirmed.";
                            break;
                        case "Completed":
                            title = "Appointment Completed";
                            body = "Your appointment with " + barber + " on " + time + " is complete. We hope to see you again!";
                            break;
                        case "Cancelled":
                            title = "Appointment Cancelled";
                            body = "Your booking for " + services + " with " + barber + " on " + time + " has been cancelled.";
                            break;
                    }
                }
            }
        }

        if (customerId != -1 && !title.isEmpty()) {
            String insertQuery = "INSERT INTO notifications (customer_id, title, body) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, customerId);
                insertStmt.setString(2, title);
                insertStmt.setString(3, body);
                insertStmt.executeUpdate();
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}