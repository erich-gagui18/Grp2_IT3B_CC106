package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailsViewModel extends ViewModel {

    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public void saveCustomerDetails(String firstName, String lastName, String phone) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = null;
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                conn = connectionClass.CONN();
                if (conn != null) {
                    String query = "INSERT INTO customers (first_name, last_name, phone_number) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.setString(3, phone);

                    int rowsAffected = stmt.executeUpdate();
                    stmt.close();

                    // Post true if the insert was successful
                    saveSuccess.postValue(rowsAffected > 0);
                } else {
                    saveSuccess.postValue(false);
                }
            } catch (SQLException e) {
                Log.e("DetailsViewModel", "Database Error: " + e.getMessage());
                saveSuccess.postValue(false);
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}