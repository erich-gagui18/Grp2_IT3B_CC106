package com.example.beteranos.ui_reservation.reservation;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;

public class SharedReservationViewModel extends ViewModel {

    // Customer Details
    public final MutableLiveData<String> firstName = new MutableLiveData<>();
    public final MutableLiveData<String> lastName = new MutableLiveData<>();
    public final MutableLiveData<String> phone = new MutableLiveData<>();

    // Service Selection (Multi-select)
    public final MutableLiveData<List<Service>> allServices = new MutableLiveData<>();
    public final MutableLiveData<List<Service>> selectedServices = new MutableLiveData<>(new ArrayList<>());

    // Barber Selection (Single-select)
    public final MutableLiveData<List<Barber>> allBarbers = new MutableLiveData<>();
    public final MutableLiveData<Barber> selectedBarber = new MutableLiveData<>();

    // Promo Selection (Single-select)
    public final MutableLiveData<List<Promo>> allPromos = new MutableLiveData<>();
    public final MutableLiveData<Promo> selectedPromo = new MutableLiveData<>();

    // Schedule Selection
    public final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    public final MutableLiveData<String> selectedTime = new MutableLiveData<>();

    // --- INTEGRATED CODE ---
    // LiveData for dynamic scheduling data
    public final MutableLiveData<List<String>> availableTimeSlots = new MutableLiveData<>();
    public final MutableLiveData<Boolean> reservationStatus = new MutableLiveData<>();


    public SharedReservationViewModel() {
        fetchServicesFromDB();
        fetchBarbersFromDB();
        fetchPromosFromDB();
    }

    public void setCustomerDetails(String fName, String lName, String pNum) {
        firstName.setValue(fName);
        lastName.setValue(lName);
        phone.setValue(pNum);
    }

    // --- Database Fetching Methods (Your existing code) ---

    private void fetchServicesFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            List<Service> fetchedServices = new ArrayList<>();
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT service_id, service_name, price FROM services";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        fetchedServices.add(new Service(rs.getInt("service_id"), rs.getString("service_name"), rs.getDouble("price")));
                    }
                    allServices.postValue(fetchedServices);
                }
            } catch (SQLException e) {
                Log.e("SharedViewModel", "DB Error (Services): " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void fetchBarbersFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            List<Barber> fetchedBarbers = new ArrayList<>();
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT barber_id, name FROM barbers";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        fetchedBarbers.add(new Barber(rs.getInt("barber_id"), rs.getString("name")));
                    }
                    allBarbers.postValue(fetchedBarbers);
                }
            } catch (SQLException e) {
                Log.e("SharedViewModel", "DB Error (Barbers): " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void fetchPromosFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            List<Promo> fetchedPromos = new ArrayList<>();
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT promo_id, promo_name, description, image_name FROM promos WHERE is_active = 1";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        fetchedPromos.add(new Promo(rs.getInt("promo_id"), rs.getString("promo_name"), rs.getString("description"), rs.getString("image_name")));
                    }
                    allPromos.postValue(fetchedPromos);
                }
            } catch (SQLException e) {
                Log.e("SharedViewModel", "DB Error (Promos): " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    // --- Selection Management Methods (Your existing code) ---

    public void addService(Service service) {
        List<Service> currentList = selectedServices.getValue();
        if (currentList != null && !currentList.contains(service)) {
            currentList.add(service);
            selectedServices.setValue(currentList);
        }
    }

    public void removeService(Service service) {
        List<Service> currentList = selectedServices.getValue();
        if (currentList != null) {
            currentList.remove(service);
            selectedServices.setValue(currentList);
        }
    }

    // --- INTEGRATED CODE ---
    // Methods for fetching available slots and saving the reservation.

    public void fetchAvailableSlots(long dateInMillis) {
        Barber barber = selectedBarber.getValue();
        if (barber == null) {
            availableTimeSlots.postValue(new ArrayList<>()); // No barber selected, no slots available
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<String> allSlots = new ArrayList<>(Arrays.asList("09:00 AM", "10:00 AM", "11:00 AM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"));
            List<String> bookedSlots = new ArrayList<>();
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT reservation_time FROM reservations WHERE barber_id = ? AND DATE(reservation_time) = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, barber.getId());
                    stmt.setDate(2, new java.sql.Date(dateInMillis));
                    ResultSet rs = stmt.executeQuery();

                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                    while(rs.next()) {
                        bookedSlots.add(timeFormat.format(rs.getTimestamp("reservation_time")));
                    }

                    allSlots.removeAll(bookedSlots);
                    availableTimeSlots.postValue(allSlots);
                }
            } catch (SQLException e) {
                Log.e("ViewModel", "DB Error fetching slots: " + e.getMessage());
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    public void saveReservation() {
        String fName = firstName.getValue();
        String lName = lastName.getValue();
        String phoneNum = phone.getValue();
        List<Service> services = selectedServices.getValue();
        Barber barber = selectedBarber.getValue();
        String date = selectedDate.getValue();
        String time = selectedTime.getValue();

        if (fName == null || services == null || services.isEmpty() || barber == null || date == null || time == null) {
            reservationStatus.postValue(false);
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    int customerId = getOrCreateCustomer(conn, fName, lName, phoneNum);
                    if (customerId == -1) {
                        reservationStatus.postValue(false);
                        return;
                    }

                    SimpleDateFormat parser = new SimpleDateFormat("M/d/yyyy hh:mm a", Locale.US);
                    Date reservationDate = parser.parse(date + " " + time);
                    Timestamp reservationTimestamp = new Timestamp(reservationDate.getTime());

                    String reservationQuery = "INSERT INTO reservations (customer_id, barber_id, service_id, reservation_time, status) VALUES (?, ?, ?, ?, 'Scheduled')";
                    PreparedStatement reservationStmt = conn.prepareStatement(reservationQuery);

                    for(Service service : services) {
                        reservationStmt.setInt(1, customerId);
                        reservationStmt.setInt(2, barber.getId());
                        reservationStmt.setInt(3, service.getId());
                        reservationStmt.setTimestamp(4, reservationTimestamp);
                        reservationStmt.addBatch();
                    }
                    reservationStmt.executeBatch();
                    reservationStatus.postValue(true);
                } else {
                    reservationStatus.postValue(false);
                }
            } catch (Exception e) {
                Log.e("ViewModel", "Error saving reservation: " + e.getMessage());
                reservationStatus.postValue(false);
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private int getOrCreateCustomer(Connection conn, String fName, String lName, String phoneNum) throws SQLException {
        int customerId = -1;
        String customerQuery = "SELECT customer_id FROM customers WHERE first_name = ? AND last_name = ? AND phone_number = ?";
        PreparedStatement stmt = conn.prepareStatement(customerQuery);
        stmt.setString(1, fName);
        stmt.setString(2, lName);
        stmt.setString(3, phoneNum);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            customerId = rs.getInt("customer_id");
        } else {
            String insertCustomer = "INSERT INTO customers (first_name, last_name, phone_number) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, fName);
            stmt.setString(2, lName);
            stmt.setString(3, phoneNum);
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                customerId = generatedKeys.getInt(1);
            }
        }
        return customerId;
    }

    public void clearReservationDetails() {
        firstName.setValue(null);
        lastName.setValue(null);
        phone.setValue(null);
        selectedServices.setValue(new ArrayList<>());
        selectedBarber.setValue(null);
        selectedPromo.setValue(null);
        selectedDate.setValue(null);
        selectedTime.setValue(null);
        reservationStatus.setValue(null); // Reset status to avoid re-triggering observer
    }
}