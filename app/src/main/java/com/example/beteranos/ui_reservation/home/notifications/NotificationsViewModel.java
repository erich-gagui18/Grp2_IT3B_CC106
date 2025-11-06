package com.example.beteranos.ui_reservation.home.notifications;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Notification; // We will create Notification objects

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp; // Import Timestamp
import java.text.SimpleDateFormat; // For formatting
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsViewModel extends ViewModel {

    private static final String TAG = "NotificationsViewModel";

    private final MutableLiveData<List<Notification>> _notifications = new MutableLiveData<>();
    public LiveData<List<Notification>> getNotifications() {
        return _notifications;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // --- THIS METHOD IS NOW UPDATED WITH YOUR LOGIC ---
    public void fetchNotifications(int customerId) {
        if (customerId == -1) {
            _notifications.postValue(new ArrayList<>()); // Post empty list for guest
            return;
        }

        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Notification> notificationList = new ArrayList<>();
            // Date formatter for the notification body
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);

            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                // ✅ This is the query logic you provided
                String query =
                        "SELECT r.reservation_id, r.reservation_time, r.status, b.name AS barber_name, " +
                                "       GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_names " +
                                "FROM reservations r " +
                                "JOIN barbers b ON r.barber_id = b.barber_id " +
                                "JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                                "JOIN services s ON rs.service_id = s.service_id " +
                                "WHERE r.customer_id = ? " +
                                "GROUP BY r.reservation_id, r.reservation_time, r.status, b.name " + // Added r.reservation_id
                                "ORDER BY r.reservation_time DESC";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {

                            // --- Generate Notification objects from the appointment data ---
                            int id = rs.getInt("reservation_id");
                            String status = rs.getString("status");
                            String barber = rs.getString("barber_name");
                            String services = rs.getString("service_names");
                            Timestamp time = rs.getTimestamp("reservation_time");

                            // Create a dynamic Title and Body
                            String title = "Appointment " + status;
                            String body = "Your appointment for " + services + " with " + barber +
                                    " on " + sdf.format(time) + " is " + status + ".";

                            // We assume 'is_read' is false, as the table doesn't exist
                            boolean isRead = false;

                            notificationList.add(new Notification(
                                    id,
                                    title,
                                    body,
                                    time,
                                    isRead
                            ));
                        }
                        _notifications.postValue(notificationList);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching notifications: " + e.getMessage(), e);
                _notifications.postValue(null); // Post null on error
            } finally {
                _isLoading.postValue(false);
            }
        });
    }
}