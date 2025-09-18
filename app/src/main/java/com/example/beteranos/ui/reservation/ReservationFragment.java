package com.example.beteranos.ui.reservation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.beteranos.R;

public class ReservationFragment extends Fragment {

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_reservation, container, false);

        ImageButton backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Top Category Buttons
        Button btnServices = root.findViewById(R.id.btn_services);
        Button btnBarbers = root.findViewById(R.id.btn_barbers);
        Button btnPromo = root.findViewById(R.id.btn_promo);


        // Services buttons
        Button btnHaircut = root.findViewById(R.id.btn_haircut);
        Button btnHairColor = root.findViewById(R.id.btn_haircolor);
        Button btnHairwash = root.findViewById(R.id.btn_hairwash);
        Button btnNext = root.findViewById(R.id.btn_next);


        // Handle button clicks

        btnHaircut.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HaircutActivity.class);
            startActivity(intent);
        });

        btnHairColor.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HairColorActivity.class);
            startActivity(intent);
        });

        btnHairwash.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HairwashActivity.class);
            startActivity(intent);
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), BarbersActivity.class);
            startActivity(intent);
        });

        return root;

    }
}
