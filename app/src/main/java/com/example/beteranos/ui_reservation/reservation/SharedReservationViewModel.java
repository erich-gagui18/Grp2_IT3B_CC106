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
import java.sql.Types; // Ensure this is imported
import java.util.Random; // Ensure this is imported

public class SharedReservationViewModel extends ViewModel {

    // Customer Details
    public final MutableLiveData<String> firstName = new MutableLiveData<>();
    public final MutableLiveData<String> middleName = new MutableLiveData<>();
    public final MutableLiveData<String> lastName = new MutableLiveData<>();
    public final MutableLiveData<String> phone = new MutableLiveData<>();

    public final MutableLiveData<String> email = new MutableLiveData<>();

    // Selections
    public final MutableLiveData<String> serviceLocation = new MutableLiveData<>("Barbershop");
    public final MutableLiveData<List<Service>> allServices = new MutableLiveData<>();
    public final MutableLiveData<List<Service>> selectedServices = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Barber> selectedBarber = new MutableLiveData<>();
    public final MutableLiveData<List<Barber>> allBarbers = new MutableLiveData<>();
    public final MutableLiveData<Promo> selectedPromo = new MutableLiveData<>();
    public final MutableLiveData<List<Promo>> allPromos = new MutableLiveData<>();
    public final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    public final MutableLiveData<String> selectedTime = new MutableLiveData<>();
    public final MutableLiveData<byte[]> paymentReceiptImage = new MutableLiveData<>();
    public final MutableLiveData<String> haircutChoice = new MutableLiveData<>();

    // Dynamic Data & Status
    public final MutableLiveData<List<String>> availableTimeSlots = new MutableLiveData<>();
    public final MutableLiveData<Boolean> reservationStatus = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isGuestCodeValid = new MutableLiveData<>(); // <<< KEEP THIS ONE
    public final MutableLiveData<String> newClaimCode = new MutableLiveData<>();
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

