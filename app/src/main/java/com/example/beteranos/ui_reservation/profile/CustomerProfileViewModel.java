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
                String query = "SELECT r.reservation_time, r.status, b.name AS barber_name, s.service_name " +
                        "FROM reservations r " +
                        "JOIN barbers b ON r.barber_id = b.barber_id " +
                        "JOIN services s ON r.service_id = s.service_id " +
                        "WHERE r.customer_id = ? " +
                        "ORDER BY r.reservation_time DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            history.add(new Appointment(
                                    0, // No ID needed for display
                                    null, // No customer name needed
                                    rs.getString("service_name"),
                                    rs.getString("barber_name"),
                                    rs.getTimestamp("reservation_time"),
                                    rs.getString("status")
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