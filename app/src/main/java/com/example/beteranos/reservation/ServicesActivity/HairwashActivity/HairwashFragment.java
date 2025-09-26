package com.example.beteranos.reservation.ServicesActivity.HairwashActivity;

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

public class HairwashFragment extends AppCompatActivity {

    private LinearLayout selectHairwash1, selectHairwash2, selectHairwash3, selectHairwash4;
    private Button btnNext, btnBarbers, btnPromo, btnServices;
    private ImageButton backButton;

    private boolean isServiceSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hairwash);

        backButton = findViewById(R.id.back_button);
        btnServices = findViewById(R.id.btn_services);
        btnBarbers = findViewById(R.id.btn_barbers);
        btnPromo = findViewById(R.id.btn_promo);
        selectHairwash1 = findViewById(R.id.select_wash1);
        selectHairwash2 = findViewById(R.id.select_wash2);
        selectHairwash3 = findViewById(R.id.select_wash3);
        selectHairwash4 = findViewById(R.id.select_wash4);
        btnNext = findViewById(R.id.btn_next);

        backButton.setOnClickListener(v -> finish());

        btnServices.setOnClickListener(v -> startActivity(new Intent(this, ReservationFragment.class)));

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

        selectHairwash1.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Hairwash 1 selected", Toast.LENGTH_SHORT).show(); });
        selectHairwash2.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Hairwash 2 selected", Toast.LENGTH_SHORT).show(); });
        selectHairwash3.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Hairwash 3 selected", Toast.LENGTH_SHORT).show(); });
        selectHairwash4.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Hairwash 4 selected", Toast.LENGTH_SHORT).show(); });

        btnNext.setOnClickListener(v -> {
            if (!isServiceSelected) {
                Toast.makeText(this, "Choose Services!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, BarbersFragment.class));
            }
        });
    }
}
