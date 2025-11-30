package com.example.beteranos.ui_reservation.home.products;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> _products = new MutableLiveData<>();
    public LiveData<List<Product>> products = _products;

    // ⭐️ NEW: Loading State ⭐️
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void fetchProducts() {
        _isLoading.postValue(true); // Start loading
        executor.execute(() -> {
            List<Product> list = new ArrayList<>();
            String query = "SELECT * FROM products";
            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            rs.getBytes("image")
                    ));
                }
                _products.postValue(list);

            } catch (Exception e) {
                Log.e("ProductViewModel", "Error: " + e.getMessage());
            } finally {
                _isLoading.postValue(false); // Stop loading
            }
        });
    }
}