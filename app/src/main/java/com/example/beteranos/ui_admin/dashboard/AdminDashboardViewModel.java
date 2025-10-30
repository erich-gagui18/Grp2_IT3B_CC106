package com.example.beteranos.ui_admin.dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminDashboardViewModel extends ViewModel {

    // --- LiveData for text (existing management section) ---
    private final MutableLiveData<String> text = new MutableLiveData<>("Welcome to Admin Management");

    public LiveData<String> getText() {
        return text;
    }

    // --- Dashboard-related LiveData ---
    private final MutableLiveData<Map<String, Integer>> haircutRanking = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> barberRanking = new MutableLiveData<>();

    public LiveData<Map<String, Integer>> getHaircutRanking() {
        return haircutRanking;
    }

    public LiveData<Map<String, Integer>> getBarberRanking() {
        return barberRanking;
    }

    // --- Fetch Haircut Ranking ---
    public void fetchHaircutRanking() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Map<String, Integer> ranking = new LinkedHashMap<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                String query = "SELECT haircut_choice, COUNT(haircut_choice) AS choice_count " +
                        "FROM reservations " +
                        "WHERE haircut_choice IS NOT NULL AND haircut_choice != '' " +
                        "GROUP BY haircut_choice " +
                        "ORDER BY choice_count DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ranking.put(rs.getString("haircut_choice"), rs.getInt("choice_count"));
                    }
                    haircutRanking.postValue(ranking);
                }
            } catch (Exception e) {
                Log.e("AdminManagementVM", "DB Error fetching haircut ranking: " + e.getMessage());
            }
        });
    }

    // --- Fetch Barber Ranking ---
    public void fetchBarberRanking() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Map<String, Integer> ranking = new LinkedHashMap<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                String query = "SELECT b.name AS barber_name, COUNT(r.reservation_id) AS appointment_count " +
                        "FROM reservations AS r " +
                        "JOIN barbers AS b ON r.barber_id = b.barber_id " +
                        "GROUP BY b.barber_id, b.name " +
                        "ORDER BY appointment_count DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ranking.put(rs.getString("barber_name"), rs.getInt("appointment_count"));
                    }
                    barberRanking.postValue(ranking);
                }
            } catch (Exception e) {
                Log.e("AdminManagementVM", "DB Error fetching barber ranking: " + e.getMessage());
            }
        });
    }
}
