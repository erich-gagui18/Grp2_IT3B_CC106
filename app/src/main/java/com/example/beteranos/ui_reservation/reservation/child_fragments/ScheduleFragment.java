package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.app.DatePickerDialog;
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
import com.example.beteranos.models.Service;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private SharedReservationViewModel sharedViewModel;
    private boolean isGuest = false;

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

        // --- THIS IS THE INTEGRATED GUEST LOGIC ---
        // 1. Check if the user is a guest and show the code field if they are.
        int customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);
        if (customerId == -1) {
            isGuest = true;
            binding.guestCodeLayout.setVisibility(View.VISIBLE);
        }

        // 2. The button now calls a new method to handle the guest/user branching logic.
        binding.btnBookNow.setOnClickListener(v -> handleBookingProceed());

        // 3. Add an observer to wait for the result of the guest code validation.
        sharedViewModel.isGuestCodeValid.observe(getViewLifecycleOwner(), isValid -> {
            if (isValid != null) {
                if (isValid) {
                    navigateToPayment(); // If code is valid, proceed.
                } else {
                    Toast.makeText(getContext(), "Invalid or already used Guest Code.", Toast.LENGTH_SHORT).show();
                }
                sharedViewModel.isGuestCodeValid.setValue(null); // Reset observer
            }
        });
    }

    // 4. This new method handles the logic for both guests and logged-in users.
    private void handleBookingProceed() {
        // First, validate that all previous steps are complete.
        if (!isReservationDataValid()) {
            return;
        }

        if (isGuest) {
            // If it's a guest, get the code and ask the ViewModel to validate it.
            String guestCode = binding.guestCodeEditText.getText().toString().trim();
            if (guestCode.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a Guest Code to proceed", Toast.LENGTH_SHORT).show();
            } else {
                sharedViewModel.validateGuestCode(guestCode);
            }
        } else {
            // If it's a logged-in user, they don't need a code, so proceed directly.
            navigateToPayment();
        }
    }

    private void navigateToPayment() {
        if (getParentFragment() instanceof ReservationFragment) {
            ((ReservationFragment) getParentFragment()).navigateToPayment();
        }
    }

    // --- The rest of your methods remain the same ---

    private void setupDatePicker() {
        binding.dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (dpView, year, month, day) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, day);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        String formattedDate = sdf.format(selectedDate.getTime());
                        binding.dateEditText.setText(formattedDate);
                        sharedViewModel.selectedDate.setValue(formattedDate);
                        binding.timeEditText.setText("");
                        sharedViewModel.selectedTime.setValue(null);
                        // sharedViewModel.fetchAvailableSlots(selectedDate.getTimeInMillis());
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    private void setupTimePicker() {
        binding.timeEditText.setOnClickListener(v -> {
            if (sharedViewModel.selectedDate.getValue() == null || sharedViewModel.selectedDate.getValue().isEmpty()) {
                Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }
            Calendar calendar = Calendar.getInstance();
            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                    requireContext(),
                    (tpView, hourOfDay, minute) -> {
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                        String formattedTime = sdf.format(selectedTime.getTime());

                        binding.timeEditText.setText(formattedTime);
                        sharedViewModel.selectedTime.setValue(formattedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
            );
            timePickerDialog.show();
        });
    }

    private boolean isReservationDataValid() {
        // This validation is good, it ensures all previous steps are filled out.
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