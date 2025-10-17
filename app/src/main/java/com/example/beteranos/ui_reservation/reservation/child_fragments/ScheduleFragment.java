package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentScheduleBinding;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;

// --- THIS IS THE FIX ---
// Import the ReservationConfirmationFragment from its correct package
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationConfirmationFragment;

import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            String dateString = (month + 1) + "/" + dayOfMonth + "/" + year;
            sharedViewModel.selectedDate.setValue(dateString);
            sharedViewModel.fetchAvailableSlots(cal.getTimeInMillis());
        });

        sharedViewModel.availableTimeSlots.observe(getViewLifecycleOwner(), slots -> {
            binding.timeRadioGroup.removeAllViews();
            if (slots == null || slots.isEmpty()) {
                binding.timeLabel.setText("No available times for this day.");
            } else {
                binding.timeLabel.setText("Available Times:");
                for (String time : slots) {
                    RadioButton radioButton = new RadioButton(getContext());
                    radioButton.setText(time);
                    radioButton.setTextSize(16);
                    binding.timeRadioGroup.addView(radioButton);
                }
            }
        });

        binding.timeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null) {
                sharedViewModel.selectedTime.setValue(checkedRadioButton.getText().toString());
            }
        });

        binding.btnBookNow.setOnClickListener(v -> {
            String date = sharedViewModel.selectedDate.getValue();
            String time = sharedViewModel.selectedTime.getValue();

            if (date == null || date.isEmpty() || time == null || time.isEmpty()) {
                Toast.makeText(getContext(), "Please select a date and time", Toast.LENGTH_SHORT).show();
            } else {
                sharedViewModel.saveReservation();
            }
        });

        sharedViewModel.reservationStatus.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Booking Successful!", Toast.LENGTH_SHORT).show();

                // Navigate to the confirmation screen
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.child_fragment_container, new ReservationConfirmationFragment())
                        .addToBackStack(null)
                        .commit();

                // Reset status to prevent re-triggering on screen rotation
                sharedViewModel.reservationStatus.setValue(null);
            } else if (success != null && !success) {
                Toast.makeText(getContext(), "Booking Failed. Please try again.", Toast.LENGTH_LONG).show();
                sharedViewModel.reservationStatus.setValue(null);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}