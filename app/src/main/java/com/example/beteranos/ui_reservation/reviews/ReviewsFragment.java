package com.example.beteranos.ui_reservation.reviews;

import androidx.activity.result.ActivityResultLauncher; // ⭐️ NEW
import androidx.activity.result.contract.ActivityResultContracts; // ⭐️ NEW
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity; // ⭐️ NEW
import android.content.ContentResolver; // ⭐️ NEW
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap; // ⭐️ NEW
import android.graphics.ImageDecoder; // ⭐️ NEW
import android.net.Uri; // ⭐️ NEW
import android.os.Build; // ⭐️ NEW
import android.os.Bundle;
import android.provider.MediaStore; // ⭐️ NEW
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.beteranos.databinding.FragmentReviewsBinding;
import com.example.beteranos.models.Barber;
import com.example.beteranos.ui_customer_login.CustomerLoginActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior; // ⭐️ Added for BottomSheet handling

import java.io.ByteArrayOutputStream; // ⭐️ NEW
import java.io.IOException; // ⭐️ NEW
import java.util.List;
import java.util.stream.Collectors;

public class ReviewsFragment extends Fragment {

    private static final String TAG = "ReviewsFragment";
    private ReviewsViewModel mViewModel;
    private FragmentReviewsBinding binding;
    private ReviewsAdapter adapter;
    private List<Barber> barberList;
    private int customerId = -1;

    // ⭐️ NEW: Variables for Image Upload
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private byte[] selectedImageBytes = null; // Data to send to DB

    // ⭐️ NEW: BottomSheetBehavior Reference
    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ⭐️ NEW: Initialize the image picker
        setupImagePickerLauncher();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReviewsBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(ReviewsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get customer ID
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        this.customerId = userPrefs.getInt("customer_id", -1);

        // ⭐️ Initialize Bottom Sheet Behavior
        View bottomSheet = binding.cardWriteReview;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Logic for Guests vs Logged In
        if (isLoggedIn && customerId != -1) {
            binding.guestView.setVisibility(View.GONE);
            setupClickListeners();
        } else {
            binding.guestView.setVisibility(View.VISIBLE);
            binding.btnGoToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CustomerLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
            // Hide the write review card for guests entirely so it doesn't peek
            binding.cardWriteReview.setVisibility(View.GONE);
        }

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new ReviewsAdapter();
        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewsRecyclerView.setAdapter(adapter);
    }

    // ⭐️ NEW: Setup Image Picker Launcher
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                // Convert Uri to Bitmap
                                Bitmap bitmap = uriToBitmap(imageUri);
                                if (bitmap != null) {
                                    // Compress and store as bytes
                                    selectedImageBytes = getBytesFromBitmap(bitmap, 70); // 70% quality to save DB space

                                    // Show Preview
                                    binding.ivSelectedImagePreview.setImageBitmap(bitmap);
                                    binding.ivSelectedImagePreview.setVisibility(View.VISIBLE);
                                    binding.btnRemoveImage.setVisibility(View.VISIBLE);

                                    // Expand sheet to show the image clearly
                                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to process image", e);
                                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void setupClickListeners() {
        // ⭐️ Optional: Expand sheet when clicking header title
        binding.tvReviewTitle.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // ⭐️ NEW: Add Image Button Logic (Ensure ID matches XML: btn_add_photo)
        binding.btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // ⭐️ NEW: Remove Image Button Logic (Ensure ID matches XML: btn_remove_image)
        binding.btnRemoveImage.setOnClickListener(v -> {
            selectedImageBytes = null;
            binding.ivSelectedImagePreview.setImageDrawable(null);
            binding.ivSelectedImagePreview.setVisibility(View.GONE);
            binding.btnRemoveImage.setVisibility(View.GONE);
        });

        // Submit Logic
        binding.btnSubmitReview.setOnClickListener(v -> {
            String selectedBarberName = binding.barberSpinnerDropdown.getText().toString();
            int rating = (int) binding.ratingBarInput.getRating();
            String comment = binding.commentEditText.getText().toString().trim();

            if (selectedBarberName.isEmpty()) {
                Toast.makeText(getContext(), "Please select a barber", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rating == 0) {
                Toast.makeText(getContext(), "Please provide a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedBarberId = -1;
            if (barberList != null) {
                for (Barber barber : barberList) {
                    if (barber.getName().equals(selectedBarberName)) {
                        selectedBarberId = barber.getBarberId();
                        break;
                    }
                }
            }

            if (selectedBarberId == -1) {
                Toast.makeText(getContext(), "Invalid barber selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // ⭐️ NEW: Pass selectedImageBytes to ViewModel
            mViewModel.submitReview(this.customerId, selectedBarberId, rating, comment, selectedImageBytes);
        });
    }

    private void setupObservers() {
        mViewModel.allBarbers.observe(getViewLifecycleOwner(), barbers -> {
            if (barbers != null && !barbers.isEmpty()) {
                this.barberList = barbers;
                List<String> barberNames = barbers.stream()
                        .map(Barber::getName)
                        .collect(Collectors.toList());
                ArrayAdapter<String> barberAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        barberNames
                );
                binding.barberSpinnerDropdown.setAdapter(barberAdapter);
            }
        });

        mViewModel.reviewsList.observe(getViewLifecycleOwner(), reviews -> {
            adapter.submitList(reviews);
            if (reviews == null || reviews.isEmpty()) {
                binding.emptyListText.setVisibility(View.VISIBLE);
                binding.reviewsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyListText.setVisibility(View.GONE);
                binding.reviewsRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        mViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (binding.btnSubmitReview.getVisibility() == View.VISIBLE) {
                binding.btnSubmitReview.setEnabled(!isLoading);
                binding.btnAddPhoto.setEnabled(!isLoading); // Disable upload while loading
            }
        });

        mViewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                mViewModel.clearToastMessage();

                if (message.contains("success")) {
                    // Reset UI
                    binding.barberSpinnerDropdown.setText("", false);
                    binding.ratingBarInput.setRating(0);
                    binding.commentEditText.setText("");

                    // ⭐️ Clear Image Preview
                    selectedImageBytes = null;
                    binding.ivSelectedImagePreview.setVisibility(View.GONE);
                    binding.btnRemoveImage.setVisibility(View.GONE);

                    // ⭐️ Collapse Sheet on Success
                    if (bottomSheetBehavior != null) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            }
        });
    }

    // ⭐️ NEW: Helper Methods for Image Conversion
    private Bitmap uriToBitmap(Uri imageUri) {
        if (imageUri == null || getContext() == null) return null;
        ContentResolver resolver = getContext().getContentResolver();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, imageUri));
            } else {
                return MediaStore.Images.Media.getBitmap(resolver, imageUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load bitmap from URI", e);
            return null;
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}