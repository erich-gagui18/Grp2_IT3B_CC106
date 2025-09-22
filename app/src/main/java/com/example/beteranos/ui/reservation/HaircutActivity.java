package com.example.beteranos.ui.reservation;

import android.content.Intent; // <-- IMPORT THIS CLASS
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R;

public class HaircutActivity extends AppCompatActivity {

    LinearLayout selectBuzz, selectLowFade, selectMidTaper, selectMullet;
    Button btnNext;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haircut);

        // Find views
        backButton = findViewById(R.id.back_button);
        selectBuzz = findViewById(R.id.select_buzz);
        selectLowFade = findViewById(R.id.select_lowfade);
        selectMidTaper = findViewById(R.id.select_midtaper);
        selectMullet = findViewById(R.id.select_mullet);
        btnNext = findViewById(R.id.btn_next);

        // Handle Back button
        backButton.setOnClickListener(v -> finish()); // This finishes the current activity

        // Haircut selections
        selectBuzz.setOnClickListener(v ->
                Toast.makeText(this, "Buzz Cut selected", Toast.LENGTH_SHORT).show());

        selectLowFade.setOnClickListener(v ->
                Toast.makeText(this, "Low Fade selected", Toast.LENGTH_SHORT).show());

        selectMidTaper.setOnClickListener(v ->
                Toast.makeText(this, "Mid Taper selected", Toast.LENGTH_SHORT).show());

        selectMullet.setOnClickListener(v ->
                Toast.makeText(this, "Mullet selected", Toast.LENGTH_SHORT).show());

        // Next button
        btnNext.setOnClickListener(v -> {
            // Create an Intent to start BarbersActivity
            Intent intent = new Intent(HaircutActivity.this, BarbersActivity.class);
            startActivity(intent); // Start the new activity
        });
    }
}

