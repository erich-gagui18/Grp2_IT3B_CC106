package com.example.beteranos.ui_admin.reservation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory; // Import for byte[] to Bitmap conversion
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Added for potential user feedback

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.utils.FullImageActivity; // ADD THIS IMPORT
import com.example.beteranos.utils.SharedImageCache; // ADD THIS IMPORT

// NOTE: Glide imports are commented out/removed as they are no longer needed for local BLOB data
// import com.bumptech.glide.Glide;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminReservationBinding;
import com.example.beteranos.models.Appointment;
import com.example.beteranos.ui_admin.AdminAppointmentAdapter;
import com.example.beteranos.ui_admin.SharedAdminAppointmentViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminReservationFragment extends Fragment implements AdminAppointmentAdapter.OnAppointmentActionListener {

    private FragmentAdminReservationBinding binding;
    private SharedAdminAppointmentViewModel viewModel;
    private AdminAppointmentAdapter adapter;

    // --- A DATE FORMATTER for the dialog ---
    private final SimpleDateFormat dialogDateFormat = new SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(SharedAdminAppointmentViewModel.class);
        binding = FragmentAdminReservationBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupCalendarListener();
        observeViewModel();

        // Fetch for today initially
        long today = Calendar.getInstance().getTimeInMillis();
        viewModel.fetchAppointmentsForDate(today);

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminAppointmentAdapter(this);
        binding.rvAdminAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminAppointments.setAdapter(adapter);
    }

    private void setupCalendarListener() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            long selectedDateInMillis = calendar.getTimeInMillis();
            viewModel.fetchAppointmentsForDate(selectedDateInMillis);
        });
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.appointments.observe(getViewLifecycleOwner(), appointments -> {
            adapter.submitList(appointments);
            if (appointments == null || appointments.isEmpty()) {
                binding.tvNoAppointments.setVisibility(View.VISIBLE);
                binding.rvAdminAppointments.setVisibility(View.GONE);
            } else {
                binding.tvNoAppointments.setVisibility(View.GONE);
                binding.rvAdminAppointments.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onConfirmClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Appointment")
                .setMessage("Are you sure you want to confirm this appointment?")
                .setPositiveButton("Yes, Confirm", (dialog, which) -> {
                    viewModel.updateAppointmentStatus(appointment.getReservationId(), "Confirmed");
                    Toast.makeText(getContext(), "Appointment #" + appointment.getReservationId() + " Confirmed.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onCancelClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    viewModel.updateAppointmentStatus(appointment.getReservationId(), "Cancelled");
                    Toast.makeText(getContext(), "Appointment #" + appointment.getReservationId() + " Cancelled.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onMarkAsCompletedClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Complete Appointment")
                .setMessage("Mark this appointment as completed?")
                .setPositiveButton("Yes, Mark as Completed", (dialog, which) -> {
                    viewModel.updateAppointmentStatus(appointment.getReservationId(), "Completed");
                    Toast.makeText(getContext(), "Appointment #" + appointment.getReservationId() + " Completed.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }


    /**
     * Handles the item click, showing a dialog with full appointment details including the payment receipt image.
     */
    @Override
    public void onItemClicked(Appointment appointment) {
        // 1. Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_appointment_details, null);

        // 2. Find all the views in the dialog
        TextView tvCustomer = dialogView.findViewById(R.id.tv_detail_customer);
        TextView tvStatus = dialogView.findViewById(R.id.tv_detail_status);
        TextView tvDateTime = dialogView.findViewById(R.id.tv_detail_datetime);
        TextView tvBarber = dialogView.findViewById(R.id.tv_detail_barber);
        TextView tvServices = dialogView.findViewById(R.id.tv_detail_services);
        ImageView ivReceipt = dialogView.findViewById(R.id.iv_payment_receipt);
        ProgressBar pbImageLoading = dialogView.findViewById(R.id.pb_image_loading);
        TextView tvNoReceipt = dialogView.findViewById(R.id.tv_no_receipt);
        // TextView tvReceiptLabel = dialogView.findViewById(R.id.tv_receipt_label);

        // 3. Set the text data
        tvCustomer.setText(appointment.getCustomerName());
        tvStatus.setText(appointment.getStatus());
        tvDateTime.setText(appointment.getReservationTime() != null ? dialogDateFormat.format(appointment.getReservationTime()) : "N/A");
        tvBarber.setText("Barber: " + appointment.getBarberName());
        tvServices.setText("Services: " + appointment.getServiceName());

        // 4. Handle the payment receipt image (BLOB/byte[] handling)
        final byte[] receiptBytes = appointment.getPaymentReceiptBytes();

        if (receiptBytes != null && receiptBytes.length > 0) {
            // Receipt exists
            pbImageLoading.setVisibility(View.GONE);
            tvNoReceipt.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.VISIBLE);

            // CORE LOGIC: Convert byte[] to Bitmap and set it to the ImageView
            try {
                // Decode the byte array into a Bitmap object
                Bitmap bitmap = BitmapFactory.decodeByteArray(receiptBytes, 0, receiptBytes.length);

                if (bitmap != null) {
                    ivReceipt.setImageBitmap(bitmap);
                } else {
                    // Handle decoding failure (e.g., if the bytes were corrupted or not a valid image format)
                    ivReceipt.setVisibility(View.GONE);
                    tvNoReceipt.setText("Failed to decode receipt image from data.");
                    tvNoReceipt.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                // Catch any unexpected array/decoding error
                ivReceipt.setVisibility(View.GONE);
                tvNoReceipt.setText("Error loading receipt data: " + e.getMessage());
                tvNoReceipt.setVisibility(View.VISIBLE);
            }

        } else {
            // No receipt was uploaded
            pbImageLoading.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.GONE);
            tvNoReceipt.setVisibility(View.VISIBLE);
            tvNoReceipt.setText("No payment receipt uploaded.");
        }

        // 🔑 IMPLEMENT THE FUNCTIONAL CLICK LISTENER HERE
        ivReceipt.setOnClickListener(v -> {
            if (receiptBytes != null && receiptBytes.length > 0) {
                // 1. Store the byte array in the temporary cache, keyed by the Reservation ID
                SharedImageCache.putReceiptBytes(appointment.getReservationId(), receiptBytes);

                // 2. Launch the FullImageActivity, passing only the small key (ID)
                Intent intent = new Intent(getContext(), FullImageActivity.class);
                intent.putExtra(FullImageActivity.EXTRA_RECEIPT_KEY, appointment.getReservationId());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No receipt available.", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Create and show the AlertDialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Booking Details")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    // --- END OF onItemClicked METHOD ---


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}