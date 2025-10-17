package com.example.beteranos.ui_admin.home;

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
import com.example.beteranos.databinding.FragmentAdminHomeBinding;
import com.example.beteranos.models.Appointment;
import com.example.beteranos.ui_admin.home.AdminHomeViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;
    private AdminHomeViewModel adminViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        adminViewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);

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
        // Observer for the list of appointments
        adminViewModel.appointments.observe(getViewLifecycleOwner(), this::populateAppointments);
    }

    private void populateAppointments(List<Appointment> appointments) {
        // Clear any previous views
        binding.reservationsContainer.removeAllViews();

        if (appointments == null || appointments.isEmpty()) {
            // Show a "no appointments" message if the list is empty
            TextView noAppointmentsView = new TextView(getContext());
            noAppointmentsView.setText("No appointments scheduled for this day.");
            noAppointmentsView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noAppointmentsView.setPadding(0, 64, 0, 0);
            binding.reservationsContainer.addView(noAppointmentsView);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        // Loop through the appointments and add them to the container
        for (Appointment appointment : appointments) {
            View appointmentView = inflater.inflate(R.layout.item_appointment, binding.reservationsContainer, false);

            TextView timeText = appointmentView.findViewById(R.id.appointment_time_text);
            TextView customerText = appointmentView.findViewById(R.id.customer_name_text);
            TextView serviceText = appointmentView.findViewById(R.id.service_name_text);
            TextView barberText = appointmentView.findViewById(R.id.barber_name_text);

            timeText.setText(timeFormat.format(appointment.getReservationTime()));
            customerText.setText(appointment.getCustomerName());
            serviceText.setText("Service: " + appointment.getServiceName());
            barberText.setText("Barber: " + appointment.getBarberName());

            binding.reservationsContainer.addView(appointmentView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}