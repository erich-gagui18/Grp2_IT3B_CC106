package com.example.beteranos.ui_reservation.home.data_barber; // ⭐️ Correct Package

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Barber;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarberProfileViewModel extends ViewModel {

    private final MutableLiveData<List<Barber>> _barbers = new MutableLiveData<>();
    public LiveData<List<Barber>> barbers = _barbers;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void fetchBarbers() {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Barber> fetchedBarbers = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("Database connection failed");

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
                _barbers.postValue(fetchedBarbers);
            } catch (Exception e) {
                Log.e("BarberProfileVM", "Error fetching barbers: " + e.getMessage());
                _barbers.postValue(null);
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