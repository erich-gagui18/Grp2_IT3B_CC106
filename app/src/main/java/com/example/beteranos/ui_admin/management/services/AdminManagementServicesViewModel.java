package com.example.beteranos.ui_admin.management.services;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminManagementServicesViewModel extends ViewModel {

    private static final String TAG = "AdminServicesViewModel";

    private final MutableLiveData<List<Service>> _allServices = new MutableLiveData<>();
    public LiveData<List<Service>> allServices = _allServices;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AdminManagementServicesViewModel() {
        fetchServices();
    }

    public void fetchServices() {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Service> serviceList = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "SELECT service_id, service_name, price FROM services ORDER BY service_name ASC";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        serviceList.add(new Service(
                                rs.getInt("service_id"),
                                rs.getString("service_name"),
                                rs.getDouble("price")
                        ));
                    }
                    _allServices.postValue(serviceList);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching services: " + e.getMessage(), e);
                _toastMessage.postValue("Error fetching services list");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void addService(String name, double price) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "INSERT INTO services (service_name, price) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setDouble(2, price);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Service added successfully");
                        fetchServices(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding service: " + e.getMessage(), e);
                _toastMessage.postValue("Error adding service");
                _isLoading.postValue(false);
            }
        });
    }

    public void updateService(int serviceId, String name, double price) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "UPDATE services SET service_name = ?, price = ? WHERE service_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setDouble(2, price);
                    stmt.setInt(3, serviceId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Service updated successfully");
                        fetchServices(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating service: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating service");
                _isLoading.postValue(false);
            }
        });
    }

    public void deleteService(Service service) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "DELETE FROM services WHERE service_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, service.getServiceId());
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Service deleted successfully");
                        fetchServices(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting service: " + e.getMessage(), e);
                _toastMessage.postValue("Error deleting service. It may be in use.");
                _isLoading.postValue(false);
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}