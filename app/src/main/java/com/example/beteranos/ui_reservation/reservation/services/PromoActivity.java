package com.example.beteranos.ui_reservation.reservation.services; // Or your actual package name

import android.content.Intent; // IMPORTANT: Import Intent
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout; // If you are using these for selection
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R; // Your R file

public class PromoActivity extends AppCompatActivity {

    // Declare views that are in activity_promo.xml
    ImageButton backButton;
    Button btnServices, btnBarbers, btnPromo; // Category buttons
    LinearLayout selectPromo1, selectPromo2, selectPromo3, selectPromo4; // Promo selection layouts
    Button btnNext; // The "NEXT >" button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo); // Make sure this matches your XML file name

        // Initialize views from activity_promo.xml
        backButton = findViewById(R.id.back_button);
        btnServices = findViewById(R.id.btn_services);
        btnBarbers = findViewById(R.id.btn_barbers);
        btnPromo = findViewById(R.id.btn_promo);

        selectPromo1 = findViewById(R.id.select_promo1);
        selectPromo2 = findViewById(R.id.select_promo2);
        selectPromo3 = findViewById(R.id.select_promo3);
        selectPromo4 = findViewById(R.id.select_promo4);

        btnNext = findViewById(R.id.btn_next); // This is your "NEXT >" button

        // --- Set OnClick Listeners ---

        // Back button functionality
        backButton.setOnClickListener(v -> finish()); // Goes back to the previous activity

        // Category button functionalities (optional, add navigation if needed)
        btnServices.setOnClickListener(v -> {
            Toast.makeText(PromoActivity.this, "Services Clicked", Toast.LENGTH_SHORT).show();
            // Example: Intent intent = new Intent(PromoActivity.this, ServicesActivity.class);
            // startActivity(intent);
        });

        btnBarbers.setOnClickListener(v -> {
            Toast.makeText(PromoActivity.this, "Barbers Clicked", Toast.LENGTH_SHORT).show();
            // Example: Intent intent = new Intent(PromoActivity.this, BarbersActivity.class);
            // startActivity(intent);
        });

        btnPromo.setOnClickListener(v -> {
            // Already on Promo, so maybe just a Toast or visual feedback
            Toast.makeText(PromoActivity.this, "Promo Clicked (Current Page)", Toast.LENGTH_SHORT).show();
        });

        // Promo selection functionalities (optional, add logic as needed)
        selectPromo1.setOnClickListener(v -> Toast.makeText(PromoActivity.this, "Promo 1 Selected", Toast.LENGTH_SHORT).show());
        selectPromo2.setOnClickListener(v -> Toast.makeText(PromoActivity.this, "Promo 2 Selected", Toast.LENGTH_SHORT).show());
        selectPromo3.setOnClickListener(v -> Toast.makeText(PromoActivity.this, "Promo 3 Selected", Toast.LENGTH_SHORT).show());
        selectPromo4.setOnClickListener(v -> Toast.makeText(PromoActivity.this, "Promo 4 Selected", Toast.LENGTH_SHORT).show());


        // ▼▼▼ THIS IS THE REVISED "NEXT >" BUTTON FUNCTIONALITY ▼▼▼
        btnNext.setOnClickListener(v -> {
            // Replace NextScreenActivity.class with the actual Activity you want to navigate to
            Intent intent = new Intent(PromoActivity.this, ScheduleActivity.class);
            startActivity(intent);
        });
    }
}
