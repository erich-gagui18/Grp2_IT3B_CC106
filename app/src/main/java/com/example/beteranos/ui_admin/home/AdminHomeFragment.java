package com.example.beteranos.ui_admin.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminHomeBinding;
import com.example.beteranos.models.Appointment;
import com.example.beteranos.ui_admin.AdminAppointmentAdapter;
import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.SharedImageCache;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AdminHomeFragment extends Fragment implements AdminAppointmentAdapter.OnAppointmentActionListener {

    // ... (Variables and onCreateView/onViewCreated remain exactly the same) ...
    private FragmentAdminHomeBinding binding;
    private AdminHomeViewModel adminViewModel;
    private AdminAppointmentAdapter pendingAdapter;
    private final SimpleDateFormat dialogDateFormat = new SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        adminViewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);
        setupRecyclerView();
        observeViewModel();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences prefs = requireActivity().getSharedPreferences("admin_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("ADMIN_NAME", "Admin");
        binding.textWelcome.setText(getString(R.string.welcome_message, username));
        loadDataIfNeeded();
        setupClickListeners(view);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().moveTaskToBack(true);
            }
        });
    }

    private void loadDataIfNeeded() {
        adminViewModel.fetchHomeDashboardDataIfNeeded();
    }

    private void setupRecyclerView() {
        pendingAdapter = new AdminAppointmentAdapter(this);
        binding.rvPendingAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPendingAppointments.setAdapter(pendingAdapter);
    }

    private void setupClickListeners(View view) {
        NavController navController = Navigation.findNavController(view);
        binding.btnActionSchedule.setOnClickListener(v -> navController.navigate(R.id.admin_nav_calendar));
        binding.btnActionAnalytics.setOnClickListener(v -> navController.navigate(R.id.admin_nav_dashboard));
        binding.cardStatRevenue.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.transactionReportFragment);
            } catch (Exception e) {
                Log.e("AdminHomeFragment", "Navigation failed.", e);
                Toast.makeText(getContext(), "Error: Could not open report.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        adminViewModel.stats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.tvStatBookings.setText(String.valueOf(stats.totalBookings));
                binding.tvStatPending.setText(String.valueOf(stats.pendingCount));
                binding.tvStatRevenue.setText(stats.getFormattedRevenue());
            }
        });
        adminViewModel.pendingAppointments.observe(getViewLifecycleOwner(), appointments -> {
            if (appointments != null && !appointments.isEmpty()) {
                binding.rvPendingAppointments.setVisibility(View.VISIBLE);
                binding.tvNoPending.setVisibility(View.GONE);
                pendingAdapter.submitList(appointments);
            } else {
                binding.rvPendingAppointments.setVisibility(View.GONE);
                binding.tvNoPending.setVisibility(View.VISIBLE);
            }
        });
        adminViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.statsLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.statsContainer.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });
    }

    // ... (onConfirm, onCancel, onMarkAsCompleted remain the same) ...
    @Override
    public void onConfirmClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Appointment")
                .setMessage("Are you sure you want to confirm this appointment?")
                .setPositiveButton("Yes, Confirm", (dialog, which) -> {
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Confirmed");
                    Toast.makeText(getContext(), "Appointment confirmed.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null).show();
    }

    @Override
    public void onCancelClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Cancelled");
                    Toast.makeText(getContext(), "Appointment cancelled.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null).show();
    }

    @Override
    public void onMarkAsCompletedClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Complete Appointment")
                .setMessage("Mark this appointment as completed?")
                .setPositiveButton("Yes, Mark as Completed", (dialog, which) -> {
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Completed");
                    Toast.makeText(getContext(), "Appointment marked as completed.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null).show();
    }

    // --- ⭐️ UPDATED: onItemClicked ⭐️ ---
    @Override
    public void onItemClicked(Appointment appointment) {
        // 1. Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_appointment_details, null);

        // 2. Find all views (Standard info)
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
        if (location == null || location.isEmpty()) location = "Barbershop"; // Default

        // Set the bold location title (e.g., "Home Service")
        tvLocation.setText(location);

        // Show/Hide address based on location type
        if ("Home Service".equalsIgnoreCase(location)) {
            String addr = appointment.getHomeAddress();
            if (addr != null && !addr.isEmpty()) {
                tvAddress.setText(addr);
            } else {
                tvAddress.setText("No address provided");
            }
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            // Hide address for Barbershop appointments
            tvAddress.setVisibility(View.GONE);
        }
        // --- ⭐️ LOCATION LOGIC END ⭐️ ---

        // 6. Handle Receipt Image (BLOB)
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
                tvNoReceipt.setText("Error loading receipt.");
                tvNoReceipt.setVisibility(View.VISIBLE);
            }
        } else {
            pbImageLoading.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.GONE);
            tvNoReceipt.setVisibility(View.VISIBLE);
            tvNoReceipt.setText("No payment receipt uploaded.");
        }

        // 7. Click Listener for Full Image
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

        // 8. Show Dialog
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