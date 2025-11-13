package com.example.beteranos.ui_reservation.home.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beteranos.R;
// ⭐ Confirmed Import Path ⭐
import com.example.beteranos.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> _products = new MutableLiveData<>();
    public LiveData<List<Product>> getProducts() {
        return _products;
    }

    public ProductViewModel() {
        loadProducts();
    }

    private void loadProducts() {
        // This is a dummy list—replace it with real network/database calls later!
        List<Product> dummyProducts = new ArrayList<>();

        // Product creation uses the official Product model
        dummyProducts.add(new Product(1, "Classic Pomade", "A firm hold, classic scent.", 19.99, R.drawable.bearded_oil));
        dummyProducts.add(new Product(2, "Beard Oil (Argan)", "Nourishes and softens the beard.", 14.50, R.drawable.hair_pomade));
        dummyProducts.add(new Product(3, "Hair Wax Matte", "Flexible hold with a matte finish.", 17.00, R.drawable.sea_salt_spray));
        dummyProducts.add(new Product(4, "Shampoo & Conditioner", "Two-in-one cleansing and conditioning.", 24.99, R.drawable.texturing_powder));
        dummyProducts.add(new Product(5, "Straight Razor Kit", "For a professional, close shave.", 35.00, R.drawable.bearded_oil));
        dummyProducts.add(new Product(6, "Styling Comb Set", "A set of three high-quality combs.", 12.99, R.drawable.hair_pomade));

        _products.setValue(dummyProducts);
    }
}