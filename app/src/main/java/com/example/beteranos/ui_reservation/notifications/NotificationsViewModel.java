package com.example.beteranos.ui_reservation.notifications;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<List<Appointment>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<List<Appointment>> getNotifications() {
        return notifications;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void fetchNotifications(int customerId) {
        isLoading.setValue(true);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Appointment> fetchedNotifications = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {

                // ✅ Updated query for new schema (uses reservation_services)
                String query =
                        "SELECT r.reservation_id, " +
                                "       r.status, " +
                                "       r.reservation_time, " +
                                "       GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_names, " +
                                "       b.name AS barber_name " +
                                "FROM reservations r " +
                                "JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                                "JOIN services s ON rs.service_id = s.service_id " +
                                "JOIN barbers b ON r.barber_id = b.barber_id " +
                                "WHERE r.customer_id = ? " +
                                "GROUP BY r.reservation_id, r.status, r.reservation_time, b.name " +
                                "ORDER BY r.reservation_time DESC";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            fetchedNotifications.add(new Appointment(
                                    rs.getInt("reservation_id"),
                                    null, // No customer name needed for their own notifications
                                    rs.getString("service_names"), // ✅ Multiple services concatenated
                                    rs.getString("barber_name"),
                                    rs.getTimestamp("reservation_time"),
                                    rs.getString("status")
                            ));
                        }
                        notifications.postValue(fetchedNotifications);
                    }
                }
            } catch (Exception e) {
                Log.e("NotificationsViewModel", "DB Error: " + e.getMessage(), e);
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}
