package com.example.beteranos.ui_admin.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminHomeBinding;
import com.example.beteranos.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;
    private AdminHomeViewModel adminViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        adminViewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);

        setupCalendarListener();
        observeViewModel();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ Retrieve username (consistent with nav_graph argument)
        String username = "Admin";
        if (getArguments() != null && getArguments().containsKey("username")) {
            username = getArguments().getString("username");
        }

        // ✅ Use string resource for better localization
        binding.textWelcome.setText(getString(R.string.welcome_message, username));

        // ✅ Fetch appointments for today
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
        binding.reservationsLabel.setText(
                getString(R.string.reservations_for_date, sdf.format(dateInMillis))
        );
        adminViewModel.fetchAppointmentsForDate(dateInMillis);
    }

    private void observeViewModel() {
        adminViewModel.appointments.observe(getViewLifecycleOwner(), this::populateAppointments);
    }

    private void populateAppointments(List<Appointment> appointments) {
        binding.reservationsContainer.removeAllViews();

        if (appointments == null || appointments.isEmpty()) {
            TextView noAppointmentsView = new TextView(getContext());
            noAppointmentsView.setText(R.string.no_appointments);
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

            timeText.setText(timeFormat.format(appointment.getReservationTime()));
            customerText.setText(appointment.getCustomerName());
            serviceText.setText("Service: " + appointment.getServiceName());
            barberText.setText("Barber: " + appointment.getBarberName());

            // ✅ Improved color handling
            int colorRes;
            switch (appointment.getStatus().toLowerCase()) {
                case "pending":
                    colorRes = android.R.color.holo_orange_dark;
                    break;
                case "scheduled":
                case "confirmed":
                    colorRes = android.R.color.holo_green_dark;
                    break;
                case "cancelled":
                    colorRes = android.R.color.holo_red_dark;
                    break;
                default:
                    colorRes = android.R.color.darker_gray;
                    break;
            }
            statusText.setText(appointment.getStatus());
            statusText.setTextColor(ContextCompat.getColor(requireContext(), colorRes));

            binding.reservationsContainer.addView(appointmentView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
