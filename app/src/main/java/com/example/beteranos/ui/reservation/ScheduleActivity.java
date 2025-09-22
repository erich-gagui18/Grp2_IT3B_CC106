package com.example.beteranos.ui.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
// import android.widget.ToggleButton; // Only if you are actually using ToggleButtons elsewhere

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R; // Ensure this matches your application's package name

public class ScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RadioGroup timeRadioGroup; // For time selection
    private Button btnBookNow;
    private TextView tvSelectedDate, tvSelectedTime; // Optional: To display selections

    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule); // Requires res/layout/activity_schedule.xml

        // Initialize Views - IDs must exist in your activity_schedule.xml
        calendarView = findViewById(R.id.calendarView);
        timeRadioGroup = findViewById(R.id.timeRadioGroup); // CRITICAL: R.id.timeRadioGroup must exist in activity_schedule.xml
        btnBookNow = findViewById(R.id.btn_book_now);
        tvSelectedDate = findViewById(R.id.tvSelectedDate); // Optional: add TextView with this ID
        tvSelectedTime = findViewById(R.id.tvSelectedTime); // Optional: add TextView with this ID

        // --- Calendar Selection ---
        if (calendarView != null) {
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year; // Month is 0-indexed
                if (tvSelectedDate != null) {
                    tvSelectedDate.setText("Selected Date: " + selectedDate);
                }
                Log.d("ScheduleActivity", "Selected Date: " + selectedDate);
            });
        } else {
            Log.e("ScheduleActivity", "CalendarView with ID 'calendarView' not found.");
            Toast.makeText(this, "Error: Calendar view not found.", Toast.LENGTH_LONG).show();
        }


        // --- Time Selection using RadioGroup ---
        if (timeRadioGroup != null) {
            timeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton checkedRadioButton = findViewById(checkedId);
                if (checkedRadioButton != null) {
                    selectedTime = checkedRadioButton.getText().toString();
                    if (tvSelectedTime != null) {
                        tvSelectedTime.setText("Selected Time: " + selectedTime);
                    }
                    Log.d("ScheduleActivity", "Selected Time: " + selectedTime);
                } else {
                    selectedTime = ""; // Clear if somehow no button is found
                    if (tvSelectedTime != null) {
                        tvSelectedTime.setText("Selected Time: ");
                    }
                }
            });
        } else {
            Log.e("ScheduleActivity", "RadioGroup with ID 'timeRadioGroup' not found in layout.");
            // Optionally inform the user or disable booking if the RadioGroup is missing
            Toast.makeText(this, "Error: Time selection options not found.", Toast.LENGTH_LONG).show();
            if (btnBookNow != null) btnBookNow.setEnabled(false); // Disable booking if time can't be selected
        }


        // --- Book Now Button Click ---
        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(v -> {
                if (selectedDate.isEmpty()) {
                    Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (timeRadioGroup == null || timeRadioGroup.getCheckedRadioButtonId() == -1) { // Check if RadioGroup exists and a selection is made
                    Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // At this point, selectedTime should have been updated by the RadioGroup's listener
                if (selectedTime.isEmpty()) { // Double-check selectedTime just in case
                    Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
                    return;
                }


                String bookingMessage = "Appointment booked for: " + selectedDate + " at " + selectedTime;
                Toast.makeText(this, bookingMessage, Toast.LENGTH_LONG).show();
                Log.d("ScheduleActivity", bookingMessage);

                // Optional: Navigate to a confirmation activity or save data
                // Intent intent = new Intent(ScheduleActivity.this, ConfirmationActivity.class);
                // intent.putExtra("SELECTED_DATE", selectedDate);
                // intent.putExtra("SELECTED_TIME", selectedTime);
                // startActivity(intent);
            });
        } else {
            Log.e("ScheduleActivity", "Button with ID 'btn_book_now' not found in layout.");
        }
    }
}
