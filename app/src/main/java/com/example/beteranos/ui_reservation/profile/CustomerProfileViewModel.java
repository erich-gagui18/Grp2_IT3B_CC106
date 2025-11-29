package com.example.beteranos.ui_reservation.profile;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Appointment;
import com.example.beteranos.models.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerProfileViewModel extends ViewModel {

    private final MutableLiveData<Customer> customerData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> appointmentHistory = new MutableLiveData<>();

    public LiveData<Customer> getCustomerData() {
        return customerData;
    }

    public LiveData<List<Appointment>> getAppointmentHistory() {
        return appointmentHistory;
    }

    public void loadCustomerData(int customerId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                String query = "SELECT * FROM customers WHERE customer_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Customer customer = new Customer(
                                    rs.getInt("customer_id"),
                                    rs.getString("first_name"),
                                    rs.getString("middle_name"),
                                    rs.getString("last_name"),
                                    rs.getString("phone_number"),
                                    rs.getString("email")
                            );
                            customerData.postValue(customer);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ProfileViewModel", "DB Error fetching customer: " + e.getMessage());
            }
        });
    }

    public void loadAppointmentHistory(int customerId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Appointment> history = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {

                // --- ⭐️ FIX: Use MAX() on the BLOB column and REMOVE it from GROUP BY ⭐️ ---
                String query =
                        "SELECT r.reservation_id, r.reservation_time, r.status, " +
                                "       MAX(r.payment_receipt) as payment_receipt, " + // <-- Aggregate the BLOB
                                "       r.service_location, r.home_address, " +
                                "       b.name AS barber_name, " +
                                "       GROUP_CONCAT(s.service_name SEPARATOR ', ') AS service_names " +
                                "FROM reservations r " +
                                "JOIN barbers b ON r.barber_id = b.barber_id " +
                                "JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                                "JOIN services s ON rs.service_id = s.service_id " +
                                "WHERE r.customer_id = ? " +
                                // ⭐️ CRITICAL: Do NOT include r.payment_receipt here
                                "GROUP BY r.reservation_id, r.reservation_time, r.status, " +
                                "         r.service_location, r.home_address, b.name " +
                                "ORDER BY r.reservation_time DESC";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            history.add(new Appointment(
                                    rs.getInt("reservation_id"),
                                    null,
                                    rs.getString("service_names"),
                                    rs.getString("barber_name"),
                                    rs.getTimestamp("reservation_time"),
                                    rs.getString("status"),
                                    rs.getBytes("payment_receipt"), // This gets the aggregated BLOB
                                    rs.getString("service_location"),
                                    rs.getString("home_address")
                            ));
                        }
                        appointmentHistory.postValue(history);
                    }
                }
            } catch (Exception e) {
                Log.e("ProfileViewModel", "DB Error fetching history: " + e.getMessage());
            }
        });
    }
}