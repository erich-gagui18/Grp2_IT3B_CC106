package com.example.beteranos.ui_reservation.reservation;

import android.util.Log;
import android.widget.Toast;

import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

    // --- Customer Details ---
    public final MutableLiveData<String> firstName = new MutableLiveData<>();
    public final MutableLiveData<String> middleName = new MutableLiveData<>();
    public final MutableLiveData<String> lastName = new MutableLiveData<>();
    public final MutableLiveData<String> phone = new MutableLiveData<>();
    public final MutableLiveData<String> email = new MutableLiveData<>();

    // --- Selections ---
    public final MutableLiveData<String> serviceLocation = new MutableLiveData<>("Barbershop"); // Default
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

    // --- Dynamic Data & Status ---
    public final MutableLiveData<List<String>> availableTimeSlots = new MutableLiveData<>();
    public final MutableLiveData<Boolean> reservationStatus = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isGuestCodeValid = new MutableLiveData<>();
    public final MutableLiveData<String> newClaimCode = new MutableLiveData<>(); // For guests who *don't* set password

    // --- LiveData for Optional Password Flow ---
    public final MutableLiveData<Integer> promptSetPassword = new MutableLiveData<>(); // Carries customerId
    public final MutableLiveData<Boolean> passwordUpdateStatus = new MutableLiveData<>(); // Result of password update
    public final MutableLiveData<String> customerCheckError = new MutableLiveData<>(); // Errors during check/create
    public final MutableLiveData<Boolean> navigateToServicesSignal = new MutableLiveData<>(); // Signal to navigate

    // LiveData for showing toasts
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public SharedReservationViewModel() {
        fetchServicesFromDB();
        fetchBarbersFromDB();
        fetchPromosFromDB();
    }

    // --- Standard Setters & Fetchers ---
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

    // --- Method called by DetailsFragment to check/create customer and signal next steps ---
    public void checkAndProcessDetails(String fName, String mName, String lName, String phone, String emailAddr, boolean isGuest) {
        setCustomerDetails(fName, mName, lName, phone, emailAddr); // Ensure details are stored in LiveData
        Log.d("ViewModel", "checkAndProcessDetails: Starting. isGuest=" + isGuest);

        if (!isGuest) {
            // Post the message to the LiveData
            toastMessage.postValue("DEBUG: handleNextClick thinks user is NOT a guest!");
        }

        // --- Guest Flow ---
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Log.d("ViewModel", "checkAndProcessDetails: Background task started.");
            try (Connection conn = new ConnectionClass().CONN()) {

                // ✅ Added log to verify connection object
                if (conn == null) {
                    Log.e("ViewModel", "checkAndProcessDetails: Connection is NULL!");
                    customerCheckError.postValue("Database connection failed.");
                    return; // Stop the process early if connection failed
                } else {
                    Log.d("ViewModel", "checkAndProcessDetails: Connection is NOT null — proceeding with queries.");
                }

                int customerId = -1;
                boolean needsPasswordPrompt = false;
                Log.d("ViewModel", "checkAndProcessDetails: DB Connection successful.");

                // 1. Check if email already exists
                String checkQuery = "SELECT customer_id, password FROM customers WHERE email = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, emailAddr);
                    Log.d("ViewModel", "checkAndProcessDetails: Executing email check query for: " + emailAddr);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) { // Email exists
                            customerId = rs.getInt("customer_id");
                            String existingPassword = rs.getString("password");
                            Log.d("ViewModel", "checkAndProcessDetails: Email exists. CustomerID=" + customerId +
                                    ", HasPassword=" + (existingPassword != null && !existingPassword.isEmpty()));

                            if (existingPassword != null && !existingPassword.isEmpty()) {
                                // Email already registered with a password - Error!
                                customerCheckError.postValue("This email is already registered. Please log in.");
                                return; // Stop the process
                            } else {
                                // Email exists without password (guest or incomplete signup)
                                updateCustomerDetails(conn, customerId, fName, mName, lName, phone);
                                needsPasswordPrompt = true; // Okay to prompt for password
                            }
                        } else {
                            // Email does not exist - create a new customer record
                            Log.d("ViewModel", "checkAndProcessDetails: Email does not exist. Creating new customer...");
                            customerId = createGuestCustomer(conn, fName, mName, lName, phone, emailAddr);
                            if (customerId != -1) {
                                needsPasswordPrompt = true; // Prompt for password for the new record
                                Log.d("ViewModel", "checkAndProcessDetails: New customer created. ID=" + customerId);
                            } else {
                                Log.e("ViewModel", "checkAndProcessDetails: Failed to create customer.");
                                customerCheckError.postValue("Failed to create customer record.");
                                return; // Stop the process
                            }
                        }
                    }
                }

                // --- REVISED SIGNALING LOGIC ---
                Log.d("ViewModel", "checkAndProcessDetails: Just before prompt check. needsPasswordPrompt=" + needsPasswordPrompt + ", customerId=" + customerId);
                if (needsPasswordPrompt && customerId != -1) {
                    // Signal password prompt ONLY, DO NOT navigate yet
                    Log.d("ViewModel", "checkAndProcessDetails: Signalling password prompt for CustomerID=" + customerId);
                    promptSetPassword.postValue(customerId);
                } else if (customerId != -1) {
                    // No prompt needed AND no error occurred, so navigate directly
                    Log.d("ViewModel", "checkAndProcessDetails: Password prompt not needed. Signalling navigation.");
                    navigateToServicesSignal.postValue(true);
                } else {
                    // customerId is -1 here, meaning customer creation failed. Error already posted.
                    Log.d("ViewModel", "checkAndProcessDetails: customerId invalid, not signalling prompt or navigation.");
                }
                // --- END REVISED SIGNALING ---

            } catch (Exception e) {
                Log.e("ViewModel", "Error checking/creating customer: " + e.getMessage(), e);
                customerCheckError.postValue("An error occurred during customer check. Please try again.");
            }
        });
    }


    // --- Helper to create a guest customer (no password yet) ---
    private int createGuestCustomer(Connection conn, String fName, String mName, String lName, String phoneNum, String emailAddr) throws SQLException {
        int customerId = -1;
        String insertQuery = "INSERT INTO customers (first_name, middle_name, last_name, phone_number, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, fName);
            insertStmt.setString(2, (mName != null && !mName.isEmpty()) ? mName : null);
            insertStmt.setString(3, lName);
            insertStmt.setString(4, phoneNum);
            insertStmt.setString(5, emailAddr); // Save email even for guests now
            insertStmt.executeUpdate();
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customerId = generatedKeys.getInt(1);
                }
            }
        }
        Log.d("ViewModel", "Created new guest customer with ID: " + customerId);
        return customerId;
    }

    // --- Method to save/update the guest's password ---
    public void updateGuestPassword(int customerId, String password) {
        // --- Hash the password securely using BCrypt ---
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12)); // 12 = cost factor
        // ------------------------------------------------

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean success = false;
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn != null) {
                    String query = "UPDATE customers SET password = ? WHERE customer_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, hashedPassword);
                        stmt.setInt(2, customerId);

                        int rowsAffected = stmt.executeUpdate();
                        success = rowsAffected > 0;

                        if (success)
                            Log.d("ViewModel", "Password updated successfully for customer ID: " + customerId);
                        else
                            Log.w("ViewModel", "Password update failed for customer ID: " + customerId + " (rows affected=0)");
                    }
                } else {
                    Log.e("ViewModel", "Database connection is null.");
                }
            } catch (Exception e) {
                Log.e("ViewModel", "Error updating password for customer ID " + customerId + ": " + e.getMessage(), e);
            }

            passwordUpdateStatus.postValue(success);

            if (success) {
                Log.d("ViewModel", "Password updated, signalling navigation.");
                navigateToServicesSignal.postValue(true);
            }
        });
    }

    // --- UPDATED saveReservation METHOD ---
    public void saveReservation(int customerIdFromSession) {
        // Get values from LiveData needed for saving
        String emailAddr = email.getValue(); // Need email to find guest ID if needed
        List<Service> services = selectedServices.getValue();
        Barber barber = selectedBarber.getValue();
        Promo promo = selectedPromo.getValue();
        String date = selectedDate.getValue();
        String time = selectedTime.getValue();
        byte[] receiptImage = paymentReceiptImage.getValue();
        String chosenLocation = serviceLocation.getValue();
        String chosenHaircut = haircutChoice.getValue();

        // Basic validation for essential reservation data
        if (emailAddr == null || services == null || services.isEmpty() || barber == null || date == null || time == null || receiptImage == null || chosenLocation == null) {
            reservationStatus.postValue(false);
            Log.e("ViewModel", "Validation failed: Missing essential reservation data for save.");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String claimCode = null; // Default to null, generate only if truly a guest
            boolean isTrulyGuest = false; // Assume registered unless proven otherwise
            int finalCustomerId = customerIdFromSession; // Start with the ID passed in

            try (Connection conn = new ConnectionClass().CONN()) {

                // If customerIdFromSession is -1, it implies a guest who might have skipped password.
                // We MUST find their ID using the email stored in the ViewModel.
                if (finalCustomerId == -1) {
                    Log.d("ViewModel", "customerIdFromSession is -1, finding guest ID via email: " + emailAddr);
                    String findIdQuery = "SELECT customer_id FROM customers WHERE email = ?";
                    try (PreparedStatement findStmt = conn.prepareStatement(findIdQuery)) {
                        findStmt.setString(1, emailAddr);
                        try (ResultSet rs = findStmt.executeQuery()) {
                            if (rs.next()) {
                                finalCustomerId = rs.getInt("customer_id");
                                Log.d("ViewModel", "Found guest customer ID: " + finalCustomerId);
                            } else {
                                // This indicates a problem - checkAndProcessDetails should have created them.
                                Log.e("ViewModel", "SAVE FAILED: Cannot find customer ID for email: " + emailAddr);
                                customerCheckError.postValue("Could not find customer record. Please restart booking."); // Use error signal
                                reservationStatus.postValue(false); // Also signal general failure
                                return; // Stop execution
                            }
                        }
                    }
                }

                // Now we have a finalCustomerId (either from session or found via email).
                // Check if this customer actually has a password set.
                String checkPassQuery = "SELECT password FROM customers WHERE customer_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkPassQuery)) {
                    checkStmt.setInt(1, finalCustomerId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            String pass = rs.getString("password");
                            if (pass == null || pass.isEmpty()) {
                                isTrulyGuest = true; // No password found, they are booking as guest
                            }
                        } else {
                            // Customer ID doesn't exist? Should have been caught earlier.
                            Log.e("ViewModel", "SAVE FAILED: Customer ID " + finalCustomerId + " not found during final password check!");
                            customerCheckError.postValue("Customer record inconsistency. Please restart booking.");
                            reservationStatus.postValue(false);
                            return; // Stop execution
                        }
                    }
                }

                // Generate claim code ONLY if they completed as a guest (no password)
                if (isTrulyGuest) {
                    claimCode = String.format("%06d", new Random().nextInt(999999));
                    Log.d("ViewModel", "Generating claim code (" + claimCode + ") for guest customer ID: " + finalCustomerId);
                } else {
                    Log.d("ViewModel", "Booking for registered customer ID: " + finalCustomerId + " (no claim code needed)");
                }

                // Parse Date/Time
                Timestamp reservationTimestamp =
                        new Timestamp(new SimpleDateFormat("M/d/yyyy hh:mm a", Locale.US).parse(date + " " + time).getTime());

                // Prepare Insert Statement
                String query = "INSERT INTO reservations (customer_id, barber_id, service_id, promo_id, reservation_time, status, payment_receipt, claim_code, haircut_choice, service_location) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    boolean haircutSavedForBatch = false;
                    for (Service service : services) {
                        stmt.setInt(1, finalCustomerId);
                        stmt.setInt(2, barber.getId());
                        stmt.setInt(3, service.getId());
                        if (promo != null) stmt.setInt(4, promo.getId()); else stmt.setNull(4, Types.INTEGER);
                        stmt.setTimestamp(5, reservationTimestamp);
                        stmt.setString(6, "Pending"); // Default status
                        stmt.setBytes(7, receiptImage);
                        stmt.setString(8, claimCode); // Will be NULL if not isTrulyGuest
                        // Haircut Choice (Parameter 9)
                        if (!haircutSavedForBatch && chosenHaircut != null && !chosenHaircut.isEmpty()) {
                            stmt.setString(9, chosenHaircut); haircutSavedForBatch = true;
                        } else { stmt.setNull(9, Types.VARCHAR); }
                        // Service Location (Parameter 10)
                        stmt.setString(10, chosenLocation);

                        stmt.addBatch();
                    }
                    // Execute Batch Insert
                    int[] rowsAffected = stmt.executeBatch();
                    if (rowsAffected.length > 0) {
                        Log.d("ViewModel", "Reservation saved successfully for customer ID: " + finalCustomerId);
                        reservationStatus.postValue(true); // Signal success
                        if (claimCode != null) { // Only post claim code if generated
                            newClaimCode.postValue(claimCode);
                        }
                    } else {
                        Log.w("ViewModel", "Reservation save failed (0 rows affected) for customer ID: " + finalCustomerId);
                        reservationStatus.postValue(false); // Signal failure
                    }
                }
            } catch (Exception e) {
                Log.e("ViewModel", "Error during saveReservation background task: " + e.getMessage(), e);
                reservationStatus.postValue(false); // Signal failure on exception
            }
        });
    }

    // --- clearReservationDetails - Ensure signals are cleared ---
    public void clearReservationDetails() {
        firstName.setValue(null);
        middleName.setValue(null);
        lastName.setValue(null);
        phone.setValue(null);
        email.setValue(null);
        serviceLocation.setValue("Barbershop"); // Reset default
        selectedServices.setValue(new ArrayList<>());
        selectedBarber.setValue(null);
        selectedPromo.setValue(null);
        selectedDate.setValue(null);
        selectedTime.setValue(null);
        paymentReceiptImage.setValue(null);
        haircutChoice.setValue(null);
        // Reset Status/Dynamic Data
        availableTimeSlots.setValue(null); // Clear slots
        reservationStatus.setValue(null);
        isGuestCodeValid.setValue(null);
        newClaimCode.setValue(null);
        // Reset password flow signals
        promptSetPassword.setValue(null);
        passwordUpdateStatus.setValue(null);
        customerCheckError.setValue(null);
        navigateToServicesSignal.setValue(null);
        Log.d("ViewModel", "Reservation details cleared.");
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

    // --- Helper to update existing customer details ---
    private void updateCustomerDetails(Connection conn, int customerId, String fName, String mName, String lName, String phoneNum) throws SQLException {
        String updateQuery = "UPDATE customers SET first_name = ?, middle_name = ?, last_name = ?, phone_number = ? WHERE customer_id = ?"; try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, fName);
            stmt.setString(2, (mName != null && !mName.isEmpty()) ? mName : null);
            stmt.setString(3, lName);
            stmt.setString(4, phoneNum);
            stmt.setInt(5, customerId);
            stmt.executeUpdate();
        }
    }
}