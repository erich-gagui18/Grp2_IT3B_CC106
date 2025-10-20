package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentPaymentBinding;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationConfirmationFragment;
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
                        binding.receiptImageView.setImageURI(imageUri);
                        try {
                            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                            byte[] imageBytes = getBytes(inputStream);
                            sharedViewModel.paymentReceiptImage.setValue(imageBytes);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        binding.btnSelectImage.setOnClickListener(v -> openImageChooser());

        binding.btnBookNow.setOnClickListener(v -> {
            if (sharedViewModel.paymentReceiptImage.getValue() == null) {
                Toast.makeText(getContext(), "Please upload a payment receipt", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.loadingOverlay.setVisibility(View.VISIBLE);
            binding.btnBookNow.setVisibility(View.GONE);
            sharedViewModel.saveReservation();
        });

        observeReservationStatus();

        return binding.getRoot();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // Helper method to convert InputStream to byte array
    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
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
                binding.loadingOverlay.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(getContext(), "Booking Successful!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.child_fragment_container, new ReservationConfirmationFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Booking Failed. Please try again.", Toast.LENGTH_LONG).show();
                    binding.btnBookNow.setVisibility(View.VISIBLE);
                }
                sharedViewModel.reservationStatus.setValue(null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}