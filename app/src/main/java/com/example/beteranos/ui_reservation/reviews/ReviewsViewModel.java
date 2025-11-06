package com.example.beteranos.ui_reservation.reviews;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Barber;
import com.example.beteranos.models.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewsViewModel extends ViewModel {

    private static final String TAG = "ReviewsViewModel";

    private final MutableLiveData<List<Review>> _reviewsList = new MutableLiveData<>();
    public LiveData<List<Review>> reviewsList = _reviewsList;

    private final MutableLiveData<List<Barber>> _allBarbers = new MutableLiveData<>();
    public LiveData<List<Barber>> allBarbers = _allBarbers;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ReviewsViewModel() {
        fetchReviews();
        fetchBarbers();
    }

    public void fetchReviews() {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Review> reviewList = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");

                // --- LIFO (Last In, First Out) ---
                String query = "SELECT r.review_id, CONCAT(c.first_name, ' ', SUBSTR(c.last_name, 1, 1), '.') AS customer_name, " +
                        "b.name AS barber_name, r.rating, r.comment, r.created_at " +
                        "FROM reviews r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN barbers b ON r.barber_id = b.barber_id " +
                        "ORDER BY r.created_at DESC"; // <-- ORDER BY DESC handles LIFO

                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        reviewList.add(new Review(
                                rs.getInt("review_id"),
                                rs.getString("customer_name"),
                                rs.getString("barber_name"),
                                rs.getInt("rating"),
                                rs.getString("comment"),
                                rs.getTimestamp("created_at")
                        ));
                    }
                    _reviewsList.postValue(reviewList);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching reviews: " + e.getMessage(), e);
                _toastMessage.postValue("Error fetching reviews");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void fetchBarbers() {
        executor.execute(() -> {
            List<Barber> barberList = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");
                String query = "SELECT barber_id, name, specialization FROM barbers";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        barberList.add(new Barber(
                                rs.getInt("barber_id"),
                                rs.getString("name"),
                                rs.getString("specialization")
                        ));
                    }
                    _allBarbers.postValue(barberList);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching barbers: " + e.getMessage(), e);
            }
        });
    }

    public void submitReview(int customerId, int barberId, int rating, String comment) {
        if (customerId == -1) {
            _toastMessage.postValue("You must be logged in to post a review.");
            return;
        }

        _isLoading.postValue(true);
        executor.execute(() -> {
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn == null) throw new Exception("DB Connection Failed");
                String query = "INSERT INTO reviews (customer_id, barber_id, rating, comment) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, barberId);
                    stmt.setInt(3, rating);
                    stmt.setString(4, comment);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        _toastMessage.postValue("Review submitted successfully!");
                        fetchReviews(); // Refresh the list
                    } else {
                        throw new Exception("Insert failed, no rows affected.");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error submitting review: " + e.getMessage(), e);
                _toastMessage.postValue("Error submitting review. Please try again.");
            } finally {
                // isLoading will be set to false by fetchReviews()
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}