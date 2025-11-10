package com.example.beteranos.ui_admin.management.transactions;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionReportViewModel extends ViewModel {

    private static final String TAG = "TransactionReportVM";

    private final MutableLiveData<List<Transaction>> _transactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> transactions = _transactions;

    private final MutableLiveData<String> _totalSales = new MutableLiveData<>("₱0.00");
    public LiveData<String> totalSales = _totalSales;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ---------------------------------------------
    // 1. DATA FETCHING METHOD (Unchanged from your last version)
    // ---------------------------------------------

    public void fetchTransactions(Long startDate, Long endDate, String status) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Transaction> transactionList = new ArrayList<>();
            double grandTotalSales = 0.0;
            List<Object> params = new ArrayList<>();

            String baseQuery = "SELECT " +
                    "    r.reservation_id, " +
                    "    r.reservation_time, " +
                    "    CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                    "    b.name AS barber_name, " +
                    "    r.status, " +
                    "    GROUP_CONCAT(s.service_name SEPARATOR ', ') AS services, " +
                    "    r.total_price, " +
                    "    r.discount_amount, " +
                    "    r.final_price, " +
                    "    r.down_payment_amount " +
                    "FROM reservations r " +
                    "JOIN customers c ON r.customer_id = c.customer_id " +
                    "JOIN barbers b ON r.barber_id = b.barber_id " +
                    "LEFT JOIN reservation_services rs ON r.reservation_id = rs.reservation_id " +
                    "LEFT JOIN services s ON rs.service_id = s.service_id ";

            String whereClause = "WHERE 1=1 ";

            if (startDate != null && endDate != null) {
                whereClause += "AND DATE(r.reservation_time) BETWEEN ? AND ? ";
                params.add(new java.sql.Date(startDate));
                params.add(new java.sql.Date(endDate));
            }

            if ("Pending".equals(status) || "Confirmed".equals(status) || "Completed".equals(status)) {
                whereClause += "AND r.status = ? ";
                params.add(status);
            } else {
                whereClause += "AND r.status IN ('Completed', 'Confirmed', 'Pending') ";
            }

            String fullQuery = baseQuery + whereClause +
                    "GROUP BY " +
                    "    r.reservation_id, customer_name, barber_name, r.reservation_time, r.status, " +
                    "    r.total_price, r.discount_amount, r.final_price, r.down_payment_amount " +
                    "ORDER BY " +
                    "    r.reservation_time DESC";

            Log.d(TAG, "Executing query: " + fullQuery);

            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(fullQuery)) {

                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String txStatus = rs.getString("status");
                        double totalPrice = rs.getDouble("total_price");
                        double discountAmount = rs.getDouble("discount_amount");
                        double downPayment = rs.getDouble("down_payment_amount");

                        double calculatedFinalPrice = totalPrice - discountAmount;
                        double calculatedRemainingBalance = calculatedFinalPrice - downPayment;

                        Transaction tx = new Transaction();
                        tx.setReservationId(String.valueOf(rs.getInt("reservation_id")));
                        tx.setReservationTime(rs.getTimestamp("reservation_time"));
                        tx.setCustomerName(rs.getString("customer_name"));
                        tx.setBarberName(rs.getString("barber_name"));
                        tx.setServices(rs.getString("services"));
                        tx.setTotalPrice(totalPrice);
                        tx.setDiscountAmount(discountAmount);
                        tx.setDownPaymentAmount(downPayment);
                        tx.setStatus(txStatus);
                        tx.setFinalPrice(calculatedFinalPrice);
                        tx.setRemainingBalance(calculatedRemainingBalance);

                        transactionList.add(tx);

                        if ("Completed".equals(txStatus)) {
                            grandTotalSales += calculatedFinalPrice;
                        } else if ("Confirmed".equals(txStatus)) {
                            grandTotalSales += downPayment;
                        }
                    }
                }

                _transactions.postValue(transactionList);
                _totalSales.postValue(String.format(Locale.US, "₱%.2f", grandTotalSales));
            } catch (Exception e) {
                Log.e(TAG, "DB Error fetching transaction report: " + e.getMessage(), e);
                _transactions.postValue(new ArrayList<>());
                _totalSales.postValue(String.format(Locale.US, "₱%.2f", 0.0));
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    // ---------------------------------------------
    // 2. LOGIC TO MARK AS COMPLETED AND PAID (Unchanged)
    // ---------------------------------------------

    public void markTransactionCompletedAndPaid(String reservationId) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) {
                    Log.e(TAG, "DB Connection failed for update.");
                    return;
                }

                String selectPriceQuery = "SELECT final_price FROM reservations WHERE reservation_id = ?";
                double finalPrice = 0.0;
                try (PreparedStatement selectStmt = conn.prepareStatement(selectPriceQuery)) {
                    selectStmt.setString(1, reservationId);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            finalPrice = rs.getDouble("final_price");
                        }
                    }
                }

                if (finalPrice > 0) {
                    String updateQuery = "UPDATE reservations SET status = 'Completed', down_payment_amount = ? WHERE reservation_id = ?";
                    stmt = conn.prepareStatement(updateQuery);
                    stmt.setDouble(1, finalPrice);
                    stmt.setString(2, reservationId);

                    int rowsAffected = stmt.executeUpdate();
                    Log.d(TAG, "Transaction " + reservationId + " marked completed and paid. Rows updated: " + rowsAffected);

                    // ⭐️ UPDATE: Call updated helper with finalPrice for both DP and finalPrice ⭐️
                    updateTransactionInLiveData(reservationId, "Completed", finalPrice, finalPrice);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error marking transaction completed: " + e.getMessage(), e);
            } finally {
                _isLoading.postValue(false);
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (Exception e) { /* ignore */ }
            }
        });
    }

    // ---------------------------------------------
    // 3. ⭐️ NEW METHOD: Confirm Reservation ⭐️
    // ---------------------------------------------

    /**
     * Marks a reservation as "Confirmed". This is used when an admin approves
     * a pending reservation, securing the down payment.
     *
     * @param reservationId The ID of the transaction to update.
     */
    public void confirmReservation(String reservationId) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = new ConnectionClass().CONN();
                if (conn == null) {
                    Log.e(TAG, "DB Connection failed for confirmation.");
                    return;
                }

                // Update the status to 'Confirmed'
                String updateQuery = "UPDATE reservations SET status = 'Confirmed' WHERE reservation_id = ?";
                stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, reservationId);

                int rowsAffected = stmt.executeUpdate();
                Log.d(TAG, "Reservation " + reservationId + " confirmed. Rows updated: " + rowsAffected);

                // Fetch the required data to update LiveData without full re-fetch
                String selectDataQuery = "SELECT total_price, discount_amount, down_payment_amount FROM reservations WHERE reservation_id = ?";
                double finalPrice = 0.0;
                double downPayment = 0.0;
                try (PreparedStatement selectStmt = conn.prepareStatement(selectDataQuery)) {
                    selectStmt.setString(1, reservationId);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            double totalPrice = rs.getDouble("total_price");
                            double discountAmount = rs.getDouble("discount_amount");
                            downPayment = rs.getDouble("down_payment_amount");
                            finalPrice = totalPrice - discountAmount;
                        }
                    }
                }

                // Update the LiveData for UI refresh. Status: 'Confirmed', DownPayment: (Existing DP)
                updateTransactionInLiveData(reservationId, "Confirmed", downPayment, finalPrice);

            } catch (Exception e) {
                Log.e(TAG, "Error confirming reservation: " + e.getMessage(), e);
            } finally {
                _isLoading.postValue(false);
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (Exception e) { /* ignore */ }
            }
        });
    }


    // ---------------------------------------------
    // 4. ⭐️ UPDATED HELPER: Live Data Utility ⭐️
    // ---------------------------------------------

    /**
     * Helper to update the LiveData list without a full database re-fetch.
     * Now accepts finalPrice to correctly recalculate balance.
     */
    private void updateTransactionInLiveData(String reservationId, String newStatus, double newDownPayment, double finalPrice) {
        List<Transaction> currentList = _transactions.getValue();
        if (currentList == null) return;

        List<Transaction> updatedList = new ArrayList<>(currentList);
        boolean found = false;

        for (int i = 0; i < updatedList.size(); i++) {
            Transaction oldTx = updatedList.get(i);

            if (oldTx.getReservationId().equals(reservationId)) {
                Transaction updatedTx = new Transaction();

                // Copy over all original fields
                updatedTx.setReservationId(oldTx.getReservationId());
                updatedTx.setReservationTime(oldTx.getReservationTime());
                updatedTx.setCustomerName(oldTx.getCustomerName());
                updatedTx.setBarberName(oldTx.getBarberName());
                updatedTx.setServices(oldTx.getServices());
                updatedTx.setTotalPrice(oldTx.getTotalPrice());
                updatedTx.setDiscountAmount(oldTx.getDiscountAmount());

                // Set the updated fields
                updatedTx.setFinalPrice(finalPrice); // Use the passed-in finalPrice
                updatedTx.setDownPaymentAmount(newDownPayment);
                updatedTx.setRemainingBalance(finalPrice - newDownPayment); // Recalculate balance
                updatedTx.setStatus(newStatus);

                updatedList.set(i, updatedTx);
                found = true;
                break;
            }
        }

        if (found) {
            _transactions.postValue(updatedList);
            recalculateTotalSales(updatedList);
        }
    }

    private void recalculateTotalSales(List<Transaction> transactions) {
        double grandTotalSales = 0.0;
        for (Transaction tx : transactions) {
            String txStatus = tx.getStatus();
            if ("Completed".equals(txStatus)) {
                grandTotalSales += tx.getFinalPrice();
            } else if ("Confirmed".equals(txStatus)) {
                grandTotalSales += tx.getDownPaymentAmount();
            }
        }
        _totalSales.postValue(String.format(Locale.US, "₱%.2f", grandTotalSales));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}