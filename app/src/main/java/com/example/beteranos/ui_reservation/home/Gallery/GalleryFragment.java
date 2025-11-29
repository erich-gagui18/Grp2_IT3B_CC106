package com.example.beteranos.ui_reservation.home.Gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import this
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.beteranos.databinding.FragmentGalleryBinding;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private GalleryViewModel viewModel; // Reference the ViewModel
    private GalleryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        observeViewModel();

        // Trigger data fetch
        viewModel.fetchGallerys();
    }

    private void setupRecyclerView() {
        // Initialize adapter with empty list first
        adapter = new GalleryAdapter(new ArrayList<>());

        binding.galleryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.galleryRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Watch for data changes from the database
        viewModel.images.observe(getViewLifecycleOwner(), images -> {
            if (images != null) {
                adapter.updateData(images);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}