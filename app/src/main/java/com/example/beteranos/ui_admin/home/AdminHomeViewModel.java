package com.example.beteranos.ui_admin.home;

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

public class AdminHomeViewModel extends ViewModel {

    public final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public void fetchAppointmentsForDate(long dateInMillis) {
        // --- FIX: Use postValue() ---
        // This is safe to call from any thread
        isLoading.postValue(true);
        appointments.postValue(new ArrayList<>());
        // --- END FIX ---
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Appointment> fetchedAppointments = new ArrayList<>();
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                String query = "SELECT " +
                        "    r.reservation_id, " +
                        "    CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                        "    b.name AS barber_name, " +
                        "    r.reservation_time, " +
                        "    r.status, " +
                        "    GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_name " + // Combines names like "Haircut, Shaving"
                        "FROM " +
                        "    reservations r " +
                        "JOIN " +
                        "    customers c ON r.customer_id = c.customer_id " +
                        "JOIN " +
                        "    barbers b ON r.barber_id = b.barber_id " +
                        "LEFT JOIN " + // Use LEFT JOIN in case an appointment somehow has no services
                        "    reservation_services rs ON r.reservation_id = rs.reservation_id " +
                        "LEFT JOIN " +
                        "    services s ON rs.service_id = s.service_id " +
                        "WHERE " +
                        "    DATE(r.reservation_time) = ? " +
                        "GROUP BY " + // We must GROUP BY the main reservation details
                        "    r.reservation_id, " +
                        "    customer_name, " +
                        "    barber_name, " +
                        "    r.reservation_time, " +
                        "    r.status " +
                        "ORDER BY " +
                        "    r.reservation_time ASC";
                // --- END OF FIXED QUERY ---

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, new java.sql.Date(dateInMillis));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // The Appointment model constructor should be compatible with this:
                    // new Appointment(id, customer_name, combined_service_names, barber_name, time, status)
                    fetchedAppointments.add(new Appointment(
                            rs.getInt("reservation_id"),
                            rs.getString("customer_name"),
                            rs.getString("service_name"), // This now contains the combined list, e.g., "Haircut, Shaving"
                            rs.getString("barber_name"),
                            rs.getTimestamp("reservation_time"),
                            rs.getString("status")
                    ));
                }
                appointments.postValue(fetchedAppointments);
            } catch (Exception e) {
                Log.e("AdminHomeViewModel", "DB Error: " + e.getMessage(), e); // Log the full exception
            } finally {
                if (conn != null) try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
                isLoading.postValue(false);
            }
        });
    }
}