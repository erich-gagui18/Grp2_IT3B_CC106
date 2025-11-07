package com.example.beteranos.ui_admin.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.ConnectionClass;
// --- REMOVED Appointment, List, ArrayList imports ---

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminProfileViewModel extends ViewModel {

    private static final String TAG = "AdminProfileViewModel";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // LiveData for admin details
    private final MutableLiveData<String> _name = new MutableLiveData<>();
    public LiveData<String> name = _name;

    // LiveData for UI state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // LiveData to signal navigation event
    private final MutableLiveData<Boolean> _navigateToLogin = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToLogin = _navigateToLogin;

    // --- REMOVED appointmentHistory LiveData ---

    public void fetchAdminDetails(int adminId) {
        if (adminId == -1) {
            _name.postValue("Admin User");
            return;
        }

        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                String query = "SELECT username FROM admins WHERE admin_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, adminId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            _name.postValue(rs.getString("username"));
                        } else {
                            _name.postValue("Admin Not Found");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching admin details: " + e.getMessage(), e);
                _name.postValue("Error");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    // --- REMOVED fetchAppointmentHistory() method ---

    public void logout(Context context) {
        executor.execute(() -> {
            // Clear admin SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Signal the fragment to navigate
            _navigateToLogin.postValue(true);
        });
    }

    public void onLoginNavigationComplete() {
        _navigateToLogin.setValue(false);
    }
}