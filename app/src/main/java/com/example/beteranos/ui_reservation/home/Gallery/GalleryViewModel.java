package com.example.beteranos.ui_reservation.home.Gallery;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Gallery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<List<Gallery>> _images = new MutableLiveData<>();
    public LiveData<List<Gallery>> images = _images;

    // ⭐️ Added missing isLoading variable
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void fetchGallerys() {
        _isLoading.postValue(true); // Start loading
        executor.execute(() -> {
            List<Gallery> list = new ArrayList<>();
            String query = "SELECT image_id, image_data FROM gallery ORDER BY created_at DESC";

            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(new Gallery(
                            rs.getInt("image_id"),
                            rs.getBytes("image_data")
                    ));
                }
                _images.postValue(list);

            } catch (Exception e) {
                Log.e("GalleryViewModel", "Error fetching images: " + e.getMessage());
            } finally {
                _isLoading.postValue(false); // Stop loading
            }
        });
    }
}