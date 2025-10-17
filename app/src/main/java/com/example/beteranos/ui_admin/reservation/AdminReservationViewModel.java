package com.example.beteranos.ui_admin.reservation;

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

public class AdminReservationViewModel extends ViewModel {

    public final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public void fetchAppointmentsForDate(long dateInMillis) {
        isLoading.setValue(true);
        appointments.setValue(new ArrayList<>());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Appointment> fetchedAppointments = new ArrayList<>();
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                String query = "SELECT r.reservation_id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                        "s.service_name, b.name AS barber_name, r.reservation_time, r.status " +
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN services s ON r.service_id = s.service_id " +
                        "JOIN barbers b ON r.barber_id = b.barber_id " +
                        "WHERE DATE(r.reservation_time) = ? " +
                        "ORDER BY r.reservation_time ASC";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, new java.sql.Date(dateInMillis));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    fetchedAppointments.add(new Appointment(
                            rs.getInt("reservation_id"),
                            rs.getString("customer_name"),
                            rs.getString("service_name"),
                            rs.getString("barber_name"),
                            rs.getTimestamp("reservation_time"),
                            rs.getString("status")
                    ));
                }
                appointments.postValue(fetchedAppointments);
            } catch (Exception e) {
                Log.e("AdminResViewModel", "DB Error: " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
                isLoading.postValue(false);
            }
        });
    }
}