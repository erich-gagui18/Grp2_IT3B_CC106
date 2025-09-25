package com.example.beteranos.ui.reservation;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
// NOTE: You will need to make sure HaircutViewModel is accessible or initialize it differently
// since Activities use a different ViewModel scope than Fragments.

public class HaircutActivity extends AppCompatActivity {

    // IMPORTANT: Remove the ViewModel lines if you're not using them,
    // or initialize them with a suitable factory for an Activity.
    // private HaircutViewModel viewModel;

    private LinearLayout selectBuzz, selectLowFade, selectMidTaper, selectMullet;
    private Button btnNext;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure you have a layout named activity_haircut.xml
        setContentView(R.layout.fragment_haircut);

        // viewModel = new ViewModelProvider(this).get(HaircutViewModel.class); // If needed

        // Find views
        backButton = findViewById(R.id.back_button);
        selectBuzz = findViewById(R.id.select_buzz);
        selectLowFade = findViewById(R.id.select_lowfade);
        selectMidTaper = findViewById(R.id.select_midtaper);
        selectMullet = findViewById(R.id.select_mullet);
        btnNext = findViewById(R.id.btn_next);

        // Back button
        backButton.setOnClickListener(v -> finish()); // Use finish() to go back

        // Haircut selections (Remove ViewModel lines if you ditch the ViewModel)
        // selectBuzz.setOnClickListener(v -> viewModel.selectHaircut("Buzz Cut"));
        // ... (rest of the listeners)

        // Next button (Navigation remains correct for BarbersActivity)
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, BarbersActivity.class);
            startActivity(intent);
        });
    }
}