    public void saveReservation(int customerIdFromSession) { // Added customerIdFromSession parameter
        String fName = firstName.getValue();
        String mName = middleName.getValue();
        String lName = lastName.getValue();
        String phoneNum = phone.getValue();
        String emailAddr = email.getValue();
        String chosenLocation = serviceLocation.getValue();
        List<Service> services = selectedServices.getValue();
        Barber barber = selectedBarber.getValue();
        Promo promo = selectedPromo.getValue();
        String date = selectedDate.getValue();
        String time = selectedTime.getValue();
        byte[] receiptImage = paymentReceiptImage.getValue();
        String chosenHaircut = haircutChoice.getValue(); // Get the chosen haircut

        // Basic validation
        if (fName == null || lName == null || emailAddr == null || services == null || services.isEmpty() || barber == null || date == null || time == null || receiptImage == null) {
            reservationStatus.postValue(false);
            Log.e("ViewModel", "Validation failed: Missing required reservation data.");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String claimCode = null; // Initialize claimCode
            try (Connection conn = new ConnectionClass().CONN()) {
                int finalCustomerId = (customerIdFromSession != -1) ? customerIdFromSession : getOrCreateCustomer(conn, fName, mName, lName, phoneNum, emailAddr);

                if (finalCustomerId == -1) {
                    Log.e("ViewModel", "Failed to get or create customer ID.");
                    reservationStatus.postValue(false);
                    return; // Stop if customer ID is invalid
                }

                // Generate claim code only for guests
                if (customerIdFromSession == -1) {
                    claimCode = String.format("%06d", new Random().nextInt(999999));
                }

                SimpleDateFormat parser = new SimpleDateFormat("M/d/yyyy hh:mm a", Locale.US);
                Date reservationDate = parser.parse(date + " " + time);
                Timestamp reservationTimestamp = new Timestamp(reservationDate.getTime());

                // Updated SQL query to include claim_code and haircut_choice
                String query = "INSERT INTO reservations (customer_id, barber_id, service_id, promo_id, reservation_time, status, payment_receipt, claim_code, haircut_choice, service_location) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    boolean haircutSavedForBatch = false; // Flag to save haircut only once per batch
                    for (Service service : services) {
                        stmt.setInt(1, finalCustomerId);
                        stmt.setInt(2, barber.getId());
                        stmt.setString(10, chosenLocation);
                        stmt.setInt(3, service.getId());
                        if (promo != null) stmt.setInt(4, promo.getId());
                        else stmt.setNull(4, Types.INTEGER);
                        stmt.setTimestamp(5, reservationTimestamp);
                        stmt.setString(6, "Pending");
                        stmt.setBytes(7, receiptImage);
                        stmt.setString(8, claimCode); // Set claim code (will be null for logged-in users)

                        // Set haircut choice (only once per reservation batch if provided)
                        if (!haircutSavedForBatch && chosenHaircut != null && !chosenHaircut.isEmpty()) {
                            stmt.setString(9, chosenHaircut);
                            haircutSavedForBatch = true;
                        } else {
                            stmt.setNull(9, Types.VARCHAR); // Set null otherwise
                        }

                        stmt.addBatch();
                    }
                    int[] rowsAffected = stmt.executeBatch(); // Execute the batch insert
                    if (rowsAffected.length > 0) {
                        reservationStatus.postValue(true); // Signal success
                        if (claimCode != null) {
                            newClaimCode.postValue(claimCode); // Post claim code if it was a guest
                        }
                    } else {
                        reservationStatus.postValue(false); // Signal failure if no rows affected
                    }
                }
            } catch (Exception e) {
                Log.e("ViewModel", "Error saving reservation: " + e.getMessage(), e);
                reservationStatus.postValue(false); // Signal failure on exception
            }
            // No finally block needed here for connection closing due to try-with-resources
        });
    }

    private int getOrCreateCustomer(Connection conn, String fName, String mName, String lName, String phoneNum, String emailAddr) throws SQLException {
        int customerId = -1;
        // 1. Check if user exists by email (unique identifier)
        String emailQuery = "SELECT customer_id FROM customers WHERE email = ?";
        try (PreparedStatement emailStmt = conn.prepareStatement(emailQuery)) {
            emailStmt.setString(1, emailAddr);
            try (ResultSet rs = emailStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id"); // Return existing customer ID
                }
            }
        }

        // 2. If no user by email, check if a guest record might exist (less reliable check)
        String guestQuery = "SELECT customer_id FROM customers WHERE first_name = ? AND last_name = ? AND phone_number = ? AND email IS NULL"; // Check for existing guests without email
        try (PreparedStatement guestStmt = conn.prepareStatement(guestQuery)) {
            guestStmt.setString(1, fName);
            guestStmt.setString(2, lName);
            guestStmt.setString(3, phoneNum);
            try (ResultSet rs = guestStmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                    // Update the existing guest record with the email
                    String updateEmailSql = "UPDATE customers SET email = ? WHERE customer_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateEmailSql)) {
                        updateStmt.setString(1, emailAddr);
                        updateStmt.setInt(2, customerId);
                        updateStmt.executeUpdate();
                    }
                    return customerId; // Return the updated guest customer ID
                }
            }
        }

        // 3. If no matching record found, create a new customer
        String insertQuery = "INSERT INTO customers (first_name, middle_name, last_name, phone_number, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, fName);
            insertStmt.setString(2, (mName != null && !mName.isEmpty()) ? mName : null); // Handle optional middle name
            insertStmt.setString(3, lName);
            insertStmt.setString(4, phoneNum);
            insertStmt.setString(5, emailAddr);
            insertStmt.executeUpdate();
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customerId = generatedKeys.getInt(1); // Get the new customer ID
                }
            }
        }
        return customerId; // Return the newly created customer ID
    }

    //clearReservationDetails
    public void clearReservationDetails() {
        firstName.setValue(null);
        middleName.setValue(null);
        lastName.setValue(null);
        phone.setValue(null);
        email.setValue(null);
        serviceLocation.setValue("Barbershop");
        selectedServices.setValue(new ArrayList<>());
        selectedBarber.setValue(null);
        selectedPromo.setValue(null);
        selectedDate.setValue(null);
        selectedTime.setValue(null);
        paymentReceiptImage.setValue(null);
        reservationStatus.setValue(null);
        isGuestCodeValid.setValue(null);
        newClaimCode.setValue(null); // Clear claim code
        haircutChoice.setValue(null); // Clear haircut choice
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