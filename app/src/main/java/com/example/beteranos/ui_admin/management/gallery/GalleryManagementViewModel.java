package com.example.beteranos.ui_admin.management.gallery;

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

public class GalleryManagementViewModel extends ViewModel {

    private final MutableLiveData<List<Gallery>> _gallerys = new MutableLiveData<>();
    public LiveData<List<Gallery>> gallerys = _gallerys;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void fetchImages() {
        _isLoading.postValue(true);
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
                _gallerys.postValue(list);

            } catch (Exception e) {
                Log.e("GalleryVM", "Error fetching images: " + e.getMessage());
                _toastMessage.postValue("Error fetching images");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void uploadImage(byte[] imageBytes) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            String query = "INSERT INTO gallery (image_data) VALUES (?)";
            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setBytes(1, imageBytes);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    _toastMessage.postValue("Image uploaded successfully");
                    fetchImages(); // Refresh list
                } else {
                    _toastMessage.postValue("Upload failed");
                }
            } catch (Exception e) {
                Log.e("GalleryVM", "Error uploading: " + e.getMessage());
                _toastMessage.postValue("Error uploading image");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void deleteImage(Gallery image) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            String query = "DELETE FROM gallery WHERE image_id = ?";
            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, image.getId());
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    _toastMessage.postValue("Image deleted");
                    fetchImages(); // Refresh list
                } else {
                    _toastMessage.postValue("Delete failed");
                }
            } catch (Exception e) {
                Log.e("GalleryVM", "Error deleting: " + e.getMessage());
                _toastMessage.postValue("Error deleting image");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}