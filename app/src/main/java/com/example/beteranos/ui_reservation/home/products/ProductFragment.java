package com.example.beteranos.ui_reservation.home.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beteranos.databinding.FragmentProductBinding;
import java.util.ArrayList;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private ProductViewModel productsViewModel;
    private ProductAdapter productAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        productsViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView with Grid Layout (2 columns)
        RecyclerView recyclerView = binding.productRecyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productAdapter = new ProductAdapter(new ArrayList<>());
        recyclerView.setAdapter(productAdapter);

        // Observe ViewModel
        productsViewModel.getProducts().observe(getViewLifecycleOwner(), productsList -> {
            if (productsList != null) {
                productAdapter.setProducts(productsList);
            }
        });

        // Trigger the database fetch
        productsViewModel.fetchProducts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}