package com.example.beteranos.reservation.ServicesActivity.HaircutActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R;
import com.example.beteranos.reservation.BarbersActivity.BarbersFragment;
import com.example.beteranos.reservation.PromoActivity.PromoFragment;
import com.example.beteranos.reservation.ServicesActivity.ReservationFragment;

public class HaircutFragment extends AppCompatActivity {

    private LinearLayout selectBuzz, selectLowFade, selectMidTaper, selectMullet;
    private Button btnNext, btnBarbers, btnPromo, btnServices;
    private ImageButton backButton;

    private boolean isServiceSelected = false; // Track selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_haircut);

        backButton = findViewById(R.id.back_button);
        btnServices = findViewById(R.id.btn_services);
        btnBarbers = findViewById(R.id.btn_barbers);
        btnPromo = findViewById(R.id.btn_promo);
        selectBuzz = findViewById(R.id.select_buzz);
        selectLowFade = findViewById(R.id.select_lowfade);
        selectMidTaper = findViewById(R.id.select_midtaper);
        selectMullet = findViewById(R.id.select_mullet);
        btnNext = findViewById(R.id.btn_next);

        backButton.setOnClickListener(v -> finish());

        btnServices.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservationFragment.class);
            startActivity(intent);
        });

        btnBarbers.setOnClickListener(v -> {
            if (!isServiceSelected) {
                Toast.makeText(this, "Choose Services!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, BarbersFragment.class));
            }
        });

        btnPromo.setOnClickListener(v -> {
            if (!isServiceSelected) {
                Toast.makeText(this, "Choose Services!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, PromoFragment.class));
            }
        });

        selectBuzz.setOnClickListener(v -> {
            isServiceSelected = true;
            Toast.makeText(this, "Buzz Cut selected", Toast.LENGTH_SHORT).show();
        });

        selectLowFade.setOnClickListener(v -> {
            isServiceSelected = true;
            Toast.makeText(this, "Low Fade selected", Toast.LENGTH_SHORT).show();
        });

        selectMidTaper.setOnClickListener(v -> {
            isServiceSelected = true;
            Toast.makeText(this, "Mid Taper selected", Toast.LENGTH_SHORT).show();
        });

        selectMullet.setOnClickListener(v -> {
            isServiceSelected = true;
            Toast.makeText(this, "Mullet selected", Toast.LENGTH_SHORT).show();
        });

        btnNext.setOnClickListener(v -> {
            if (!isServiceSelected) {
                Toast.makeText(this, "Choose Services!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, BarbersFragment.class));
            }
        });
    }
}
