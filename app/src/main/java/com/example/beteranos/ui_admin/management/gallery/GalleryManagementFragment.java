package com.example.beteranos.ui_admin.management.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.beteranos.databinding.FragmentGalleryManagementBinding;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class GalleryManagementFragment extends Fragment {

    private FragmentGalleryManagementBinding binding;
    private GalleryManagementViewModel viewModel;
    private GalleryAdapter adapter;

    // Image Picker Launcher
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    byte[] imageBytes = uriToBytes(uri);
                    if (imageBytes != null) {
                        viewModel.uploadImage(imageBytes);
                    } else {
                        Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryManagementBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(GalleryManagementViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        observeViewModel();

        binding.fabAddImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Fetch images on load
        viewModel.fetchImages();
    }

    private void setupRecyclerView() {
        adapter = new GalleryAdapter(image -> {
            // Delete confirmation dialog
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> viewModel.deleteImage(image))
                    .setNegativeButton("No", null)
                    .show();
        });

        // Use Grid Layout (2 columns)
        binding.rvGallery.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvGallery.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.gallerys.observe(getViewLifecycleOwner(), images -> {
            adapter.submitList(images);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearToastMessage();
            }
        });
    }

    // Utility to convert Uri to byte[]
    private byte[] uriToBytes(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {

            if (inputStream == null) return null;

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}