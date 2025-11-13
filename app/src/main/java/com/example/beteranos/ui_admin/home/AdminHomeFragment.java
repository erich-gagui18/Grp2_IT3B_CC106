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
import android.widget.EditText; // ⭐️ ADDED IMPORT
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
// ⭐️ IMPORT YOUR VIEWMODEL (Assuming it's in ui_admin package)
import com.example.beteranos.ui_admin.SharedAdminAppointmentViewModel;
import com.example.beteranos.utils.FullImageActivity;
import com.example.beteranos.utils.SharedImageCache;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AdminHomeFragment extends Fragment implements AdminAppointmentAdapter.OnAppointmentActionListener {

    private FragmentAdminHomeBinding binding;
    // ⭐️ CHANGED: Use the correct ViewModel
    private SharedAdminAppointmentViewModel adminViewModel;
    private AdminAppointmentAdapter pendingAdapter;

    private final SimpleDateFormat dialogDateFormat = new SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        // ⭐️ CHANGED: Use the correct ViewModel
        adminViewModel = new ViewModelProvider(this).get(SharedAdminAppointmentViewModel.class);

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
        setupClickListeners(view); // <-- This method is updated
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().moveTaskToBack(true);
            }
        });
    }

    private void loadDataIfNeeded() {
        // ⭐️ CHANGED: Call the correct ViewModel method
        adminViewModel.fetchAppointmentsForDate(System.currentTimeMillis()); // Fetch for today
        adminViewModel.fetchHomeDashboardDataIfNeeded();
    }

    private void setupRecyclerView() {
        pendingAdapter = new AdminAppointmentAdapter(this);
        binding.rvPendingAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPendingAppointments.setAdapter(pendingAdapter);
    }

    // --- THIS METHOD IS UPDATED ---
    // --- ⭐ THIS METHOD IS UPDATED ---
    private void setupClickListeners(View view) {
        NavController navController = Navigation.findNavController(view);

        binding.btnActionSchedule.setOnClickListener(v ->
                navController.navigate(R.id.admin_nav_calendar));

        binding.btnActionAnalytics.setOnClickListener(v ->
                navController.navigate(R.id.admin_nav_dashboard));

        // --- ⭐️ THIS IS THE FIX ⭐️ ---
        // The listener is now attached to the CARD ID from your new XML.
        binding.cardStatRevenue.setOnClickListener(v -> {
            try {
                // --- ⭐️ IMPORTANT ⭐️ ---
                // This is the correct ACTION ID. You must create this
                // in your admin_navigation.xml file.
                navController.navigate(R.id.transactionReportFragment);
            } catch (Exception e) {
                // This catch block prevents a crash if the action ID is wrong
                Log.e("AdminHomeFragment", "Navigation failed. Check nav graph for action 'action_admin_nav_home_to_transactionReportFragment'", e);
                Toast.makeText(getContext(), "Error: Could not open report.", Toast.LENGTH_SHORT).show();
            }
        });
        // --- END OF FIX ---
    }

    private void observeViewModel() {
        // ⭐️ CHANGED: Observe the 'appointments' LiveData from the correct ViewModel
        adminViewModel.appointments.observe(getViewLifecycleOwner(), appointments -> {
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
            // ⭐️ CHANGED: Use the correct binding IDs for your stats loading
            binding.statsLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.statsContainer.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        // ⭐️ REMOVED stats and pendingAppointments observers, as they are now one 'appointments' observer
    }


    // --- ⭐️ INTERFACE METHODS ARE NOW CORRECTED ⭐️ ---
    // --- (All your interface methods for onConfirmClicked, onCancelClicked, etc. are correct and unchanged) ---

    @Override
    public void onConfirmClicked(Appointment appointment) {
        // ⭐️ FIXED: Show the payment dialog instead of just updating status
        showConfirmPaymentDialog(appointment);
    }

    @Override
    public void onCancelClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    // This correctly calls the ViewModel, which triggers the notification
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
                    // This correctly calls the ViewModel, which triggers the notification
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Completed");
                    Toast.makeText(getContext(), "Appointment marked as completed.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // --- ⭐️ ADDED: Helper method to show the payment dialog ⭐️ ---
    private void showConfirmPaymentDialog(Appointment appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Appointment");
        builder.setMessage("Enter down payment amount for " + appointment.getCustomerName());

        // Use the custom layout for the EditText
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_payment, null);
        final EditText input = dialogView.findViewById(R.id.et_down_payment);
        builder.setView(dialogView);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String amountString = input.getText().toString();
            if (amountString.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double amount = Double.parseDouble(amountString);
                // ⭐️ CALLS THE CORRECT VIEWMODEL METHOD (This triggers the "Confirmed" notification)
                adminViewModel.confirmAppointmentWithDownPayment(appointment.getReservationId(), amount);
                Toast.makeText(getContext(), "Appointment confirmed.", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    // --- (Your existing onItemClicked logic is correct) ---
    @Override
    public void onItemClicked(Appointment appointment) {
        // ... (this method is unchanged and correct) ...
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
                    tvNoReceipt.setText("Failed to decode receipt image from data.");
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