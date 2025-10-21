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
    public final MutableLiveData<String> middleName = new MutableLiveData<>();
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

    // Receipt Image
    public final MutableLiveData<byte[]> paymentReceiptImage = new MutableLiveData<>();

    // LiveData for dynamic scheduling data
    public final MutableLiveData<List<String>> availableTimeSlots = new MutableLiveData<>();
    public final MutableLiveData<Boolean> reservationStatus = new MutableLiveData<>();

    // LiveData for guest email
    public final MutableLiveData<String> email = new MutableLiveData<>();

    // LiveData for guest code
    public final MutableLiveData<Boolean> isGuestCodeValid = new MutableLiveData<>();

    public SharedReservationViewModel() {
        fetchServicesFromDB();
        fetchBarbersFromDB();
        fetchPromosFromDB();
    }

    public void setCustomerDetails(String fName, String mName, String lName, String pNum, String emailAddr) {
        firstName.setValue(fName);
        middleName.setValue(mName);
        lastName.setValue(lName);
        phone.setValue(pNum);
        email.setValue(emailAddr);
    }

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
                if (conn != null) try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
                if (conn != null) try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
                if (conn != null) try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

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

    public void fetchAvailableSlots(long dateInMillis) {
        Barber barber = selectedBarber.getValue();
        if (barber == null) {
            availableTimeSlots.postValue(new ArrayList<>());
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
                    while (rs.next()) {
                        bookedSlots.add(timeFormat.format(rs.getTimestamp("reservation_time")));
                    }

                    allSlots.removeAll(bookedSlots);
                    availableTimeSlots.postValue(allSlots);
                }
            } catch (SQLException e) {
                Log.e("ViewModel", "DB Error fetching slots: " + e.getMessage());
            } finally {
                if (conn != null) try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void saveReservation() {
        String fName = firstName.getValue();
        String mName = middleName.getValue();
        String lName = lastName.getValue();
        String phoneNum = phone.getValue();
        String emailAddr = email.getValue();
        List<Service> services = selectedServices.getValue();
        Barber barber = selectedBarber.getValue();
        Promo promo = selectedPromo.getValue();
        String date = selectedDate.getValue();
        String time = selectedTime.getValue();
        byte[] receiptImage = paymentReceiptImage.getValue();

        if (fName == null || lName == null || services == null || services.isEmpty() || barber == null || date == null || time == null || receiptImage == null || emailAddr == null) {
            reservationStatus.postValue(false);
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    // Use the updated method signature that includes email
                    int customerId = getOrCreateCustomer(conn, fName, mName, lName, phoneNum, emailAddr);
                    if (customerId == -1) {
                        reservationStatus.postValue(false);
                        return;
                    }

                    SimpleDateFormat parser = new SimpleDateFormat("M/d/yyyy hh:mm a", Locale.US);
                    Date reservationDate = parser.parse(date + " " + time);
                    Timestamp reservationTimestamp = new Timestamp(reservationDate.getTime());

                    String reservationQuery = "INSERT INTO reservations (customer_id, barber_id, service_id, promo_id, reservation_time, status, payment_receipt) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement reservationStmt = conn.prepareStatement(reservationQuery);

                    for (Service service : services) {
                        reservationStmt.setInt(1, customerId);
                        reservationStmt.setInt(2, barber.getId());
                        reservationStmt.setInt(3, service.getId());
                        if (promo != null) {
                            reservationStmt.setInt(4, promo.getId());
                        } else {
                            reservationStmt.setNull(4, Types.INTEGER);
                        }
                        reservationStmt.setTimestamp(5, reservationTimestamp);
                        reservationStmt.setString(6, "Pending");
                        reservationStmt.setBytes(7, receiptImage);
                        reservationStmt.addBatch();
                    }
                    reservationStmt.executeBatch();
                    reservationStatus.postValue(true);
                } else {
                    reservationStatus.postValue(false);
                }
            } catch (Exception e) {
                Log.e("ViewModel", "Error saving reservation: " + e.getMessage(), e);
                reservationStatus.postValue(false);
            } finally {
                if (conn != null) try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int getOrCreateCustomer(Connection conn, String fName, String mName, String lName, String phoneNum, String email) throws SQLException {
        int customerId = -1;
        // First, try to find an existing customer by email, as it should be unique
        String query = "SELECT customer_id FROM customers WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                } else {
                    // If not found, create a new customer record
                    String insertQuery = "INSERT INTO customers (first_name, middle_name, last_name, phone_number, email) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, fName);
                        insertStmt.setString(2, mName != null ? mName : "");
                        insertStmt.setString(3, lName);
                        insertStmt.setString(4, phoneNum != null ? phoneNum : "");
                        insertStmt.setString(5, email);
                        insertStmt.executeUpdate();
                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                customerId = generatedKeys.getInt(1);
                            }
                        }
                    }
                }
            }
        }
        return customerId;
    }

    public void clearReservationDetails() {
        firstName.setValue(null);
        middleName.setValue(null);
        lastName.setValue(null);
        phone.setValue(null);
        email.setValue(null);
        selectedServices.setValue(new ArrayList<>());
        selectedBarber.setValue(null);
        selectedPromo.setValue(null);
        selectedDate.setValue(null);
        selectedTime.setValue(null);
        paymentReceiptImage.setValue(null);
        reservationStatus.setValue(null);
        isGuestCodeValid.setValue(null);
    }

    public void validateGuestCode(String code) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean isValid = false;
            Connection conn = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn != null) {
                    String query = "SELECT is_used FROM guest_codes WHERE code_value = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, code);
                        try (ResultSet rs = stmt.executeQuery()) {
                            // Check if a code was found AND it has not been used yet
                            if (rs.next() && !rs.getBoolean("is_used")) {
                                isValid = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ViewModel", "DB Error validating guest code: " + e.getMessage());
            } finally {
                if (conn != null) try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            isGuestCodeValid.postValue(isValid);
        });
    }
}