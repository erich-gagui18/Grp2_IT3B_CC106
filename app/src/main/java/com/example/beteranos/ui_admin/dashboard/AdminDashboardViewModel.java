package com.example.beteranos.ui_admin.dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// --- Enum for sorting ---
enum SortType {
    POPULARITY,
    NAME
}

public class AdminDashboardViewModel extends ViewModel {

    private static final String TAG = "AdminDashboardVM";

    // --- LiveData for text (existing management section) ---
    private final MutableLiveData<String> text = new MutableLiveData<>("Welcome to Admin Management");

    public LiveData<String> getText() {
        return text;
    }

    // --- Dashboard-related LiveData ---
    private final MutableLiveData<Map<String, Integer>> haircutRanking = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> barberRanking = new MutableLiveData<>();

    // --- ADDED: Loading state ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    public LiveData<Map<String, Integer>> getHaircutRanking() {
        return haircutRanking;
    }

    public LiveData<Map<String, Integer>> getBarberRanking() {
        return barberRanking;
    }

    // --- UPDATED: Fetch Haircut Ranking with Filters ---
    public void fetchHaircutRanking(Long startDate, Long endDate, SortType sortType) {
        _isLoading.postValue(true);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Map<String, Integer> ranking = (sortType == SortType.NAME) ? new TreeMap<>() : new LinkedHashMap<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT haircut_choice, COUNT(reservation_id) as count ");
            queryBuilder.append("FROM reservations ");
            queryBuilder.append("WHERE haircut_choice IS NOT NULL AND haircut_choice != '' ");

            if (startDate != null && endDate != null) {
                queryBuilder.append("AND reservation_time BETWEEN ? AND ? ");
            }

            queryBuilder.append("GROUP BY haircut_choice ");

            if (sortType == SortType.NAME) {
                queryBuilder.append("ORDER BY haircut_choice ASC");
            } else {
                queryBuilder.append("ORDER BY count DESC");
            }

            String query = queryBuilder.toString();
            Log.d(TAG, "Executing Haircut Query: " + query);

            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    if (startDate != null && endDate != null) {
                        long inclusiveEndDate = endDate + (24 * 60 * 60 * 1000);
                        stmt.setTimestamp(1, new Timestamp(startDate));
                        stmt.setTimestamp(2, new Timestamp(inclusiveEndDate));
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            ranking.put(rs.getString("haircut_choice"), rs.getInt("count"));
                        }
                        haircutRanking.postValue(ranking);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "DB Error fetching haircut ranking: " + e.getMessage(), e);
                haircutRanking.postValue(null);
            } finally {
                // Only set loading to false after both queries are done
                // We'll let the barber ranking query handle this
            }
        });
    }

    // --- UPDATED: Fetch Barber Ranking with Filters ---
    public void fetchBarberRanking(Long startDate, Long endDate, SortType sortType) {
        // Do not set _isLoading(true) here, let the first query do it
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Map<String, Integer> ranking = (sortType == SortType.NAME) ? new TreeMap<>() : new LinkedHashMap<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT b.name AS barber_name, COUNT(r.reservation_id) AS count ");
            queryBuilder.append("FROM reservations AS r ");
            queryBuilder.append("JOIN barbers AS b ON r.barber_id = b.barber_id ");

            if (startDate != null && endDate != null) {
                queryBuilder.append("WHERE r.reservation_time BETWEEN ? AND ? ");
            }

            queryBuilder.append("GROUP BY b.barber_id, b.name ");

            if (sortType == SortType.NAME) {
                queryBuilder.append("ORDER BY barber_name ASC");
            } else {
                queryBuilder.append("ORDER BY count DESC");
            }

            String query = queryBuilder.toString();
            Log.d(TAG, "Executing Barber Query: " + query);

            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    if (startDate != null && endDate != null) {
                        long inclusiveEndDate = endDate + (24 * 60 * 60 * 1000);
                        stmt.setTimestamp(1, new Timestamp(startDate));
                        stmt.setTimestamp(2, new Timestamp(inclusiveEndDate));
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            ranking.put(rs.getString("barber_name"), rs.getInt("count"));
                        }
                        barberRanking.postValue(ranking);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "DB Error fetching barber ranking: " + e.getMessage(), e);
                barberRanking.postValue(null);
            } finally {
                // This is the second query, so now we can stop loading
                _isLoading.postValue(false);
            }
        });
    }
}