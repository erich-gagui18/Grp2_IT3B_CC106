package com.example.beteranos.ui_admin.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.beteranos.ui_admin.AdminAppointmentAdapter; // Make sure this import is correct

// Implement the adapter's interface
public class AdminHomeFragment extends Fragment implements AdminAppointmentAdapter.OnAppointmentActionListener {

    private FragmentAdminHomeBinding binding;
    private AdminHomeViewModel adminViewModel; // Use the new ViewModel
    private AdminAppointmentAdapter pendingAdapter; // Adapter for the pending list

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);

        // --- Get the NEW ViewModel ---
        adminViewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);

        setupRecyclerView();
        observeViewModel(); // Call this here

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get username from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("admin_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("ADMIN_NAME", "Admin");

        // This line now correctly finds the 'text_welcome' in your new layout
        binding.textWelcome.setText(getString(R.string.welcome_message, username));

        // Fetch all data for the dashboard
        adminViewModel.fetchHomeDashboardData();

        setupClickListeners(view);

        // Back press handler
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().moveTaskToBack(true);
            }
        });
    }

    private void setupRecyclerView() {
        pendingAdapter = new AdminAppointmentAdapter(this); // Pass 'this' as the listener
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

    // --- THIS METHOD IS NOW FULLY UPDATED ---
    private void observeViewModel() {

        // --- THIS IS THE NEWLY FUNCTIONAL PART ---
        // Observer for the "At a Glance" stats
        adminViewModel.stats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.tvStatBookings.setText(String.valueOf(stats.totalBookings));
                binding.tvStatPending.setText(String.valueOf(stats.pendingCount));
                binding.tvStatRevenue.setText(stats.getFormattedRevenue());
            }
        });
        // --- END OF NEW PART ---

        // Observer for the "Pending" list
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

        // Observer for the main loading state (for the stats)
        adminViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.statsLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Use INVISIBLE to prevent the layout from "jumping"
            binding.statsContainer.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });
    }

    // --- Interface methods (unchanged, but necessary) ---

    @Override
    public void onConfirmClicked(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Appointment")
                .setMessage("Are you sure you want to confirm this appointment?")
                .setPositiveButton("Yes, Confirm", (dialog, which) -> {
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Confirmed");
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
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}