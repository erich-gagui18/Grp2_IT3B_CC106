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
import java.util.List;

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
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.productRecyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productAdapter = new ProductAdapter(new ArrayList<>());
        recyclerView.setAdapter(productAdapter);

        // Observe the product list from the ViewModel
        productsViewModel.getProducts().observe(getViewLifecycleOwner(), productsList -> {
            if (productsList != null) {
                // Update the adapter's data set
                productAdapter.setProducts(productsList);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}