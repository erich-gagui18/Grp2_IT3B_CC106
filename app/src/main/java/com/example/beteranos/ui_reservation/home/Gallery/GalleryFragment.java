package com.example.beteranos.ui_reservation.home.Gallery;

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

import com.example.beteranos.databinding.FragmentGalleryBinding;
import com.example.beteranos.models.Gallery;
import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.GridSpacingItemDecoration;
import com.example.beteranos.utils.SharedImageCache;

import com.example.beteranos.R; // ⭐️ ADD THIS LINE

import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private GalleryViewModel viewModel;
    private GalleryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ⭐️ 1. Handle Custom Back Button ⭐️
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        setupRecyclerView();
        observeViewModel();

        viewModel.fetchGallerys();
    }

    // ⭐️ 2. Hide System Arrow & Title when entering ⭐️
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false); // Hide system back arrow
                actionBar.setTitle(""); // Hide system title (so your XML title shows alone)
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
                actionBar.setDisplayHomeAsUpEnabled(true); // Restore arrow for other screens
                actionBar.setTitle("Beteranos"); // Optional: Reset to app name
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new GalleryAdapter(new ArrayList<>(), this::onImageClick);
        binding.galleryRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // ⭐️ Add Spacing (16dp spacing, 2 columns)
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing); // Or hardcode 32
        binding.galleryRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, true));

        binding.galleryRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.images.observe(getViewLifecycleOwner(), images -> {
            if (images != null) {
                adapter.updateData(images);
                boolean isEmpty = images.isEmpty();
                binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.galleryRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        if (viewModel.isLoading != null) {
            viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void onImageClick(Gallery item) {
        if (item.getImageData() != null) {
            SharedImageCache.putReceiptBytes(item.getId(), item.getImageData());
            Intent intent = new Intent(requireContext(), FullImageActivity.class);
            intent.putExtra(FullImageActivity.EXTRA_RECEIPT_KEY, item.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}