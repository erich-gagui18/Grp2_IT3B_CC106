package com.example.beteranos.reservation.BarbersActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R;
import com.example.beteranos.reservation.PromoActivity.PromoFragment;
import com.example.beteranos.reservation.ServicesActivity.ReservationFragment;

public class BarbersFragment extends AppCompatActivity {

    LinearLayout selectAaron, selectJames, selectCarl, selectKevin;
    Button btnNext, btnServices, btnPromo;
    ImageButton backButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_barbers);

    // Find views
    backButton = findViewById(R.id.back_button);
    btnServices = findViewById(R.id.btn_services);
    btnPromo = findViewById(R.id.btn_promo);
    selectAaron = findViewById(R.id.select_aaron);
    selectJames = findViewById(R.id.select_james);
    selectCarl = findViewById(R.id.select_carl);
    selectKevin = findViewById(R.id.select_kevin);
    btnNext = findViewById(R.id.btn_next);

    // 🔙 Handle Back button
        backButton.setOnClickListener(v -> finish()); // This finishes the current activity
        btnServices.setOnClickListener(v -> {
            Intent intent = new Intent(BarbersFragment.this, ReservationFragment.class);
            startActivity(intent);
        });
        btnPromo.setOnClickListener(v -> {
            Intent intent = new Intent(BarbersFragment.this, PromoFragment.class);
            startActivity(intent);
        });

    // Barber selections (These are for user feedback, not navigation)
        selectAaron.setOnClickListener(v ->
            Toast.makeText(this, "Aaron selected", Toast.LENGTH_SHORT).show());

        selectJames.setOnClickListener(v ->
            Toast.makeText(this, "James selected", Toast.LENGTH_SHORT).show());

        selectCarl.setOnClickListener(v ->
            Toast.makeText(this, "Carl selected", Toast.LENGTH_SHORT).show());

        selectKevin.setOnClickListener(v ->
            Toast.makeText(this, "Kevin selected", Toast.LENGTH_SHORT).show());

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, PromoFragment.class);
            startActivity(intent);
        });
    }
}