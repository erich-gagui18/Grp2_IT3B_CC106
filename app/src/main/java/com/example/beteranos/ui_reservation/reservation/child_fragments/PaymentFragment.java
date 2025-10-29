package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentPaymentBinding; // Ensure correct binding class
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PaymentFragment extends Fragment {

    private FragmentPaymentBinding binding;
    private SharedReservationViewModel sharedViewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isGuest = false; // Flag to track guest status

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ActivityResultLauncher for image picking
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null && binding != null) {
                            binding.receiptImageView.setImageURI(imageUri);
                            try {
                                InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                                if (inputStream != null) {
                                    byte[] imageBytes = getBytes(inputStream);
                                    sharedViewModel.paymentReceiptImage.setValue(imageBytes);
                                    inputStream.close();
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

        // Determine guest status
        int customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);
        isGuest = (customerId == -1);

        // Show guest code input field only for guests
        // Assumes you have guestCodeLayout in your fragment_payment.xml
        if (binding.guestCodeLayout != null) {
            binding.guestCodeLayout.setVisibility(isGuest ? View.VISIBLE : View.GONE);
        } else {
            Log.w("PaymentFragment", "guestCodeLayout not found in binding. Cannot show/hide.");
        }


        binding.btnSelectImage.setOnClickListener(v -> openImageChooser());

        // --- UPDATED: Final Booking Button Logic ---
        binding.btnBookNow.setOnClickListener(v -> {
            // 1. Check if receipt image is uploaded
            if (sharedViewModel.paymentReceiptImage.getValue() == null) {
                Toast.makeText(getContext(), "Please upload a payment receipt", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Check guest status
            if (isGuest) {
                // --- GUEST FLOW: Validate Guest Code ---
                if (binding.guestCodeEditText == null) {
                    Log.e("PaymentFragment", "guestCodeEditText is null. Cannot proceed.");
                    Toast.makeText(getContext(), "Error: Guest code field not found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String guestCode = binding.guestCodeEditText.getText().toString().trim();
                if (guestCode.isEmpty()) {
                    binding.guestCodeLayout.setError("Guest Code is required"); // Show error on layout
                    // Toast.makeText(getContext(), "Please enter Guest Code", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    binding.guestCodeLayout.setError(null); // Clear error
                }

                // Show loading and disable button
                showLoadingState(true);

                // Call ViewModel to validate the code
                sharedViewModel.validateGuestCode(guestCode);
                // The observer (setupObservers) will handle the result

            } else {
                // --- LOGGED-IN USER FLOW: Proceed directly ---
                proceedWithBooking(customerId); // Pass the actual customer ID
            }
        });

        // Setup observers for ViewModel LiveData
        setupObservers();
    }

    // --- NEW: Setup Observers Method ---
    private void setupObservers() {
        // Observer for Guest Code Validation Result
        sharedViewModel.isGuestCodeValid.observe(getViewLifecycleOwner(), isValid -> {
            if (isValid != null) { // Check if the event is fresh
                if (binding == null) return; // Check if fragment view is still valid

                if (isValid) {
                    // Guest code is valid, proceed with booking
                    // Pass -1; saveReservation logic will find/create guest based on ViewModel details
                    proceedWithBooking(-1);
                } else {
                    // Guest code is invalid
                    showLoadingState(false); // Hide loading, enable button
                    if (binding.guestCodeLayout != null) {
                        binding.guestCodeLayout.setError("Invalid or used Guest Code");
                    } else {
                        Toast.makeText(getContext(), "Invalid or used Guest Code", Toast.LENGTH_LONG).show();
                    }
                }
                // Reset the signal in ViewModel to prevent re-triggering on config change
                sharedViewModel.isGuestCodeValid.setValue(null);
            }
        });

        // Observer for the final reservation status (after calling saveReservation)
        sharedViewModel.reservationStatus.observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (binding == null) {
                    Log.w("PaymentFragment", "Binding is null in reservationStatus observer.");
                    return;
                }
                showLoadingState(false); // Hide loading, enable button (if failed)

                if (success) {
                    Toast.makeText(getContext(), "Booking Successful!", Toast.LENGTH_SHORT).show();
                    // Navigate to Confirmation
                    if (getParentFragment() instanceof ReservationFragment) {
                        ((ReservationFragment) getParentFragment()).navigateToConfirmation();
                    } else {
                        Log.e("PaymentFragment", "Parent fragment is not ReservationFragment, cannot navigate.");
                    }
                } else {
                    Toast.makeText(getContext(), "Booking Failed. Please try again.", Toast.LENGTH_LONG).show();
                    // Button is re-enabled by showLoadingState(false)
                }
                // Reset the status signal
                sharedViewModel.reservationStatus.setValue(null);
            }
        });
    }

    // --- NEW: Helper method to handle booking initiation ---
    private void proceedWithBooking(int customerIdForSave) {
        if (binding == null) return;
        Log.d("PaymentFragment", "Proceeding with booking. Customer ID for save: " + customerIdForSave);
        showLoadingState(true); // Show loading, disable button
        // Call ViewModel's saveReservation
        sharedViewModel.saveReservation(customerIdForSave);
        // The reservationStatus observer will handle the result
    }

    // --- NEW: Helper method to manage loading UI state ---
    private void showLoadingState(boolean isLoading) {
        if (binding == null) return;
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnBookNow.setEnabled(!isLoading); // Enable/disable button
        binding.btnBookNow.setVisibility(isLoading ? View.GONE : View.VISIBLE); // Show/Hide button

        // Disable guest code input during loading
        if (binding.guestCodeLayout != null) {
            binding.guestCodeLayout.setEnabled(!isLoading);
        }
        // Disable image selection button during loading
        if (binding.btnSelectImage != null){
            binding.btnSelectImage.setEnabled(!isLoading);
        }
    }


    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        // ... (existing code) ...
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024 * 4;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Removed observeReservationStatus() - logic moved into setupObservers()

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Nullify the binding
    }
}