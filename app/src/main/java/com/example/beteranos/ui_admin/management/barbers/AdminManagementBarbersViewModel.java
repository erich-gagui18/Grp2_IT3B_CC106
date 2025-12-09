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
                if (conn == null) {
                    throw new Exception("Database connection failed");
                }

                // ⭐️ UPDATED QUERY: JOIN barbers with barber_schedules
                // We LEFT JOIN on 'day_of_week = Monday' to get a sample start/end time
                // without duplicating rows for every day of the week.
                String query = "SELECT b.barber_id, b.name, b.specialization, b.day_off, b.image_url, b.is_active, " +
                        "s.start_time, s.end_time " +
                        "FROM barbers b " +
                        "LEFT JOIN barber_schedules s ON b.barber_id = s.barber_id AND s.day_of_week = 'Monday' " +
                        "ORDER BY b.name ASC";

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        // Handle potential NULLs if schedule is missing
                        String start = rs.getString("start_time");
                        String end = rs.getString("end_time");
                        if (start == null) start = "8:00 am";
                        if (end == null) end = "7:00 pm";

                        // ⭐️ UPDATED: Use 10-parameter constructor (includes start/end time)
                        barberList.add(new Barber(
                                rs.getInt("barber_id"),
                                rs.getString("name"),
                                rs.getString("specialization"),
                                0, // Dummy experience_years
                                "N/A", // Dummy contact_number
                                rs.getString("image_url"),
                                rs.getString("day_off"),
                                rs.getBoolean("is_active"),
                                start, // ⭐️ Time from Schedule Table
                                end    // ⭐️ Time from Schedule Table
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

    // ⭐️ UPDATED: Accepts start/end times and inserts 7 schedule rows
    public void addBarber(String name, String specialization, String imageUrl, String dayOff, String startTime, String endTime) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("Database connection failed");

                // ⭐️ START TRANSACTION
                conn.setAutoCommit(false);

                // 1. Insert into BARBERS table
                String insertBarber = "INSERT INTO barbers (name, specialization, image_url, day_off, is_active) VALUES (?, ?, ?, ?, 1)";
                int newBarberId = -1;

                try (PreparedStatement stmt = conn.prepareStatement(insertBarber, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    stmt.setString(3, imageUrl);
                    stmt.setString(4, dayOff);

                    int rows = stmt.executeUpdate();
                    if (rows == 0) throw new Exception("Creating barber failed, no rows affected.");

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newBarberId = generatedKeys.getInt(1); // Get the generated ID
                        } else {
                            throw new Exception("Creating barber failed, no ID obtained.");
                        }
                    }
                }

                // ⭐️ 2. Insert 7 Rows into barber_schedules (Mon-Sun)
                String insertSchedule = "INSERT INTO barber_schedules (barber_id, day_of_week, is_day_off, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
                String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                try (PreparedStatement stmt = conn.prepareStatement(insertSchedule)) {
                    for (String day : daysOfWeek) {
                        stmt.setInt(1, newBarberId);
                        stmt.setString(2, day);
                        // Check if this day matches the chosen Day Off
                        stmt.setInt(3, day.equalsIgnoreCase(dayOff) ? 1 : 0);
                        stmt.setString(4, startTime);
                        stmt.setString(5, endTime);
                        stmt.addBatch(); // Batch for performance
                    }
                    stmt.executeBatch();
                }

                // ⭐️ COMMIT TRANSACTION
                conn.commit();

                _toastMessage.postValue("Barber added successfully");
                fetchBarbers(); // Refresh list

            } catch (Exception e) {
                Log.e(TAG, "Error adding barber: " + e.getMessage(), e);
                _toastMessage.postValue("Error adding barber: " + e.getMessage());
                // Rollback on error
                if (conn != null) {
                    try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
                }
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) { ex.printStackTrace(); }
                }
                _isLoading.postValue(false);
            }
        });
    }

    // ⭐️ UPDATED: Accepts start/end times and updates all schedule rows
    public void updateBarber(int barberId, String name, String specialization, String imageUrl, String dayOff, String startTime, String endTime) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) throw new Exception("Database connection failed");

                // ⭐️ START TRANSACTION
                conn.setAutoCommit(false);

                // 1. Update BARBERS Table
                String updateBarber = "UPDATE barbers SET name = ?, specialization = ?, image_url = ?, day_off = ? WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateBarber)) {
                    stmt.setString(1, name);
                    stmt.setString(2, specialization);
                    stmt.setString(3, imageUrl);
                    stmt.setString(4, dayOff);
                    stmt.setInt(5, barberId);
                    stmt.executeUpdate();
                }

                // 2. Update Times for ALL days for this barber and reset is_day_off
                String updateTimes = "UPDATE barber_schedules SET start_time=?, end_time=?, is_day_off=0 WHERE barber_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(updateTimes)) {
                    stmt.setString(1, startTime);
                    stmt.setString(2, endTime);
                    stmt.setInt(3, barberId);
                    stmt.executeUpdate();
                }

                // 3. Set the specific Day Off (if not "No day off")
                if (dayOff != null && !dayOff.equalsIgnoreCase("No day off")) {
                    String setDayOff = "UPDATE barber_schedules SET is_day_off=1 WHERE barber_id=? AND day_of_week=?";
                    try (PreparedStatement stmt = conn.prepareStatement(setDayOff)) {
                        stmt.setInt(1, barberId);
                        stmt.setString(2, dayOff); // Matches ENUM string in DB
                        stmt.executeUpdate();
                    }
                }

                // ⭐️ COMMIT TRANSACTION
                conn.commit();

                _toastMessage.postValue("Barber updated successfully");
                fetchBarbers();

            } catch (Exception e) {
                Log.e(TAG, "Error updating barber: " + e.getMessage(), e);
                _toastMessage.postValue("Error updating barber: " + e.getMessage());
                if (conn != null) {
                    try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
                }
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) { ex.printStackTrace(); }
                }
                _isLoading.postValue(false);
            }
        });
    }

    public void toggleBarberVisibility(Barber barber) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            boolean newStatus = !barber.isActive();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

                String query = "UPDATE barbers SET is_active = ? WHERE barber_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setBoolean(1, newStatus);
                    stmt.setInt(2, barber.getBarberId());

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Barber is now " + (newStatus ? "Visible" : "Hidden"));
                        fetchBarbers();
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

                // Note: Schedules will automatically be deleted due to ON DELETE CASCADE
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