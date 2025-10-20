package com.example.beteranos.ui_admin.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminReservationBinding;
import com.example.beteranos.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.widget.Button;
import android.widget.LinearLayout;

public class AdminReservationFragment extends Fragment {

    private FragmentAdminReservationBinding binding;
    private AdminReservationViewModel adminViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // --- THIS IS THE FIX ---
        // Inflate the layout using the correct binding class
        binding = FragmentAdminReservationBinding.inflate(inflater, container, false);
        adminViewModel = new ViewModelProvider(this).get(AdminReservationViewModel.class);

        setupCalendarListener();
        observeViewModel();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch appointments for today's date when the fragment is first created
        long today = binding.calendarView.getDate();
        updateLabelAndFetchAppointments(today);
    }

    private void setupCalendarListener() {
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            long selectedDateInMillis = calendar.getTimeInMillis();
            updateLabelAndFetchAppointments(selectedDateInMillis);
        });
    }

    private void updateLabelAndFetchAppointments(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        binding.reservationsLabel.setText("Reservations for " + sdf.format(dateInMillis));
        adminViewModel.fetchAppointmentsForDate(dateInMillis);
    }

    private void observeViewModel() {
        adminViewModel.appointments.observe(getViewLifecycleOwner(), this::populateAppointments);
    }

    private void populateAppointments(List<Appointment> appointments) {
        binding.reservationsContainer.removeAllViews();
        if (appointments == null || appointments.isEmpty()) {
            TextView noAppointmentsView = new TextView(getContext());
            noAppointmentsView.setText("No appointments scheduled for this day.");
            noAppointmentsView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noAppointmentsView.setPadding(0, 64, 0, 0);
            binding.reservationsContainer.addView(noAppointmentsView);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        for (Appointment appointment : appointments) {
            View appointmentView = inflater.inflate(R.layout.item_appointment, binding.reservationsContainer, false);
            TextView timeText = appointmentView.findViewById(R.id.appointment_time_text);
            TextView customerText = appointmentView.findViewById(R.id.customer_name_text);
            TextView serviceText = appointmentView.findViewById(R.id.service_name_text);
            TextView barberText = appointmentView.findViewById(R.id.barber_name_text);
            TextView statusText = appointmentView.findViewById(R.id.appointment_status_text);

            LinearLayout actionButtonsContainer = appointmentView.findViewById(R.id.action_buttons_container);
            Button btnApprove = appointmentView.findViewById(R.id.btn_approve);
            Button btnReject = appointmentView.findViewById(R.id.btn_reject);

            timeText.setText(timeFormat.format(appointment.getReservationTime()));
            customerText.setText(appointment.getCustomerName());
            serviceText.setText("Service: " + appointment.getServiceName());
            barberText.setText("Barber: " + appointment.getBarberName());

            // Set the status text and color
            statusText.setText(appointment.getStatus());
            switch (appointment.getStatus().toLowerCase()) {
                case "pending":
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "scheduled":
                case "confirmed":
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "cancelled":
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    statusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    break;
            }
            // Show buttons only if the status is "Pending"
            if (appointment.getStatus().equalsIgnoreCase("Pending")) {
                actionButtonsContainer.setVisibility(View.VISIBLE);

                btnApprove.setOnClickListener(v -> {
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Scheduled");
                });

                btnReject.setOnClickListener(v -> {
                    adminViewModel.updateAppointmentStatus(appointment.getReservationId(), "Cancelled");
                });
            } else {
                actionButtonsContainer.setVisibility(View.GONE);
            }

            binding.reservationsContainer.addView(appointmentView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}