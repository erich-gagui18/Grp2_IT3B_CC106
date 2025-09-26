package com.example.beteranos.reservation.PromoActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R;
import com.example.beteranos.reservation.BarbersActivity.BarbersFragment;
import com.example.beteranos.reservation.ScheduleActivity.ScheduleFragment;
import com.example.beteranos.reservation.ServicesActivity.ReservationFragment;

public class PromoFragment extends AppCompatActivity {
    ImageButton backButton;
    Button btnServices, btnBarbers, btnPromo;
    LinearLayout selectPromo1, selectPromo2, selectPromo3, selectPromo4;
    Button btnNext;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_promo);

        backButton = findViewById(R.id.back_button);
        btnServices = findViewById(R.id.btn_services);
        btnBarbers = findViewById(R.id.btn_barbers);
        btnPromo = findViewById(R.id.btn_promo);

        selectPromo1 = findViewById(R.id.select_promo1);
        selectPromo2 = findViewById(R.id.select_promo2);
        selectPromo3 = findViewById(R.id.select_promo3);
        selectPromo4 = findViewById(R.id.select_promo4);

        btnNext = findViewById(R.id.btn_next);


        // Back button functionality
        backButton.setOnClickListener(v -> finish()); // Goes back to the previous activity

        // Category button functionalities (optional, add navigation if needed)
        btnServices.setOnClickListener(v -> {
            Intent intent = new Intent(PromoFragment.this, ReservationFragment.class);
            startActivity(intent);
        });

        btnBarbers.setOnClickListener(v -> {
            Intent intent = new Intent(PromoFragment.this, BarbersFragment.class);
            startActivity(intent);
        });


        // Promo selection functionalities (optional, add logic as needed)
        selectPromo1.setOnClickListener(v -> Toast.makeText(PromoFragment.this, "Promo 1 Selected", Toast.LENGTH_SHORT).show());
        selectPromo2.setOnClickListener(v -> Toast.makeText(PromoFragment.this, "Promo 2 Selected", Toast.LENGTH_SHORT).show());
        selectPromo3.setOnClickListener(v -> Toast.makeText(PromoFragment.this, "Promo 3 Selected", Toast.LENGTH_SHORT).show());
        selectPromo4.setOnClickListener(v -> Toast.makeText(PromoFragment.this, "Promo 4 Selected", Toast.LENGTH_SHORT).show());

        // Next button functionality
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleFragment.class);
            startActivity(intent);
        });
    }
}