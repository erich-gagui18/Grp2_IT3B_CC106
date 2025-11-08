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
                String query = "SELECT barber_id, name, specialization, day_off FROM barbers ORDER BY name ASC";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        barberList.add(new Barber(
                                rs.getInt("barber_id"),
                                rs.getString("name"),
                                rs.getString("specialization"),
                                rs.getString("day_off")
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

    public void addBarber(String name, String specialization) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "INSERT INTO barbers (name, specialization) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Barber added successfully");
                        fetchBarbers(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding barber: " + e.getMessage(), e);
                _toastMessage.postValue("Error adding barber");
                _isLoading.postValue(false); // Only stop loading on error (success stops in fetch)
            }
        });
    }

    public void updateBarber(int barberId, String name, String specialization) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "UPDATE barbers SET name = ?, specialization = ? WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    stmt.setInt(3, barberId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Barber updated successfully");
                        fetchBarbers(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating barber: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating barber");
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

    // Call this to clear the toast message after it's shown
    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}