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
            double total = 0.0;

            // This query joins all tables, groups by reservation,
            // concatenates service names, and sums their prices.
            String query = "SELECT " +
                    "    r.reservation_id, " +
                    "    r.reservation_time, " +
                    "    CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                    "    b.name AS barber_name, " +
                    "    GROUP_CONCAT(s.service_name SEPARATOR ', ') AS services, " +
                    "    SUM(s.price) AS total_amount " +
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
                    "    (r.status = 'Confirmed' OR r.status = 'Scheduled') " + // Or use 'Completed' if you have it
                    (startDate != null && endDate != null ? "AND r.reservation_time BETWEEN ? AND ? " : "") +
                    "GROUP BY " +
                    "    r.reservation_id, customer_name, barber_name, r.reservation_time " +
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
                            double amount = rs.getDouble("total_amount");
                            transactionList.add(new Transaction(
                                    rs.getInt("reservation_id"),
                                    rs.getTimestamp("reservation_time"),
                                    rs.getString("customer_name"),
                                    rs.getString("barber_name"),
                                    rs.getString("services"),
                                    amount
                            ));
                            total += amount;
                        }
                    }
                }
                _transactions.postValue(transactionList);
                _totalSales.postValue(String.format(Locale.US, "₱%.2f", total));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching transactions: " + e.getMessage(), e);
                _transactions.postValue(new ArrayList<>()); // Post empty list
                _totalSales.postValue("Error");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }
}