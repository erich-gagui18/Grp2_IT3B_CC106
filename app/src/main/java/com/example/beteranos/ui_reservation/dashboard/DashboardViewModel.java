package com.example.beteranos.ui_reservation.dashboard; // Adjusted package name, ensure it's correct

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// Removed unused ArrayList and HashMap imports
import java.util.LinkedHashMap; // To maintain order
// Removed unused List import
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends ViewModel {

    // --- LiveData for Haircut Ranking ---
    private final MutableLiveData<Map<String, Integer>> haircutRanking = new MutableLiveData<>();

    // --- ADDED: LiveData for Barber Ranking ---
    private final MutableLiveData<Map<String, Integer>> barberRanking = new MutableLiveData<>();


    // --- Getters ---
    public LiveData<Map<String, Integer>> getHaircutRanking() {
        return haircutRanking;
    }

    // --- ADDED: Getter for Barber Ranking ---
    public LiveData<Map<String, Integer>> getBarberRanking() {
        return barberRanking;
    }


    // --- Method to fetch Haircut Ranking ---
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
                Log.e("DashboardViewModel", "DB Error fetching haircut ranking: " + e.getMessage());
                // Optionally post an empty map or error state: haircutRanking.postValue(new LinkedHashMap<>());
            }
        });
    }

    // --- ADDED: Method to fetch Barber Ranking ---
    public void fetchBarberRanking() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Map<String, Integer> ranking = new LinkedHashMap<>(); // Use LinkedHashMap for order
            try (Connection conn = new ConnectionClass().CONN()) {
                String query = "SELECT b.name AS barber_name, COUNT(r.reservation_id) AS appointment_count " +
                        "FROM reservations AS r " +
                        "JOIN barbers AS b ON r.barber_id = b.barber_id " +
                        "GROUP BY b.barber_id, b.name " + // Group by both ID and Name
                        "ORDER BY appointment_count DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ranking.put(rs.getString("barber_name"), rs.getInt("appointment_count"));
                    }
                    barberRanking.postValue(ranking); // Update LiveData
                }
            } catch (Exception e) {
                Log.e("DashboardViewModel", "DB Error fetching barber ranking: " + e.getMessage());
                // Optionally post an empty map or error state: barberRanking.postValue(new LinkedHashMap<>());
            }
        });
    }

    // --- Other Dashboard ViewModel logic can be added here ---

}