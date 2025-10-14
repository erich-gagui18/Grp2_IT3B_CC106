package com.example.beteranos.ui_reservation.reservation.child_fragments; // Make sure package is correct

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentScheduleBinding;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
            binding.tvSelectedDate.setText("Selected Date: " + selectedDate);
        });

        binding.timeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_9am) selectedTime = "9:00 AM";
            else if (checkedId == R.id.radio_10am) selectedTime = "10:00 AM";
            else if (checkedId == R.id.radio_11am) selectedTime = "11:00 AM";
            else if (checkedId == R.id.radio_1pm) selectedTime = "1:00 PM";
            binding.tvSelectedTime.setText("Selected Time: " + selectedTime);
        });

        binding.btnBookNow.setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(getContext(), "Please select a date and time", Toast.LENGTH_SHORT).show();
            } else {
                String bookingMessage = "Booked for " + selectedDate + " at " + selectedTime;
                Toast.makeText(getContext(), bookingMessage, Toast.LENGTH_LONG).show();
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