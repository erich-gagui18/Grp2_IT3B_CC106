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
import java.sql.Types;
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
        fetchPromos(); // ⭐️ RENAMED to match your Fragment usage
    }

    // ⭐️ UPDATED: Fetch 'is_active' status ⭐️
    public void fetchPromos() {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Promo> promoList = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                // ⭐️ Added is_active to SELECT
                String query = "SELECT promo_id, promo_name, description, discount_percentage, image_name, is_active FROM promos ORDER BY promo_name ASC";

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // ⭐️ Updated Constructor (6 params)
                        promoList.add(new Promo(
                                rs.getInt("promo_id"),
                                rs.getString("promo_name"),
                                rs.getString("description"),
                                rs.getInt("discount_percentage"),
                                rs.getBytes("image_name"),
                                rs.getBoolean("is_active") // ⭐️ Read visibility status
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

    public void fetchPromosFromDB() {
        fetchPromos(); // Alias for compatibility if needed
    }

    // ⭐️ UPDATED: Set default visibility to TRUE (1) on Add ⭐️
    public void addPromo(String name, String description, int discount, byte[] imageBytes) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                // ⭐️ Added is_active column
                String query = "INSERT INTO promos (promo_name, description, discount_percentage, image_name, is_active) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setInt(3, discount);
                    if (imageBytes != null) {
                        stmt.setBytes(4, imageBytes);
                    } else {
                        stmt.setNull(4, Types.BLOB);
                    }
                    stmt.setInt(5, 1); // ⭐️ Default to Active (Visible)

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Promo added successfully");
                        fetchPromos();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding promo: " + e.getMessage(), e);
                _toastMessage.postValue("Error adding promo");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    // ⭐️ UPDATED: Accept 'isActive' parameter for Updates ⭐️
    public void updatePromo(int promoId, String name, String description, int discount, byte[] imageBytes, boolean isActive) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                // ⭐️ Update is_active
                String query = "UPDATE promos SET promo_name = ?, description = ?, discount_percentage = ?, image_name = ?, is_active = ? WHERE promo_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setInt(3, discount);
                    if (imageBytes != null) {
                        stmt.setBytes(4, imageBytes);
                    } else {
                        stmt.setNull(4, Types.BLOB);
                    }
                    stmt.setBoolean(5, isActive); // ⭐️ Update status
                    stmt.setInt(6, promoId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Promo updated successfully");
                        fetchPromos();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating promo: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating promo");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    // Compatibility overload for older calls (defaults to keeping existing status logic if query was simpler, but safe to force active here if needed)
    public void updatePromo(int promoId, String name, String description, int discount, byte[] imageBytes) {
        updatePromo(promoId, name, description, discount, imageBytes, true);
    }

    // ⭐️ NEW METHOD: Toggle Visibility ⭐️
    public void togglePromoVisibility(Promo promo) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            boolean newStatus = !promo.isActive(); // Flip status
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                String query = "UPDATE promos SET is_active = ? WHERE promo_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setBoolean(1, newStatus);
                    stmt.setInt(2, promo.getPromoId());

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Promo is now " + (newStatus ? "Visible" : "Hidden"));
                        fetchPromos(); // Refresh UI
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
                        fetchPromos();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting promo: " + e.getMessage(), e);
                _toastMessage.postValue("Error deleting promo. It may be in use.");
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
        executor.shutdownNow();
    }
}