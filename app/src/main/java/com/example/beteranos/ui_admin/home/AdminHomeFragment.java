package com.example.beteranos.ui_admin.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap; // 🔑 NEW IMPORT
import android.graphics.BitmapFactory; // 🔑 NEW IMPORT
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Recommended for user feedback

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

// Glide Imports removed/commented out as they are no longer used for local BLOB data
// import com.bumptech.glide.Glide;
// import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminHomeBinding;
import com.example.beteranos.models.Appointment;
import com.example.beteranos.ui_admin.AdminAppointmentAdapter;
import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.SharedImageCache;

import java.text.SimpleDateFormat;
import java.util.Locale;

// Implement the adapter's interface
public class AdminHomeFragment extends Fragment implements AdminAppointmentAdapter.OnAppointmentActionListener {

    private FragmentAdminHomeBinding binding;
    private AdminHomeViewModel adminViewModel;
    private AdminAppointmentAdapter pendingAdapter;

    private final SimpleDateFormat dialogDateFormat = new SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        adminViewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);

        setupRecyclerView();
        observeViewModel();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
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
        // The ViewModel will now check if data has already been loaded
        // in its constructor or in this method call.
        adminViewModel.fetchHomeDashboardDataIfNeeded();
    }

    private void setupRecyclerView() {
        pendingAdapter = new AdminAppointmentAdapter(this);
        binding.rvPendingAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPendingAppointments.setAdapter(pendingAdapter);
    }

    private void setupClickListeners(View view) {
        NavController navController = Navigation.findNavController(view);
        binding.btnActionSchedule.setOnClickListener(v ->
                navController.navigate(R.id.admin_nav_calendar));
        binding.btnActionAnalytics.setOnClickListener(v ->
                navController.navigate(R.id.admin_nav_dashboard));
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


    // --- Interface methods for the "Pending" list adapter (Status updates are unchanged) ---

    @Override
    public void onConfirmClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Appointment")
                .setMessage("Are you sure you want to confirm this appointment?")
                .setPositiveButton("Yes, Confirm", (dialog, which) -> {
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Confirmed");
                    Toast.makeText(getContext(), "Appointment confirmed.", Toast.LENGTH_SHORT).show();
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
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Cancelled");
                    Toast.makeText(getContext(), "Appointment cancelled.", Toast.LENGTH_SHORT).show();
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
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Completed");
                    Toast.makeText(getContext(), "Appointment marked as completed.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // --- 🔑 CRITICAL FIX: BLOB/byte[] image display logic ---
    @Override
    public void onItemClicked(Appointment appointment) {
        // This is the same logic used in AdminReservationFragment
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_appointment_details, null);

        TextView tvCustomer = dialogView.findViewById(R.id.tv_detail_customer);
        TextView tvStatus = dialogView.findViewById(R.id.tv_detail_status);
        TextView tvDateTime = dialogView.findViewById(R.id.tv_detail_datetime);
        TextView tvBarber = dialogView.findViewById(R.id.tv_detail_barber);
        TextView tvServices = dialogView.findViewById(R.id.tv_detail_services);
        ImageView ivReceipt = dialogView.findViewById(R.id.iv_payment_receipt);
        ProgressBar pbImageLoading = dialogView.findViewById(R.id.pb_image_loading);
        TextView tvNoReceipt = dialogView.findViewById(R.id.tv_no_receipt);

        tvCustomer.setText(appointment.getCustomerName());
        tvStatus.setText("Status: " + appointment.getStatus());
        tvDateTime.setText(appointment.getReservationTime() != null ? dialogDateFormat.format(appointment.getReservationTime()) : "N/A");
        tvBarber.setText("Barber: " + appointment.getBarberName());
        tvServices.setText("Services: " + appointment.getServiceName());

        final byte[] receiptBytes = appointment.getPaymentReceiptBytes(); // 🔑 Retrieve the byte array

        if (receiptBytes != null && receiptBytes.length > 0) {
            // Receipt exists
            pbImageLoading.setVisibility(View.GONE); // Local data, no network loading time
            tvNoReceipt.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.VISIBLE);

            // Decode the byte array into a Bitmap
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(receiptBytes, 0, receiptBytes.length);
                if (bitmap != null) {
                    ivReceipt.setImageBitmap(bitmap);
                } else {
                    ivReceipt.setVisibility(View.GONE);
                    tvNoReceipt.setText("Failed to decode receipt image from data.");
                    tvNoReceipt.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                ivReceipt.setVisibility(View.GONE);
                tvNoReceipt.setText("Error loading receipt data.");
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

        new AlertDialog.Builder(requireContext())
                .setTitle("Booking Details")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    // --- END OF FIX ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}