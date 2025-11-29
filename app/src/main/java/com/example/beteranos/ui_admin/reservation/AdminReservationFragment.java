package com.example.beteranos.ui_admin.reservation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.SharedImageCache;

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

    private final SimpleDateFormat dialogDateFormat = new SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(SharedAdminAppointmentViewModel.class);
        binding = FragmentAdminReservationBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupCalendarListener();
        observeViewModel();

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
     * Handles the item click, showing a dialog with full details including Home Service Address.
     */
    @Override
    public void onItemClicked(Appointment appointment) {
        // 1. Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_appointment_details, null);

        // 2. Find all the views (Standard info)
        TextView tvCustomer = dialogView.findViewById(R.id.tv_detail_customer);
        TextView tvStatus = dialogView.findViewById(R.id.tv_detail_status);
        TextView tvDateTime = dialogView.findViewById(R.id.tv_detail_datetime);
        TextView tvBarber = dialogView.findViewById(R.id.tv_detail_barber);
        TextView tvServices = dialogView.findViewById(R.id.tv_detail_services);

        // 3. Find views for Receipt
        ImageView ivReceipt = dialogView.findViewById(R.id.iv_payment_receipt);
        ProgressBar pbImageLoading = dialogView.findViewById(R.id.pb_image_loading);
        TextView tvNoReceipt = dialogView.findViewById(R.id.tv_no_receipt);

        // 4. Find views for Location & Address (New)
        TextView tvLocation = dialogView.findViewById(R.id.tv_detail_location);
        TextView tvAddress = dialogView.findViewById(R.id.tv_detail_address);

        // 5. Set Standard Data
        tvCustomer.setText(appointment.getCustomerName());
        tvStatus.setText("Status: " + appointment.getStatus());
        tvDateTime.setText(appointment.getReservationTime() != null ?
                dialogDateFormat.format(appointment.getReservationTime()) : "N/A");
        tvBarber.setText("Barber: " + appointment.getBarberName());
        tvServices.setText("Services: " + appointment.getServiceName());

        // --- ⭐️ LOCATION LOGIC START ⭐️ ---
        String location = appointment.getServiceLocation();
        if (location == null || location.isEmpty()) {
            location = "Barbershop"; // Default
        }

        // Just set the location name (e.g. "Home Service" or "Barbershop")
        // The icon in the XML handles the "Location" context.
        tvLocation.setText(location);

        if ("Home Service".equalsIgnoreCase(location)) {
            String addr = appointment.getHomeAddress();
            if (addr != null && !addr.isEmpty()) {
                tvAddress.setText(addr);
            } else {
                tvAddress.setText("No address provided");
            }
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            tvAddress.setVisibility(View.GONE);
        }
        // --- ⭐️ LOCATION LOGIC END ⭐️ ---

        // 6. Handle the payment receipt image
        final byte[] receiptBytes = appointment.getPaymentReceiptBytes();

        if (receiptBytes != null && receiptBytes.length > 0) {
            pbImageLoading.setVisibility(View.GONE);
            tvNoReceipt.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.VISIBLE);

            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(receiptBytes, 0, receiptBytes.length);
                if (bitmap != null) {
                    ivReceipt.setImageBitmap(bitmap);
                } else {
                    ivReceipt.setVisibility(View.GONE);
                    tvNoReceipt.setText("Failed to decode receipt image.");
                    tvNoReceipt.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                ivReceipt.setVisibility(View.GONE);
                tvNoReceipt.setText("Error loading receipt data.");
                tvNoReceipt.setVisibility(View.VISIBLE);
            }
        } else {
            pbImageLoading.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.GONE);
            tvNoReceipt.setVisibility(View.VISIBLE);
            tvNoReceipt.setText("No payment receipt uploaded.");
        }

        ivReceipt.setOnClickListener(v -> {
            if (receiptBytes != null && receiptBytes.length > 0) {
                SharedImageCache.putReceiptBytes(appointment.getReservationId(), receiptBytes);
                Intent intent = new Intent(getContext(), FullImageActivity.class);
                intent.putExtra(FullImageActivity.EXTRA_RECEIPT_KEY, appointment.getReservationId());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No receipt available.", Toast.LENGTH_SHORT).show();
            }
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Booking Details")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}