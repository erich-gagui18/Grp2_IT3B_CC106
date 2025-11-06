package com.example.beteranos.ui_reservation.dashboard;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp; // Import Timestamp
import java.util.LinkedHashMap; // To maintain order
import java.util.Map;
import java.util.TreeMap; // For alphabetical sorting
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends ViewModel {

    private static final String TAG = "DashboardViewModel";

    // --- LiveData for Haircut Ranking ---
    private final MutableLiveData<Map<String, Integer>> haircutRanking = new MutableLiveData<>();

    // --- LiveData for Barber Ranking ---
    private final MutableLiveData<Map<String, Integer>> barberRanking = new MutableLiveData<>();


    // --- Getters (no change) ---
    public LiveData<Map<String, Integer>> getHaircutRanking() {
        return haircutRanking;
    }

    public LiveData<Map<String, Integer>> getBarberRanking() {
        return barberRanking;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;


    // --- UPDATED: Method to fetch Haircut Ranking with Filters ---
    public void fetchHaircutRanking(Long startDate, Long endDate, SortType sortType) {
        _isLoading.postValue(true);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            // Use TreeMap for NAME sort (alphabetical), LinkedHashMap for POPULARITY sort (insertion order)
            Map<String, Integer> ranking = (sortType == SortType.NAME) ? new TreeMap<>() : new LinkedHashMap<>();

            // Use StringBuilder to dynamically create the query
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT haircut_choice, COUNT(reservation_id) as count ");
            queryBuilder.append("FROM reservations ");
            queryBuilder.append("WHERE haircut_choice IS NOT NULL AND haircut_choice != '' ");

            // 1. Add DATE filter if dates are provided
            if (startDate != null && endDate != null) {
                queryBuilder.append("AND reservation_time BETWEEN ? AND ? ");
            }

            queryBuilder.append("GROUP BY haircut_choice ");

            // 2. Add SORTING filter
            if (sortType == SortType.NAME) {
                queryBuilder.append("ORDER BY haircut_choice ASC");
            } else {
                // Default to POPULARITY
                queryBuilder.append("ORDER BY count DESC");
            }

            String query = queryBuilder.toString();
            Log.d(TAG, "Executing Haircut Query: " + query);

            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                try (PreparedStatement stmt = conn.prepareStatement(query)) {

                    // 3. Set DATE parameters if they exist
                    if (startDate != null && endDate != null) {
                        // Add 1 day (in millis) to the end date to make the range inclusive
                        long inclusiveEndDate = endDate + (24 * 60 * 60 * 1000);
                        stmt.setTimestamp(1, new Timestamp(startDate));
                        stmt.setTimestamp(2, new Timestamp(inclusiveEndDate));
                        Log.d(TAG, "Filtering dates from " + new Timestamp(startDate) + " to " + new Timestamp(inclusiveEndDate));
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            ranking.put(rs.getString("haircut_choice"), rs.getInt("count"));
                        }
                        haircutRanking.postValue(ranking);
                        _isLoading.postValue(false);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "DB Error fetching haircut ranking: " + e.getMessage(), e);
                haircutRanking.postValue(null); // Post null on error
            }
        });
    }

    // --- UPDATED: Method to fetch Barber Ranking with Filters ---
    public void fetchBarberRanking(Long startDate, Long endDate, SortType sortType) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            Map<String, Integer> ranking = (sortType == SortType.NAME) ? new TreeMap<>() : new LinkedHashMap<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT b.name AS barber_name, COUNT(r.reservation_id) AS count ");
            queryBuilder.append("FROM reservations AS r ");
            queryBuilder.append("JOIN barbers AS b ON r.barber_id = b.barber_id ");

            // 1. Add DATE filter
            if (startDate != null && endDate != null) {
                queryBuilder.append("WHERE r.reservation_time BETWEEN ? AND ? ");
            }

            queryBuilder.append("GROUP BY b.barber_id, b.name "); // Group by both ID and Name

            // 2. Add SORTING filter
            if (sortType == SortType.NAME) {
                queryBuilder.append("ORDER BY barber_name ASC");
            } else {
                // Default to POPULARITY
                queryBuilder.append("ORDER BY count DESC");
            }

            String query = queryBuilder.toString();
            Log.d(TAG, "Executing Barber Query: " + query);

            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                try (PreparedStatement stmt = conn.prepareStatement(query)) {

                    // 3. Set DATE parameters
                    if (startDate != null && endDate != null) {
                        long inclusiveEndDate = endDate + (24 * 60 * 60 * 1000);
                        stmt.setTimestamp(1, new Timestamp(startDate));
                        stmt.setTimestamp(2, new Timestamp(inclusiveEndDate));
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            ranking.put(rs.getString("barber_name"), rs.getInt("count"));
                        }
                        barberRanking.postValue(ranking); // Update LiveData
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "DB Error fetching barber ranking: " + e.getMessage(), e);
                barberRanking.postValue(null); // Post null on error
            }
        });
    }
}