package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog; // ⭐️ Explicit Import
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.databinding.FragmentScheduleBinding;
import com.example.beteranos.models.Barber; // ⭐️ Import Barber
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        setupDatePicker();
        setupTimePicker();

        binding.btnBookNow.setOnClickListener(v -> handleBookingProceed());

        sharedViewModel.isGuestCodeValid.observe(getViewLifecycleOwner(), isValid -> {
            if (isValid != null) {
                if (isValid) {
                    navigateToPayment();
                } else {
                    Toast.makeText(getContext(), "Invalid or already used Guest Code.", Toast.LENGTH_SHORT).show();
                }
                sharedViewModel.isGuestCodeValid.setValue(null);
            }
        });
    }

    private void handleBookingProceed() {
        if (!isReservationDataValid()) {
            return;
        } else {
            navigateToPayment();
        }
    }

    private void navigateToPayment() {
        if (getParentFragment() instanceof ReservationFragment) {
            ((ReservationFragment) getParentFragment()).navigateToPayment();
        }
    }

    // -------------------------------------------------------------------------
    // ⭐️ UPDATED: DATE PICKER WITH DAY OFF VALIDATION
    // -------------------------------------------------------------------------
    private void setupDatePicker() {
        binding.dateEditText.setOnClickListener(v -> {
            // Get selected Barber to check Day Off
            Barber selectedBarber = sharedViewModel.selectedBarber.getValue();
            if (selectedBarber == null) {
                Toast.makeText(getContext(), "Please select a barber first.", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (dpView, year, month, day) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, day);

                        // ⭐️ CHECK 1: Is this the Barber's Day Off?
                        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.US).format(selectedDate.getTime()); // e.g., "Monday"
                        String barberDayOff = selectedBarber.getDayOff();

                        if (barberDayOff != null && barberDayOff.equalsIgnoreCase(dayOfWeek)) {
                            Toast.makeText(getContext(),
                                    "Sorry, " + selectedBarber.getName() + " is off on " + dayOfWeek + "s.",
                                    Toast.LENGTH_LONG).show();
                            // Clear fields
                            binding.dateEditText.setText("");
                            sharedViewModel.selectedDate.setValue(null);
                            return; // Stop here
                        }

                        // If Valid:
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        String formattedDate = sdf.format(selectedDate.getTime());
                        binding.dateEditText.setText(formattedDate);
                        sharedViewModel.selectedDate.setValue(formattedDate);

                        // Reset time when date changes
                        binding.timeEditText.setText("");
                        sharedViewModel.selectedTime.setValue(null);

                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    // -------------------------------------------------------------------------
    // ⭐️ UPDATED: TIME PICKER WITH START/END TIME VALIDATION
    // -------------------------------------------------------------------------
    private void setupTimePicker() {
        binding.timeEditText.setOnClickListener(v -> {
            if (sharedViewModel.selectedDate.getValue() == null || sharedViewModel.selectedDate.getValue().isEmpty()) {
                Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            Barber selectedBarber = sharedViewModel.selectedBarber.getValue();
            if (selectedBarber == null) return;

            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    requireContext(),
                    (tpView, hourOfDay, minute) -> {

                        // ⭐️ CHECK 2: Is Time Within Schedule?
                        if (!isTimeWithinBarberSchedule(selectedBarber, hourOfDay, minute)) {
                            // Error message is shown inside the helper function
                            binding.timeEditText.setText("");
                            sharedViewModel.selectedTime.setValue(null);
                            return;
                        }

                        // If Valid:
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String formattedTime = sdf.format(selectedTime.getTime());

                        binding.timeEditText.setText(formattedTime);
                        sharedViewModel.selectedTime.setValue(formattedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false // False = 12h view
            );
            timePickerDialog.show();
        });
    }

    // -------------------------------------------------------------------------
    // ⭐️ HELPER: LOGIC TO COMPARE TIME STRINGS vs INTEGERS
    // -------------------------------------------------------------------------
    private boolean isTimeWithinBarberSchedule(Barber barber, int userHour, int userMinute) {
        String startStr = barber.getStartTime(); // e.g., "8:00 am"
        String endStr = barber.getEndTime();     // e.g., "5:00 pm"

        // Default fallback if data is missing
        if (startStr == null) startStr = "08:00 am";
        if (endStr == null) endStr = "08:00 pm";

        int userTotalMinutes = (userHour * 60) + userMinute;
        int barberStartMinutes = parseTimeStringToMinutes(startStr);
        int barberEndMinutes = parseTimeStringToMinutes(endStr);

        // Validation Logic
        if (userTotalMinutes < barberStartMinutes) {
            Toast.makeText(getContext(), "Barber not available yet. Starts at " + startStr, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (userTotalMinutes >= barberEndMinutes) { // Use >= to ensure they don't book exactly at closing time
            Toast.makeText(getContext(), "Barber unavailable. Ends at " + endStr, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Converts "8:30 am" -> 510 minutes
    private int parseTimeStringToMinutes(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            Date date = sdf.parse(timeString);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1; // Error case
    }

    private boolean isReservationDataValid() {
        if (sharedViewModel.firstName.getValue() == null || sharedViewModel.firstName.getValue().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please go back and provide your details.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sharedViewModel.selectedServices.getValue() == null || sharedViewModel.selectedServices.getValue().isEmpty()) {
            Toast.makeText(getContext(), "Please go back and select a service.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sharedViewModel.selectedBarber.getValue() == null) {
            Toast.makeText(getContext(), "Please go back and select a barber.", Toast.LENGTH_SHORT).show();
            return false;
        }
        String date = sharedViewModel.selectedDate.getValue();
        String time = sharedViewModel.selectedTime.getValue();
        if (date == null || date.isEmpty() || time == null || time.isEmpty()) {
            Toast.makeText(getContext(), "Please select a date and time.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}