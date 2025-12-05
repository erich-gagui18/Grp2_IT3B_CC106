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

                // ⭐️ UPDATED: Fetch 'is_active' column
                String query = "SELECT service_id, service_name, price, is_active FROM services ORDER BY service_name ASC";

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        // ⭐️ UPDATED: Use 4-parameter constructor (ID, Name, Price, IsActive)
                        serviceList.add(new Service(
                                rs.getInt("service_id"),
                                rs.getString("service_name"),
                                rs.getDouble("price"),
                                rs.getBoolean("is_active")
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

                // ⭐️ UPDATED: Insert 'is_active' with default value 1 (Visible)
                String query = "INSERT INTO services (service_name, price, is_active) VALUES (?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setDouble(2, price);
                    stmt.setInt(3, 1); // Default to Active

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Service added successfully");
                        fetchServices(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding service: " + e.getMessage(), e);
                _toastMessage.postValue("Error adding service");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    // ⭐️ UPDATED: Accepts isActive status to preserve it during updates
    public void updateService(int serviceId, String name, double price, boolean isActive) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                // ⭐️ UPDATED: Update 'is_active' as well
                String query = "UPDATE services SET service_name = ?, price = ?, is_active = ? WHERE service_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setDouble(2, price);
                    stmt.setBoolean(3, isActive);
                    stmt.setInt(4, serviceId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Service updated successfully");
                        fetchServices(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating service: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating service");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    // Overload for backward compatibility (defaults to true if needed, or preserves old logic)
    public void updateService(int serviceId, String name, double price) {
        updateService(serviceId, name, price, true);
    }

    // ⭐️ NEW METHOD: Toggle Visibility (Hide/Show) ⭐️
    public void toggleServiceVisibility(Service service) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            boolean newStatus = !service.isActive(); // Flip status
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                String query = "UPDATE services SET is_active = ? WHERE service_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setBoolean(1, newStatus);
                    stmt.setInt(2, service.getServiceId());

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Service is now " + (newStatus ? "Visible" : "Hidden"));
                        fetchServices(); // Refresh list to update UI icon
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error toggling visibility: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating visibility");
            } finally {
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
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}