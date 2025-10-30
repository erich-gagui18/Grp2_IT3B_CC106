package com.example.beteranos.ui_admin.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.databinding.FragmentAdminReservationBinding;
import com.example.beteranos.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdminReservationFragment extends Fragment implements AdminAppointmentAdapter.OnAppointmentActionListener {

    private FragmentAdminReservationBinding binding;
    private AdminReservationViewModel adminViewModel;
    private AdminAppointmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminReservationBinding.inflate(inflater, container, false);
        adminViewModel = new ViewModelProvider(this).get(AdminReservationViewModel.class);

        setupRecyclerView();
        setupCalendarListener();
        observeViewModel();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load today's appointments initially
        long todayMillis = binding.calendarView.getDate();
        fetchAppointmentsForDate(todayMillis);
    }

    private void setupRecyclerView() {
        adapter = new AdminAppointmentAdapter(this);
        binding.reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reservationsRecyclerView.setAdapter(adapter);
    }

    private void setupCalendarListener() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            long selectedDateMillis = calendar.getTimeInMillis();
            fetchAppointmentsForDate(selectedDateMillis);
        });
    }

    private void fetchAppointmentsForDate(long dateInMillis) {
        // Update label
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        binding.reservationsLabel.setText("Reservations for " + sdf.format(dateInMillis));

        // Fetch from ViewModel
        adminViewModel.fetchAppointmentsForDate(dateInMillis);
    }

    private void observeViewModel() {
        adminViewModel.appointments.observe(getViewLifecycleOwner(), appointments -> {
            // Submit a new copy to ListAdapter to trigger DiffUtil efficiently
            adapter.submitList(appointments == null ? new ArrayList<>() : new ArrayList<>(appointments));

            // Show empty list text if no appointments
            boolean empty = appointments == null || appointments.isEmpty();
            binding.reservationsRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.emptyListText.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        adminViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading != null && isLoading ? View.VISIBLE : View.GONE);
        });
    }

    // Adapter interface callbacks
    @Override
    public void onApprove(Appointment appointment) {
        adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Scheduled");
    }

    @Override
    public void onReject(Appointment appointment) {
        adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Cancelled");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.reservationsRecyclerView.setAdapter(null);
        binding = null;
    }
}
