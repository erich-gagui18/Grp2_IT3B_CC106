package com.example.beteranos.ui.reservation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.R;

public class BarbersActivity extends AppCompatActivity {

    LinearLayout selectAaron, selectJames, selectCarl, selectKevin;
    Button btnNext;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barbers); // make sure the file name matches your XML

        // Find views
        backButton = findViewById(R.id.back_button);
        selectAaron = findViewById(R.id.select_aaron);
        selectJames = findViewById(R.id.select_james);
        selectCarl = findViewById(R.id.select_carl);
        selectKevin = findViewById(R.id.select_kevin);
        btnNext = findViewById(R.id.btn_next);

        // 🔙 Handle Back button
        backButton.setOnClickListener(v -> finish());

        // Barber selections
        selectAaron.setOnClickListener(v ->
                Toast.makeText(this, "Aaron selected", Toast.LENGTH_SHORT).show());

        selectJames.setOnClickListener(v ->
                Toast.makeText(this, "James selected", Toast.LENGTH_SHORT).show());

        selectCarl.setOnClickListener(v ->
                Toast.makeText(this, "Carl selected", Toast.LENGTH_SHORT).show());

        selectKevin.setOnClickListener(v ->
                Toast.makeText(this, "Kevin selected", Toast.LENGTH_SHORT).show());

        // Next button
        btnNext.setOnClickListener(v ->
                Toast.makeText(this, "Next button clicked", Toast.LENGTH_SHORT).show());
    }
}
