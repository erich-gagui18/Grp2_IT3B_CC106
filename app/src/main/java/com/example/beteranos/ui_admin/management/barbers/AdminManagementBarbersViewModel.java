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
import java.sql.Statement;
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
                if (conn == null) throw new Exception("Database connection failed");

                // We LEFT JOIN on 'day_of_week = Monday' to get a snapshot of the schedule
                String query = "SELECT b.barber_id, b.name, b.specialization, b.day_off, b.image_url, b.is_active, " +
                        "s.start_time, s.end_time " +
                        "FROM barbers b " +
                        "LEFT JOIN barber_schedules s ON b.barber_id = s.barber_id AND s.day_of_week = 'Monday' " +
                        "ORDER BY b.name ASC";

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        String start = rs.getString("start_time");
                        String end = rs.getString("end_time");
                        // Default to standard hours if no schedule exists yet
                        if (start == null) start = "8:00 am";
                        if (end == null) end = "7:00 pm";

                        barberList.add(new Barber(
                                rs.getInt("barber_id"),
                                rs.getString("name"),
                                rs.getString("specialization"),
                                0, "N/A",
                                rs.getString("image_url"),
                                rs.getString("day_off"),
                                rs.getBoolean("is_active"),
                                start, end
                        ));
                    }
                    _allBarbers.postValue(barberList);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching barbers", e);
                _toastMessage.postValue("Error fetching barbers list");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void addBarber(String name, String specialization, String imageUrl, String dayOff, String startTime, String endTime) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                conn.setAutoCommit(false); // Start Transaction

                // 1. Insert Barber
                String insertBarber = "INSERT INTO barbers (name, specialization, image_url, day_off, is_active) VALUES (?, ?, ?, ?, 1)";
                int newBarberId = -1;
                try (PreparedStatement stmt = conn.prepareStatement(insertBarber, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    stmt.setString(3, imageUrl);
                    stmt.setString(4, dayOff);
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) newBarberId = keys.getInt(1);
                    }
                }

                // 2. Insert 7 Schedules (using helper method)
                insertSchedulesForBarber(conn, newBarberId, dayOff, startTime, endTime);

                conn.commit();
                _toastMessage.postValue("Barber added successfully");
                fetchBarbers();

            } catch (Exception e) {
                if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
                Log.e(TAG, "Error adding barber", e);
                _toastMessage.postValue("Error adding: " + e.getMessage());
            } finally {
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {}
                _isLoading.postValue(false);
            }
        });
    }

    // ⭐️ FIXED UPDATE METHOD: Deletes old schedules then inserts new ones
    public void updateBarber(int barberId, String name, String specialization, String imageUrl, String dayOff, String startTime, String endTime) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("Database connection failed");

                conn.setAutoCommit(false); // Start Transaction

                // 1. Update Basic Barber Info
                String updateBarber = "UPDATE barbers SET name=?, specialization=?, image_url=?, day_off=? WHERE barber_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(updateBarber)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    stmt.setString(3, imageUrl);
                    stmt.setString(4, dayOff);
                    stmt.setInt(5, barberId);
                    stmt.executeUpdate();
                }

                // 2. ⭐️ DELETE old schedules (This fixes the issue for old barbers!)
                // If they had no rows, this deletes 0 rows (no error). If they had rows, it clears them.
                String deleteSchedules = "DELETE FROM barber_schedules WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteSchedules)) {
                    stmt.setInt(1, barberId);
                    stmt.executeUpdate();
                }

                // 3. ⭐️ INSERT fresh schedules with the new times
                insertSchedulesForBarber(conn, barberId, dayOff, startTime, endTime);

                conn.commit();
                _toastMessage.postValue("Barber updated successfully");
                fetchBarbers();

            } catch (Exception e) {
                if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
                Log.e(TAG, "Error updating barber", e);
                _toastMessage.postValue("Error updating: " + e.getMessage());
            } finally {
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {}
                _isLoading.postValue(false);
            }
        });
    }

    // ⭐️ HELPER METHOD to insert Mon-Sun schedules (Used by both Add and Update)
    private void insertSchedulesForBarber(Connection conn, int barberId, String dayOff, String startTime, String endTime) throws Exception {
        String insertSchedule = "INSERT INTO barber_schedules (barber_id, day_of_week, is_day_off, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        try (PreparedStatement stmt = conn.prepareStatement(insertSchedule)) {
            for (String day : daysOfWeek) {
                stmt.setInt(1, barberId);
                stmt.setString(2, day);
                // Set is_day_off = 1 if it matches the selected day string
                stmt.setInt(3, day.equalsIgnoreCase(dayOff) ? 1 : 0);
                stmt.setString(4, startTime);
                stmt.setString(5, endTime);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void toggleBarberVisibility(Barber barber) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                String query = "UPDATE barbers SET is_active = ? WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setBoolean(1, !barber.isActive());
                    stmt.setInt(2, barber.getBarberId());
                    stmt.executeUpdate();
                    _toastMessage.postValue("Visibility updated");
                    fetchBarbers();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error toggling visibility", e);
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void deleteBarber(Barber barber) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                // Cascading delete in DB handles schedules
                String query = "DELETE FROM barbers WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, barber.getBarberId());
                    stmt.executeUpdate();
                    _toastMessage.postValue("Barber deleted");
                    fetchBarbers();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting barber", e);
                _isLoading.postValue(false);
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}