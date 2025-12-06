package com.example.beteranos.ui_admin.management.barbers;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Barber;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminManagementBarbersViewModel extends ViewModel {

    private static final String TAG = "AdminBarbersViewModel";

    private final MutableLiveData<List<Barber>> _allBarbers = new MutableLiveData<>();
    public LiveData<List<Barber>> allBarbers = _allBarbers;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AdminManagementBarbersViewModel() {
        fetchBarbers();
    }

    public void fetchBarbers() {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Barber> barberList = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) {
                    throw new Exception("Database connection failed");
                }
                // ⭐️ UPDATED: Fetch 'is_active' column
                String query = "SELECT barber_id, name, specialization, day_off, image_url, is_active FROM barbers ORDER BY name ASC";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        // ⭐️ UPDATED: Use 8-parameter constructor to include visibility status
                        barberList.add(new Barber(
                                rs.getInt("barber_id"),
                                rs.getString("name"),
                                rs.getString("specialization"),
                                0, // Dummy experience_years
                                "N/A", // Dummy contact_number
                                rs.getString("image_url"),
                                rs.getString("day_off"),
                                rs.getBoolean("is_active") // ⭐️ Pass the status
                        ));
                    }
                    _allBarbers.postValue(barberList);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching barbers: " + e.getMessage(), e);
                _toastMessage.postValue("Error fetching barbers list");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void addBarber(String name, String specialization, String imageUrl, String dayOff) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                // ⭐️ UPDATED: Insert 'is_active' with default value 1 (Visible)
                String query = "INSERT INTO barbers (name, specialization, image_url, day_off, is_active) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    stmt.setString(3, imageUrl);
                    stmt.setString(4, dayOff);
                    stmt.setInt(5, 1); // ⭐️ Default to Active

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Barber added successfully");
                        fetchBarbers(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding barber: " + e.getMessage(), e);
                _toastMessage.postValue("Error adding barber: " + e.getMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void updateBarber(int barberId, String name, String specialization, String imageUrl, String dayOff) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                String query = "UPDATE barbers SET name = ?, specialization = ?, image_url = ?, day_off = ? WHERE barber_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    stmt.setString(3, imageUrl);
                    stmt.setString(4, dayOff);
                    stmt.setInt(5, barberId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Barber updated successfully");
                        fetchBarbers(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating barber: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating barber: " + e.getMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    // ⭐️ NEW METHOD: Toggle Visibility (Hide/Show) ⭐️
    public void toggleBarberVisibility(Barber barber) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            boolean newStatus = !barber.isActive(); // Flip status
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                String query = "UPDATE barbers SET is_active = ? WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setBoolean(1, newStatus);
                    stmt.setInt(2, barber.getBarberId());

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Barber is now " + (newStatus ? "Visible" : "Hidden"));
                        fetchBarbers(); // Refresh list to update UI icon
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

    public void deleteBarber(Barber barber) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "DELETE FROM barbers WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, barber.getBarberId());
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Barber deleted successfully");
                        fetchBarbers(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting barber: " + e.getMessage(), e);
                _toastMessage.postValue("Error deleting barber. They may have appointments.");
                _isLoading.postValue(false);
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}