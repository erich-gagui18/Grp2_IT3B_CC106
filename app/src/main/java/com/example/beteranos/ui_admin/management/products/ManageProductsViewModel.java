package com.example.beteranos.ui_admin.management.products;

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

public class ManageProductsViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> _productList = new MutableLiveData<>();
    public LiveData<List<Product>> productList = _productList;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void fetchProducts() {
        _isLoading.postValue(true);
        executor.execute(() -> {
            List<Product> list = new ArrayList<>();
            String query = "SELECT * FROM products";

            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"), // Fixed: matches schema 'name'
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"), // ⭐️ FIX: matches schema 'stock_quantity'
                            rs.getBytes("image")
                    ));
                }
                _productList.postValue(list);

            } catch (Exception e) {
                Log.e("ManageProductsVM", "Error fetching: " + e.getMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void addProduct(String name, String desc, double price, int stock, byte[] imageBytes) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            // ⭐️ FIX: Used 'stock_quantity' in INSERT
            String query = "INSERT INTO products (name, description, price, stock_quantity, image) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setString(2, desc);
                stmt.setDouble(3, price);
                stmt.setInt(4, stock);
                stmt.setBytes(5, imageBytes);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    _toastMessage.postValue("Success: Product added");
                    fetchProducts();
                } else {
                    _toastMessage.postValue("Error: Failed to add product");
                }
            } catch (Exception e) {
                _toastMessage.postValue("Error: " + e.getMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void updateProduct(int id, String name, String desc, double price, int stock, byte[] imageBytes) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            // ⭐️ FIX: Used 'stock_quantity' in UPDATE
            String query;
            if (imageBytes != null) {
                query = "UPDATE products SET name=?, description=?, price=?, stock_quantity=?, image=? WHERE product_id=?";
            } else {
                query = "UPDATE products SET name=?, description=?, price=?, stock_quantity=? WHERE product_id=?";
            }

            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setString(2, desc);
                stmt.setDouble(3, price);
                stmt.setInt(4, stock);

                if (imageBytes != null) {
                    stmt.setBytes(5, imageBytes);
                    stmt.setInt(6, id);
                } else {
                    stmt.setInt(5, id);
                }

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    _toastMessage.postValue("Success: Product updated");
                    fetchProducts();
                } else {
                    _toastMessage.postValue("Error: Failed to update");
                }
            } catch (Exception e) {
                _toastMessage.postValue("Error: " + e.getMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void deleteProduct(Product product) {
        _isLoading.postValue(true);
        executor.execute(() -> {
            String query = "DELETE FROM products WHERE product_id = ?";
            try (Connection conn = new ConnectionClass().CONN();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, product.getId());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    _toastMessage.postValue("Success: Product deleted");
                    fetchProducts();
                } else {
                    _toastMessage.postValue("Error: Failed to delete");
                }
            } catch (Exception e) {
                _toastMessage.postValue("Error: " + e.getMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void clearToastMessage() {
        _toastMessage.setValue(null);
    }
}