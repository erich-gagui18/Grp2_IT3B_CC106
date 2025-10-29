package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Import Nullable
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentPaymentBinding;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationConfirmationFragment; // Correct import if it's a parent fragment
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment; // Import parent ReservationFragment

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PaymentFragment extends Fragment {

    private FragmentPaymentBinding binding;
    private SharedReservationViewModel sharedViewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null && binding != null) { // Add null check for binding
                            binding.receiptImageView.setImageURI(imageUri);
                            try {
                                InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                                if (inputStream != null) {
                                    byte[] imageBytes = getBytes(inputStream);
                                    sharedViewModel.paymentReceiptImage.setValue(imageBytes);
                                    inputStream.close(); // Close the stream
                                } else {
                                    Toast.makeText(getContext(), "Failed to open image stream", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e("PaymentFragment", "Error processing image", e);
                                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSelectImage.setOnClickListener(v -> openImageChooser());

        binding.btnBookNow.setOnClickListener(v -> {
            if (sharedViewModel.paymentReceiptImage.getValue() == null) {
                Toast.makeText(getContext(), "Please upload a payment receipt", Toast.LENGTH_SHORT).show();
                return;
            }
            // Show loading state
            binding.loadingOverlay.setVisibility(View.VISIBLE);
            binding.btnBookNow.setVisibility(View.GONE); // Hide button during processing

            // --- THIS IS THE FIX ---
            // Get the customer ID from the hosting Activity's Intent
            // Default to -1 if not found (indicating a guest user)
            int customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);

            // Call saveReservation WITH the customerId parameter
            sharedViewModel.saveReservation(customerId);
        });

        observeReservationStatus(); // Set up the observer for the result
    }


    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // Helper method to convert InputStream to byte array
    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024 * 4; // Increased buffer size slightly
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void observeReservationStatus() {
        sharedViewModel.reservationStatus.observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                // Important: Check if binding is still valid before accessing UI
                if (binding == null) {
                    Log.w("PaymentFragment", "Binding is null in observer, cannot update UI.");
                    return;
                }

                binding.loadingOverlay.setVisibility(View.GONE); // Hide loading overlay

                if (success) {
                    Toast.makeText(getContext(), "Booking Successful!", Toast.LENGTH_SHORT).show();
                    // Navigate to Confirmation using the parent fragment's method
                    if (getParentFragment() instanceof ReservationFragment) {
                        ((ReservationFragment) getParentFragment()).navigateToConfirmation();
                    } else {
                        Log.e("PaymentFragment", "Parent fragment is not ReservationFragment, cannot navigate.");
                        // Fallback or error handling
                    }
                } else {
                    Toast.makeText(getContext(), "Booking Failed. Please try again.", Toast.LENGTH_LONG).show();
                    binding.btnBookNow.setVisibility(View.VISIBLE); // Show button again on failure
                }
                // Reset the status in the ViewModel to prevent re-triggering
                sharedViewModel.reservationStatus.setValue(null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Nullify the binding
    }
}