package com.example.beteranos.ui_admin;

// ⭐️ 1. ADDED IMPORTS
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel; // ⭐️ CHANGED from ViewModel

import android.util.Log;
// import androidx.lifecycle.ViewModel; // ⭐️ REMOVED
import androidx.lifecycle.MutableLiveData;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.MainActivity; // Or your main app entry point
import com.example.beteranos.models.Appointment;

// ⭐️ THE FIX: Changed this import path to match your existing file ⭐️
import com.example.beteranos.ui_reservation.home.notifications.NotificationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp; // ⭐️ ADDED Import
import java.text.SimpleDateFormat; // ⭐️ ADDED Import
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // ⭐️ ADDED Import
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ⭐️ 2. CHANGED to AndroidViewModel
public class SharedAdminAppointmentViewModel extends AndroidViewModel {

    public final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private long lastFetchedDateInMillis;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ⭐️ 3. ADDED NotificationHelper
    private final NotificationHelper notificationHelper;

    // ⭐️ 4. CHANGED Constructor for AndroidViewModel
    public SharedAdminAppointmentViewModel(@NonNull Application application) {
        super(application);
        // Initialize the helper using the application context
        this.notificationHelper = new NotificationHelper(application.getApplicationContext());
    }

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

                        // ⭐️ 5. TRIGGER "Confirmed" NOTIFICATION ⭐️
                        sendNotificationForStatus(reservationId, "Confirmed");
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

                    // ⭐️ 6. TRIGGER "Completed" or "Cancelled" NOTIFICATION ⭐️
                    if (newStatus.equalsIgnoreCase("Completed") || newStatus.equalsIgnoreCase("Cancelled")) {
                        sendNotificationForStatus(reservationId, newStatus);
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

    // ⭐️ 7. ADDED Helper method to build and send the notification ⭐️
    /**
     * Helper method to fetch reservation details AND send the notification.
     */
    private void sendNotificationForStatus(int reservationId, String status) {
        // We must fetch the reservation details to know what to put in the notification
        // This query gets the time and services for the notification body
        String query = "SELECT r.reservation_time, GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_names " +
                "FROM reservations r " +
                "JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                "JOIN services s ON rs.service_id = s.service_id " +
                "WHERE r.reservation_id = ? " +
                "GROUP BY r.reservation_time";

        try (Connection conn = new ConnectionClass().CONN();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp time = rs.getTimestamp("reservation_time");
                String services = rs.getString("service_names");

                String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(time);
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(time);

                // --- Build and Send Notification ---
                Context context = getApplication().getApplicationContext();
                Intent intent = new Intent(context, MainActivity.class); // Opens the app
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, reservationId, intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                String title = "Appointment " + status;
                String body = "Your appointment for " + services + " on " + dateStr + " at " + timeStr + " is now " + status + "!";

                // Use the helper to show the pop-up notification
                notificationHelper.showNotification(title, body, pendingIntent);
            }

        } catch (Exception e) {
            Log.e("SharedAdminApptVM", "Error sending " + status + " notification: " + e.getMessage());
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}