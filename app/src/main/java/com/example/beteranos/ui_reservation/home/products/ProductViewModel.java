package com.example.beteranos.ui_reservation.home.products;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.beteranos.ConnectionClass;
import com.example.beteranos.models.Product;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> _products = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LiveData<List<Product>> getProducts() {
        return _products;
    }

    public void fetchProducts() {
        executor.execute(() -> {
            List<Product> list = new ArrayList<>();
            try (Connection conn = new ConnectionClass().CONN()) {
                if (conn != null) {
                    String query = "SELECT * FROM products ORDER BY name ASC";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        list.add(new Product(
                                rs.getInt("product_id"),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getDouble("price"),
                                rs.getInt("stock_quantity"),
                                rs.getBytes("image") // Fetch BLOB
                        ));
                    }
                    _products.postValue(list);
                }
            } catch (Exception e) {
                Log.e("ProductViewModel", "Error fetching products", e);
            }
        });
    }
}