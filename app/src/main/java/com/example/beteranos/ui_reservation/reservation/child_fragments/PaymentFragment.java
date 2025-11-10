package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.beteranos.databinding.FragmentPaymentBinding;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale; // ⭐️ ADDED IMPORT ⭐️

public class PaymentFragment extends Fragment {

    private FragmentPaymentBinding binding;
    private SharedReservationViewModel sharedViewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isGuest = false;

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

                            // 1. Convert Uri to Bitmap
                            Bitmap receiptBitmap = uriToBitmap(imageUri);

                            if (receiptBitmap != null) {
                                // 2. Display the Bitmap preview
                                binding.receiptImageView.setImageBitmap(receiptBitmap);

                                // 3. Convert the Bitmap to compressed byte[] for saving
                                // Using 70% quality for good compression of a receipt
                                byte[] compressedBytes = getBytesFromBitmap(receiptBitmap, 70);
                                sharedViewModel.paymentReceiptImage.setValue(compressedBytes);

                                // Show confirmation message
                                Toast.makeText(getContext(), "Receipt uploaded successfully.", Toast.LENGTH_SHORT).show();
                            } else {
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
        if (binding.guestCodeLayout != null) {
            binding.guestCodeLayout.setVisibility(isGuest ? View.VISIBLE : View.GONE);
        } else {
            Log.w("PaymentFragment", "guestCodeLayout not found in binding. Cannot show/hide.");
        }

        // ⭐️ NEW: Display the required payment amount (down payment) ⭐️
        displayPaymentAmount();

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
                    return;
                } else {
                    binding.guestCodeLayout.setError(null); // Clear error
                }

                showLoadingState(true);
                sharedViewModel.validateGuestCode(guestCode);

            } else {
                // --- LOGGED-IN USER FLOW: Proceed directly ---
                proceedWithBooking(customerId);
            }
        });

        setupObservers();

        // ⭐️ NEW: Observe final price to update down payment display ⭐️
        sharedViewModel.finalPrice.observe(getViewLifecycleOwner(), price -> displayPaymentAmount());
    }

    // ⭐️ NEW METHOD: Calculate and display the down payment ⭐️
    private void displayPaymentAmount() {
        Double finalPrice = sharedViewModel.finalPrice.getValue();
        double downPayment = 0.0;

        if (finalPrice != null && finalPrice > 0.0) {
            // Calculate half of the final service amount
            downPayment = finalPrice / 2.0;
        }

        // 1. Format the down payment for display
        String paymentText = String.format(Locale.US, "₱%.2f", downPayment);

        // 2. Set the text in the corresponding TextView (assumes the XML has tvPaymentAmount)
        if (binding.tvPaymentAmount != null) {
            binding.tvPaymentAmount.setText(paymentText);
        } else {
            Log.w("PaymentFragment", "tvPaymentAmount not found in binding. XML layout update required.");
        }

        // 3. Store the down payment in the ViewModel for reservation saving
        sharedViewModel.downPaymentAmount.setValue(downPayment);

        // Display the total amount for user context (assumes tvTotalServiceAmount)
        String totalText = String.format(Locale.US, "₱%.2f", (finalPrice != null ? finalPrice : 0.0));
        if (binding.tvTotalServiceAmount != null) {
            binding.tvTotalServiceAmount.setText(totalText);
        } else {
            Log.w("PaymentFragment", "tvTotalServiceAmount not found in binding. XML layout update required.");
        }
    }


    // --- Setup Observers Method ---
    private void setupObservers() {
        // ... (Existing setupObservers code remains the same) ...
        // Observer for Guest Code Validation Result
        sharedViewModel.isGuestCodeValid.observe(getViewLifecycleOwner(), isValid -> {
            if (isValid != null) {
                if (binding == null) return;

                if (isValid) {
                    proceedWithBooking(-1);
                } else {
                    showLoadingState(false);
                    if (binding.guestCodeLayout != null) {
                        binding.guestCodeLayout.setError("Invalid or used Guest Code");
                    } else {
                        Toast.makeText(getContext(), "Invalid or used Guest Code", Toast.LENGTH_LONG).show();
                    }
                }
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
                showLoadingState(false);

                if (success) {
                    Toast.makeText(getContext(), "Booking Successful!", Toast.LENGTH_SHORT).show();
                    // Navigate to Confirmation
                    if (getParentFragment() instanceof ReservationFragment) {
                        // ⭐️ The ReservationFragment's logic is responsible for initiating the database save here.
                        ((ReservationFragment) getParentFragment()).navigateToConfirmation();
                    } else {
                        Log.e("PaymentFragment", "Parent fragment is not ReservationFragment, cannot navigate.");
                    }
                } else {
                    Toast.makeText(getContext(), "Booking Failed. Please try again.", Toast.LENGTH_LONG).show();
                }
                sharedViewModel.reservationStatus.setValue(null);
            }
        });
    }

    // --- Helper method to handle booking initiation ---
    private void proceedWithBooking(int customerIdForSave) {
        if (binding == null) return;
        Log.d("PaymentFragment", "Proceeding with booking. Customer ID for save: " + customerIdForSave);
        showLoadingState(true);
        // ⭐️ The ViewModel will now save the reservation, including the downPaymentAmount
        // stored just above. ⭐️
        sharedViewModel.saveReservation(customerIdForSave);
    }

    // --- Helper method to manage loading UI state ---
    private void showLoadingState(boolean isLoading) {
        if (binding == null) return;
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnBookNow.setEnabled(!isLoading);
        binding.btnBookNow.setVisibility(isLoading ? View.GONE : View.VISIBLE);

        if (binding.guestCodeLayout != null) {
            binding.guestCodeLayout.setEnabled(!isLoading);
        }
        if (binding.btnSelectImage != null){
            binding.btnSelectImage.setEnabled(!isLoading);
        }
    }


    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // --------------------------------------------------------------------
    // ⭐️ IMAGE HELPER METHODS (uriToBitmap, getBytesFromBitmap) ⭐️
    // --------------------------------------------------------------------

    // ... (uriToBitmap and getBytesFromBitmap remain the same) ...
    private Bitmap uriToBitmap(Uri imageUri) {
        if (imageUri == null || getContext() == null) {
            return null;
        }

        ContentResolver contentResolver = getContext().getContentResolver();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, imageUri);
                return ImageDecoder.decodeBitmap(source);
            } else {
                //noinspection deprecation
                return MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
            }
        } catch (IOException e) {
            Log.e("PaymentFragment", "Failed to load bitmap from URI", e);
            return null;
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        return outputStream.toByteArray();
    }

    // --------------------------------------------------------------------

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}