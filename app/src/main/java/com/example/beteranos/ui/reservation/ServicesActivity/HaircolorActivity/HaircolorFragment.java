package com.example.beteranos.ui.reservation.ServicesActivity.HaircolorActivity;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R;
import com.example.beteranos.ui.reservation.BarbersActivity.BarbersFragment;
import com.example.beteranos.ui.reservation.PromoActivity.PromoFragment;
import com.example.beteranos.ui.reservation.ServicesActivity.ReservationFragment;

public class HaircolorFragment extends AppCompatActivity {

    private LinearLayout selectGray, selectRed, selectBlue, selectYellow;
    private Button btnNext, btnBarbers, btnPromo, btnServices;
    private ImageButton backButton;

    private boolean isServiceSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_haircolor);

        backButton = findViewById(R.id.back_button);
        btnServices = findViewById(R.id.btn_services);
        btnBarbers = findViewById(R.id.btn_barbers);
        btnPromo = findViewById(R.id.btn_promo);
        selectGray = findViewById(R.id.select_gray);
        selectRed = findViewById(R.id.select_red);
        selectBlue = findViewById(R.id.select_blue);
        selectYellow = findViewById(R.id.select_yellow);
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

        selectGray.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Gray selected", Toast.LENGTH_SHORT).show(); });
        selectRed.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Red selected", Toast.LENGTH_SHORT).show(); });
        selectBlue.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Blue selected", Toast.LENGTH_SHORT).show(); });
        selectYellow.setOnClickListener(v -> { isServiceSelected = true; Toast.makeText(this, "Yellow selected", Toast.LENGTH_SHORT).show(); });

        btnNext.setOnClickListener(v -> {
            if (!isServiceSelected) {
                Toast.makeText(this, "Choose Services!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, BarbersFragment.class));
            }
        });
    }
}
