package com.example.beteranos.ui_admin.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.databinding.FragmentAdminReservationBinding;
import com.example.beteranos.models.Appointment;

import com.example.beteranos.ui_admin.SharedAdminAppointmentViewModel;

import java.util.Calendar;

// The interface implementation is unchanged
public class AdminReservationFragment extends Fragment implements AdminAppointmentAdapter.OnAppointmentActionListener {

    private FragmentAdminReservationBinding binding;
    private SharedAdminAppointmentViewModel viewModel;
    private AdminAppointmentAdapter adapter;

    // onCreateView is unchanged
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

    // All setup and observe methods are unchanged...
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

    // --- INTERFACE METHODS (UPDATE) ---

    @Override
    public void onConfirmClicked(Appointment appointment) {
        // Show a confirmation dialog before confirming
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Appointment")
                .setMessage("Are you sure you want to confirm this appointment?")
                .setPositiveButton("Yes, Confirm", (dialog, which) -> {
                    viewModel.updateAppointmentStatus(appointment.getReservationId(), "Confirmed");
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onCancelClicked(Appointment appointment) {
        // Show a confirmation dialog before cancelling
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    viewModel.updateAppointmentStatus(appointment.getReservationId(), "Cancelled");
                })
                .setNegativeButton("No", null)
                .show();
    }

    // --- ADD THIS NEW METHOD ---
    @Override
    public void onMarkAsCompletedClicked(Appointment appointment) {
        // Show a confirmation dialog before completing
        new AlertDialog.Builder(requireContext())
                .setTitle("Complete Appointment")
                .setMessage("Mark this appointment as completed?")
                .setPositiveButton("Yes, Mark as Completed", (dialog, which) -> {
                    viewModel.updateAppointmentStatus(appointment.getReservationId(), "Completed");
                })
                .setNegativeButton("No", null)
                .show();
    }
    // --- END OF NEW METHOD ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}