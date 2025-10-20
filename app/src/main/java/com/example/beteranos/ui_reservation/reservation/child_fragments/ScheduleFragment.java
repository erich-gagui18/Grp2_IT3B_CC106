package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentScheduleBinding;
import com.example.beteranos.models.Service;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationConfirmationFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        // --- THIS IS THE OPTIMIZATION ---
        setupDatePicker();
        setupTimePicker();

        binding.btnBookNow.setOnClickListener(v -> {
            if (isReservationDataValid()) {
                // Instead of showing a loading screen and saving,
                // navigate to the new PaymentFragment.
                if (getParentFragment() instanceof com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment) {
                    ((com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment) getParentFragment())
                            .navigateToPayment(); // You will need to create this method in ReservationFragment
                }
            }
        });

        observeReservationStatus();

        return binding.getRoot();
    }

    private void setupDatePicker() {
        binding.dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format the date to MM/dd/yyyy
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        String formattedDate = sdf.format(selectedDate.getTime());

                        // Update UI and ViewModel
                        binding.dateEditText.setText(formattedDate);
                        sharedViewModel.selectedDate.setValue(formattedDate);

                        // Clear the selected time and fetch available slots for the new date
                        binding.timeEditText.setText("");
                        sharedViewModel.selectedTime.setValue(null);
                        sharedViewModel.fetchAvailableSlots(selectedDate.getTimeInMillis());
                    }, year, month, day);

            // Prevent selecting past dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    private void setupTimePicker() {
        binding.timeEditText.setOnClickListener(v -> {
            // Ensure a date is selected first
            if (sharedViewModel.selectedDate.getValue() == null || sharedViewModel.selectedDate.getValue().isEmpty()) {
                Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a calendar instance for current time
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Show a TimePickerDialog
            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                    requireContext(),
                    (view, selectedHour, selectedMinute) -> {
                        // Format the time to hh:mm a
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedTime.set(Calendar.MINUTE, selectedMinute);
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String formattedTime = sdf.format(selectedTime.getTime());

                        // Update UI and ViewModel
                        binding.timeEditText.setText(formattedTime);
                        sharedViewModel.selectedTime.setValue(formattedTime);
                    },
                    hour, minute, false
            );

            timePickerDialog.setTitle("Set Time");
            timePickerDialog.show();
        });
    }

    private void observeReservationStatus() {
        sharedViewModel.reservationStatus.observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                // Hide the loading overlay
                binding.loadingOverlay.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(getContext(), "Booking Successful!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.child_fragment_container, new ReservationConfirmationFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Booking Failed. Please try again.", Toast.LENGTH_LONG).show();
                    // --- THIS IS THE FIX ---
                    // Show the button again if the booking fails
                    binding.btnBookNow.setVisibility(View.VISIBLE);
                }
                sharedViewModel.reservationStatus.setValue(null); // Reset status
            }
        });
    }

    private boolean isReservationDataValid() {
        // ... (Your existing validation logic from the previous step)
        String firstName = sharedViewModel.firstName.getValue();
        if (firstName == null || firstName.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please provide customer details.", Toast.LENGTH_SHORT).show();
            return false;
        }
        List<Service> services = sharedViewModel.selectedServices.getValue();
        if (services == null || services.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one service.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sharedViewModel.selectedBarber.getValue() == null) {
            Toast.makeText(getContext(), "Please select a barber.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sharedViewModel.selectedPromo.getValue() == null) {
            Toast.makeText(getContext(), "Please select a promo.", Toast.LENGTH_SHORT).show();
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