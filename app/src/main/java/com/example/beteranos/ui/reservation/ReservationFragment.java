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
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // Find all views
        ImageButton backButton = root.findViewById(R.id.back_button);
        Button btnServices = root.findViewById(R.id.btn_services);
        Button btnBarbers = root.findViewById(R.id.btn_barbers);
        Button btnPromo = root.findViewById(R.id.btn_promo);

        Button btnHaircut = root.findViewById(R.id.btn_haircut);
        Button btnHairColor = root.findViewById(R.id.btn_haircolor);
        Button btnHairwash = root.findViewById(R.id.btn_hairwash);
        Button btnNext = root.findViewById(R.id.btn_next);

        // Back button logic
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // ********************************************
        // ******* HAIRCUT NAVIGATION (THE FINAL FIX) *******
        // ********************************************
        btnHaircut.setOnClickListener(v -> {
            // Uses Intent, which is correct because HaircutActivity is now an Activity
            Intent intent = new Intent(getContext(), HaircutActivity.class);
            startActivity(intent);
        });

        // Navigation for Hair Color (Starts an Activity)
        btnHairColor.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HairColorActivity.class);
            startActivity(intent);
        });

        // Navigation for Hair Wash (Starts an Activity)
        btnHairwash.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HairwashActivity.class);
            startActivity(intent);
        });

        // Navigation for Next (Starts BarbersActivity)
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), BarbersActivity.class);
            startActivity(intent);
        });
    }
}