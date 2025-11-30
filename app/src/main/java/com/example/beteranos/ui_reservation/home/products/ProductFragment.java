package com.example.beteranos.ui_reservation.home.products;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

// ⭐️ IMPORTS FOR ACTION BAR CONTROL ⭐️
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import com.example.beteranos.databinding.FragmentProductBinding;
import com.example.beteranos.models.Product;
import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.SharedImageCache;

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
        binding = FragmentProductBinding.inflate(inflater, container, false);
        productsViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ⭐️ 1. Handle Custom Back Button ⭐️
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        // Setup RecyclerView
        productAdapter = new ProductAdapter(new ArrayList<>(), this::onProductImageClick);
        binding.productRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.productRecyclerView.setAdapter(productAdapter);

        // Observe ViewModel
        productsViewModel.getProducts().observe(getViewLifecycleOwner(), productsList -> {
            if (productsList != null) {
                productAdapter.setProducts(productsList);

                // Toggle Empty State
                boolean isEmpty = productsList.isEmpty();
                binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.productRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        // Observe Loading
        if (productsViewModel.isLoading != null) {
            productsViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            });
        }

        // Fetch Data
        productsViewModel.fetchProducts();
    }

    // ⭐️ 2. Hide System Arrow & Title when entering ⭐️
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle(""); // Clear system title
            }
        }
    }

    // ⭐️ 3. Restore System Arrow when leaving ⭐️
    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("Beteranos");
            }
        }
    }

    private void onProductImageClick(Product product) {
        if (product.getImageBytes() != null) {
            SharedImageCache.putReceiptBytes(product.getId(), product.getImageBytes());
            Intent intent = new Intent(requireContext(), FullImageActivity.class);
            intent.putExtra(FullImageActivity.EXTRA_RECEIPT_KEY, product.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}