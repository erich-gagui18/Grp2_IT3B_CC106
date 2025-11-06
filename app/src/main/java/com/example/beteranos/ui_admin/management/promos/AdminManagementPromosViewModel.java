package com.example.beteranos.ui_admin.management.promos;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Promo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminManagementPromosViewModel extends ViewModel {

    private static final String TAG = "AdminPromosViewModel";

    private final MutableLiveData<List<Promo>> _allPromos = new MutableLiveData<>();
    public LiveData<List<Promo>> allPromos = _allPromos;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AdminManagementPromosViewModel() {
        fetchPromos();
    }

    public void fetchPromos() {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Promo> promoList = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "SELECT promo_id, promo_name, description, discount_percentage FROM promos ORDER BY promo_name ASC";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        promoList.add(new Promo(
                                rs.getInt("promo_id"),
                                rs.getString("promo_name"),
                                rs.getString("description"),
                                rs.getInt("discount_percentage")
                        ));
                    }
                    _allPromos.postValue(promoList);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching promos: " + e.getMessage(), e);
                _toastMessage.postValue("Error fetching promos list");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void addPromo(String name, String description, int discount) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "INSERT INTO promos (promo_name, description, discount_percentage) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setInt(3, discount);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Promo added successfully");
                        fetchPromos(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding promo: " + e.getMessage(), e);
                _toastMessage.postValue("Error adding promo");
                _isLoading.postValue(false);
            }
        });
    }

    public void updatePromo(int promoId, String name, String description, int discount) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "UPDATE promos SET promo_name = ?, description = ?, discount_percentage = ? WHERE promo_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setInt(3, discount);
                    stmt.setInt(4, promoId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Promo updated successfully");
                        fetchPromos(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating promo: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating promo");
                _isLoading.postValue(false);
            }
        });
    }

    public void deletePromo(Promo promo) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");
                String query = "DELETE FROM promos WHERE promo_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, promo.getPromoId());
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Promo deleted successfully");
                        fetchPromos(); // Refresh the list
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting promo: " + e.getMessage(), e);
                _toastMessage.postValue("Error deleting promo. It may be in use.");
                _isLoading.postValue(false);
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}