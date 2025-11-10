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
import java.sql.Timestamp;
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

    public void fetchTransactions(Long startDate, Long endDate) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Transaction> transactionList = new ArrayList<>();
            double grandTotalSales = 0.0;

            // ⭐️ UPDATED QUERY: Selects all price columns for accurate reporting ⭐️
            String query = "SELECT " +
                    "    r.reservation_id, " +
                    "    r.reservation_time, " +
                    "    CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                    "    b.name AS barber_name, " +
                    "    GROUP_CONCAT(s.service_name SEPARATOR ', ') AS services, " +
                    "    r.total_price, " +       // Original Price before discount
                    "    r.discount_amount, " +   // Discount applied
                    "    r.final_price, " +       // Price after discount (The full transaction value)
                    "    r.down_payment_amount " +// Amount paid at booking
                    "FROM " +
                    "    reservations r " +
                    "JOIN " +
                    "    customers c ON r.customer_id = c.customer_id " +
                    "JOIN " +
                    "    barbers b ON r.barber_id = b.barber_id " +
                    "LEFT JOIN " +
                    "    reservation_services rs ON r.reservation_id = rs.reservation_id " +
                    "LEFT JOIN " +
                    "    services s ON rs.service_id = s.service_id " +
                    "WHERE " +
                    "    r.status = 'Completed' " +
                    (startDate != null && endDate != null ? "AND r.reservation_time BETWEEN ? AND ? " : "") +
                    "GROUP BY " +
                    // Grouping by all selected columns except the GROUP_CONCAT is required,
                    // including the new price fields.
                    "    r.reservation_id, customer_name, barber_name, r.reservation_time, " +
                    "    r.total_price, r.discount_amount, r.final_price, r.down_payment_amount " +
                    "ORDER BY " +
                    "    r.reservation_time DESC";

            Log.d(TAG, "Executing query: " + query);

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
                            // Use final_price as the true transaction amount (what the customer paid in total)
                            double finalPrice = rs.getDouble("final_price");

                            transactionList.add(new Transaction(
                                    rs.getInt("reservation_id"),
                                    rs.getTimestamp("reservation_time"),
                                    rs.getString("customer_name"),
                                    rs.getString("barber_name"),
                                    rs.getString("services"),
                                    finalPrice, // Use finalPrice as the amount for the Transaction model
                                    rs.getDouble("down_payment_amount"), // ⭐️ NEW: Down Payment ⭐️
                                    rs.getDouble("total_price"), // ⭐️ NEW: Total Price ⭐️
                                    rs.getDouble("discount_amount") // ⭐️ NEW: Discount ⭐️
                            ));
                            grandTotalSales += finalPrice;
                        }
                    }
                }
                _transactions.postValue(transactionList);
                _totalSales.postValue(String.format(Locale.US, "₱%.2f", grandTotalSales)); // Sum of all final prices
            } catch (Exception e) {
                Log.e(TAG, "Error fetching transactions: " + e.getMessage(), e);
                _transactions.postValue(new ArrayList<>());
                _totalSales.postValue("Error");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}