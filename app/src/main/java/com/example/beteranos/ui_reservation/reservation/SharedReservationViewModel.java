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
import com.example.beteranos.ui_reservation.reservation.child_fragments.ServicesFragment;

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
    public final MutableLiveData<String> serviceLocation = new MutableLiveData<>("Barbershop");
    public final MutableLiveData<List<Service>> allServices = new MutableLiveData<>();
    // ⭐️ ADDED: Observe selectedServices for price calculation ⭐️
    public final MutableLiveData<List<Service>> selectedServices = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Barber> selectedBarber = new MutableLiveData<>();
    public final MutableLiveData<List<Barber>> allBarbers = new MutableLiveData<>();
    // ⭐️ ADDED: Observe selectedPromo for price calculation ⭐️
    public final MutableLiveData<Promo> selectedPromo = new MutableLiveData<>();
    public final MutableLiveData<List<Promo>> allPromos = new MutableLiveData<>();
    public final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    public final MutableLiveData<String> selectedTime = new MutableLiveData<>();
    public final MutableLiveData<byte[]> paymentReceiptImage = new MutableLiveData<>();
    public final MutableLiveData<String> haircutChoice = new MutableLiveData<>();

    public final MutableLiveData<Double> downPaymentAmount = new MutableLiveData<>();

    // --- ⭐️ NEW: Price Calculation LiveData ⭐️ ---
    public final MutableLiveData<Double> totalPrice = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> promoDiscount = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> finalPrice = new MutableLiveData<>(0.0);

    // --- Dynamic Data & Status ---
    public final MutableLiveData<List<String>> availableTimeSlots = new MutableLiveData<>();
    public final MutableLiveData<Boolean> reservationStatus = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isGuestCodeValid = new MutableLiveData<>();
    public final MutableLiveData<String> newClaimCode = new MutableLiveData<>();

    // --- LiveData for Optional Password Flow ---
    public final MutableLiveData<Integer> promptSetPassword = new MutableLiveData<>();
    public final MutableLiveData<Boolean> passwordUpdateStatus = new MutableLiveData<>();
    public final MutableLiveData<String> customerCheckError = new MutableLiveData<>();
    public final MutableLiveData<Boolean> navigateToServicesSignal = new MutableLiveData<>();

    // LiveData for showing toasts
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    // --- Use one shared thread pool ---
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public SharedReservationViewModel() {
        fetchServicesFromDB();
        fetchBarbersFromDB();
        fetchPromosFromDB();

        // ⭐️ NEW: Set up price calculation observers ⭐️
        selectedServices.observeForever(services -> calculateFinalPrice());
        selectedPromo.observeForever(promo -> calculateFinalPrice());
    }

    // --------------------------------------------------------------------------------
    // ⭐️ NEW CORE BUSINESS LOGIC: DISCOUNT CALCULATION ⭐️
    // --------------------------------------------------------------------------------

    /**
     * Calculates the total price of selected services and applies the active promo discount.
     */
    private void calculateFinalPrice() {
        double currentTotal = 0.0;
        double currentDiscount = 0.0;

        // 1. Calculate Base Total
        List<Service> services = selectedServices.getValue();
        if (services != null) {
            for (Service service : services) {
                currentTotal += service.getPrice();
            }
        }

        // 2. Apply Discount
        Promo promo = selectedPromo.getValue();
        if (promo != null && currentTotal > 0) {
            int percentage = promo.getDiscountPercentage();
            currentDiscount = currentTotal * ((double) percentage / 100.0);
            currentDiscount = Math.round(currentDiscount * 100.0) / 100.0;
        }

        double finalAmount = currentTotal - currentDiscount;

        // 3. Post Results
        totalPrice.setValue(currentTotal);
        promoDiscount.setValue(currentDiscount);
        finalPrice.setValue(Math.max(0.0, finalAmount));

        // ⭐️ CALCULATE AND SET DOWN PAYMENT (50% of final price) ⭐️
        downPaymentAmount.setValue(Math.max(0.0, finalAmount) / 2.0);

        Log.d("PriceCalc", String.format("Total: %.2f, Discount: %.2f, Final: %.2f, DownPayment: %.2f",
                currentTotal, currentDiscount, finalAmount, downPaymentAmount.getValue()));
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
            List<Service> fetchedServices = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                String query = "SELECT service_id, service_name, price FROM services";

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        fetchedServices.add(new Service(
                                rs.getInt("service_id"),
                                rs.getString("service_name"),
                                rs.getDouble("price")
                        ));
                    }
                }
                allServices.postValue(fetchedServices);
            } catch (Exception e) {
                Log.e("ViewModel", "Error fetching services: " + e.getMessage(), e);
            }
        });
    }

    private void fetchBarbersFromDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Barber> fetchedBarbers = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                String query = "SELECT barber_id, name, specialization, day_off, image_url FROM barbers";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        fetchedBarbers.add(new Barber(
                                rs.getInt("barber_id"),
                                rs.getString("name"),
                                rs.getString("specialization"),
                                rs.getString("day_off"),
                                rs.getString("image_url")
                        ));
                    }
                }
                allBarbers.postValue(fetchedBarbers);
            } catch (Exception e) {
                Log.e("ViewModel", "Error fetching barbers: " + e.getMessage(), e);
            }
        });
    }

    private void fetchPromosFromDB() {
        // --- OPTIMIZATION: Removed local executor, uses class executor ---
        executor.execute(() -> {
            List<Promo> fetchedPromos = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                String query = "SELECT promo_id, promo_name, description, discount_percentage, image_name FROM promos";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        fetchedPromos.add(new Promo(
                                rs.getInt("promo_id"),
                                rs.getString("promo_name"),
                                rs.getString("description"),
                                rs.getInt("discount_percentage"),
                                rs.getBytes("image_name")
                        ));
                    }
                }
                allPromos.postValue(fetchedPromos);
            } catch (Exception e) {
                Log.e("ViewModel", "Error fetching promos: " + e.getMessage(), e);
            }
        });
    }

    // --- Service Selectors (Now call calculateFinalPrice) ---
    public void addService(Service service) {
        List<Service> currentList = selectedServices.getValue();
        if (currentList != null && !currentList.contains(service)) {
            currentList.add(service);
            selectedServices.setValue(currentList);
            calculateFinalPrice(); // ⭐️ Trigger calculation ⭐️
        }
    }

    public void removeService(Service service) {
        List<Service> currentList = selectedServices.getValue();
        if (currentList != null) {
            currentList.remove(service);
            selectedServices.setValue(currentList);
            calculateFinalPrice(); // ⭐️ Trigger calculation ⭐️
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
                    stmt.setInt(1, barber.getBarberId());
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
                                // Email exists with password - navigate to ServicesFragment
                                navigateToServicesSignal.postValue(true);
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
            insertStmt.setString(5, emailAddr);
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
        // --- Retrieve ALL necessary values from LiveData ---
        String emailAddr = email.getValue();
        List<Service> services = selectedServices.getValue();
        Barber barber = selectedBarber.getValue();
        Promo promo = selectedPromo.getValue();
        String date = selectedDate.getValue();
        String time = selectedTime.getValue();
        byte[] receiptImageBytes = paymentReceiptImage.getValue();
        String chosenLocation = serviceLocation.getValue();
        String chosenHaircut = haircutChoice.getValue();

        // ⭐️ RETRIEVE PRICE VALUES ⭐️
        Double total = totalPrice.getValue();
        Double discount = promoDiscount.getValue();
        Double finalAmo = finalPrice.getValue();
        Double downPayment = downPaymentAmount.getValue();

        // --- Basic validation (now includes price validation) ---
        if (emailAddr == null || services == null || services.isEmpty() || barber == null || date == null || time == null || receiptImageBytes == null || chosenLocation == null || total == null || finalAmo == null || downPayment == null) {
            reservationStatus.postValue(false);
            Log.e("ViewModel", "Validation failed: Missing essential reservation data for save (including price/receipt).");
            return;
        }

        // Use primitive double values for cleaner SQL insertion
        final double finalTotal = total;
        final double finalDiscount = (discount != null ? discount : 0.0);
        final double finalFinalAmo = finalAmo;
        final double finalDownPayment = downPayment;


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String claimCode = null;
            boolean isTrulyGuest = false;
            int finalCustomerId = customerIdFromSession;
            int newReservationId = -1;
            Connection conn = null;
            boolean reservationSuccess = false;

            try {
                // --- Step 0: Get Connection and Start Transaction ---
                conn = new ConnectionClass().CONN();
                if (conn == null) {
                    throw new SQLException("Database connection failed.");
                }
                conn.setAutoCommit(false);

                // --- Step 1: Determine finalCustomerId ---
                if (finalCustomerId == -1) {
                    // Find guest ID using email (logic remains the same)
                    Log.d("ViewModel", "customerIdFromSession is -1, finding guest ID via email: " + emailAddr);
                    String findIdQuery = "SELECT customer_id FROM customers WHERE email = ?";
                    try (PreparedStatement findStmt = conn.prepareStatement(findIdQuery)) {
                        findStmt.setString(1, emailAddr);
                        try (ResultSet rs = findStmt.executeQuery()) {
                            if (rs.next()) {
                                finalCustomerId = rs.getInt("customer_id");
                                Log.d("ViewModel", "Found guest customer ID: " + finalCustomerId);
                            } else {
                                Log.e("ViewModel", "SAVE FAILED: Cannot find customer ID for email: " + emailAddr);
                                throw new SQLException("Customer record not found for email."); // Throw to trigger rollback
                            }
                        }
                    }
                }

                // --- Step 2: Check if guest needs a claim code ---
                // (Logic remains the same - check if password is null/empty for finalCustomerId)
                String checkPassQuery = "SELECT password FROM customers WHERE customer_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkPassQuery)) {
                    checkStmt.setInt(1, finalCustomerId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            String pass = rs.getString("password");
                            if (pass == null || pass.isEmpty()) {
                                isTrulyGuest = true;
                            }
                        } else {
                            throw new SQLException("Customer ID " + finalCustomerId + " not found during password check!");
                        }
                    }
                }
                if (isTrulyGuest) {
                    claimCode = String.format("%06d", new Random().nextInt(999999));
                    Log.d("ViewModel", "Generating claim code (" + claimCode + ") for guest ID: " + finalCustomerId);
                }

                // --- Step 3: Parse Date/Time (Re-adding this for context) ---
                Timestamp reservationTimestamp =
                        new Timestamp(new SimpleDateFormat("M/d/yyyy hh:mm a", Locale.US).parse(date + " " + time).getTime());


                // --- Step 4: Insert ONE row into reservations table (NOW INCLUDES PRICE FIELDS) ---
                // ⭐️ UPDATED QUERY: Added total_price, discount_amount, final_price, down_payment_amount ⭐️
                String reservationQuery = "INSERT INTO reservations (customer_id, barber_id, promo_id, reservation_time, status, payment_receipt, claim_code, haircut_choice, service_location, total_price, discount_amount, final_price, down_payment_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(reservationQuery, Statement.RETURN_GENERATED_KEYS)) {
                    // Set Customer/Barber/Time/Status (Indices 1-5)
                    stmt.setInt(1, finalCustomerId);
                    stmt.setInt(2, barber.getBarberId());
                    if (promo != null) stmt.setInt(3, promo.getPromoId()); else stmt.setNull(3, Types.INTEGER);
                    stmt.setTimestamp(4, reservationTimestamp);
                    stmt.setString(5, "Pending");

                    // Set Receipt/Code/Haircut/Location (Indices 6-9)
                    stmt.setBytes(6, receiptImageBytes);
                    stmt.setString(7, claimCode);
                    if (chosenHaircut != null && !chosenHaircut.isEmpty()) stmt.setString(8, chosenHaircut); else stmt.setNull(8, Types.VARCHAR);
                    stmt.setString(9, chosenLocation);

                    // ⭐️ SET PRICE VALUES (Indices 10-13) ⭐️
                    stmt.setDouble(10, finalTotal);
                    stmt.setDouble(11, finalDiscount);
                    stmt.setDouble(12, finalFinalAmo);
                    stmt.setDouble(13, finalDownPayment); // This is the amount the user paid

                    int affectedRows = stmt.executeUpdate();

                    // ... (Get generatedKeys and newReservationId) ...

                    if (affectedRows == 0) {
                        throw new SQLException("Creating reservation failed, no rows affected.");
                    }

                    // Get the generated reservation_id for the linking table
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newReservationId = generatedKeys.getInt(1);
                            Log.d("ViewModel", "Main reservation inserted with ID: " + newReservationId);
                        } else {
                            throw new SQLException("Creating reservation failed, no ID obtained.");
                        }
                    }
                }

                // --- Step 5: Insert multiple rows into reservation_services table ---
                if (newReservationId != -1 && services != null && !services.isEmpty()) {
                    String serviceLinkQuery = "INSERT INTO reservation_services (reservation_id, service_id) VALUES (?, ?)";
                    try (PreparedStatement serviceStmt = conn.prepareStatement(serviceLinkQuery)) {
                        for (Service service : services) {
                            serviceStmt.setInt(1, newReservationId); // Link to the main reservation ID
                            serviceStmt.setInt(2, service.getServiceId());  // Link to the specific service ID
                            serviceStmt.addBatch();
                        }
                        int[] serviceRowsAffected = serviceStmt.executeBatch(); // Execute batch insert for services
                        Log.d("ViewModel", "Inserted " + serviceRowsAffected.length + " rows into reservation_services for reservation ID: " + newReservationId);
                        // Optional: Check if serviceRowsAffected.length matches services.size()
                        if (serviceRowsAffected.length != services.size()) {
                            Log.w("ViewModel", "Warning: Service link insert count mismatch. Expected=" + services.size() + ", Actual=" + serviceRowsAffected.length);
                            // Consider if this requires a rollback depending on your business logic
                        }
                    }
                } else {
                    // Handle case where services list might be empty or newReservationId is invalid
                    if (newReservationId == -1) {
                        throw new SQLException("Cannot link services, main reservation ID is invalid.");
                    }
                    // If services is empty, maybe log a warning but don't necessarily fail
                    Log.w("ViewModel", "No services selected to link for reservation ID: " + newReservationId);
                }

                // --- Step 6: Commit transaction ---
                conn.commit();
                reservationSuccess = true;
                Log.d("ViewModel", "Reservation transaction committed successfully.");

            } catch (Exception e) {
                // ... (Error handling and rollback remain the same) ...
                Log.e("ViewModel", "Error during saveReservation transaction: " + e.getMessage(), e);
                if (conn != null) {
                    try {
                        Log.w("ViewModel", "Rolling back transaction due to error.");
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        Log.e("ViewModel", "Error rolling back transaction: " + rollbackEx.getMessage());
                    }
                }
                reservationSuccess = false;

            } finally {
                // ... (Close connection remains the same) ...
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException closeEx) {
                        Log.e("ViewModel", "Error closing connection: " + closeEx.getMessage());
                    }
                }
            }

            // --- Step 9: Post final status ---
            reservationStatus.postValue(reservationSuccess);
            if (reservationSuccess && claimCode != null) {
                newClaimCode.postValue(claimCode);
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
        totalPrice.setValue(0.0);
        promoDiscount.setValue(0.0);
        finalPrice.setValue(0.0);
        downPaymentAmount.setValue(null);
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
        String updateQuery = "UPDATE customers SET first_name = ?, middle_name = ?, last_name = ?, phone_number = ? WHERE customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, fName);
            stmt.setString(2, (mName != null && !mName.isEmpty()) ? mName : null);
            stmt.setString(3, lName);
            stmt.setString(4, phoneNum);
            stmt.setInt(5, customerId);
            stmt.executeUpdate();
        }
    }
}