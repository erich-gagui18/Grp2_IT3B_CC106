package com.example.beteranos.reservation.ServicesActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.beteranos.R;
import com.example.beteranos.reservation.BarbersActivity.BarbersFragment;
import com.example.beteranos.reservation.PromoActivity.PromoFragment;
import com.example.beteranos.reservation.ServicesActivity.HaircolorActivity.HaircolorFragment;
import com.example.beteranos.reservation.ServicesActivity.HaircutActivity.HaircutFragment;
import com.example.beteranos.reservation.ServicesActivity.HairwashActivity.HairwashFragment;

public class ReservationFragment extends Fragment {

    private boolean serviceSelected = false; // ✅ track if service chosen

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        ImageButton backButton = root.findViewById(R.id.back_button);
        Button btnServices = root.findViewById(R.id.btn_services);
        Button btnBarbers = root.findViewById(R.id.btn_barbers);
        Button btnPromo = root.findViewById(R.id.btn_promo);
        Button btnHaircut = root.findViewById(R.id.btn_haircut);
        Button btnHairColor = root.findViewById(R.id.btn_haircolor);
        Button btnHairwash = root.findViewById(R.id.btn_hairwash);

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // ✅ Mark service chosen when clicked
        btnHaircut.setOnClickListener(v -> {
            serviceSelected = true;
            startActivity(new Intent(getContext(), HaircutFragment.class));
        });

        btnHairColor.setOnClickListener(v -> {
            serviceSelected = true;
            startActivity(new Intent(getContext(), HaircolorFragment.class));
        });

        btnHairwash.setOnClickListener(v -> {
            serviceSelected = true;
            startActivity(new Intent(getContext(), HairwashFragment.class));
        });

        // ✅ Only allow going to Barbers if service chosen
        btnBarbers.setOnClickListener(v -> {
            if (!serviceSelected) {
                Toast.makeText(getContext(), "Choose Services!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getContext(), BarbersFragment.class));
            }
        });

        // ✅ Only allow going to Promo if service chosen
        btnPromo.setOnClickListener(v -> {
            if (!serviceSelected) {
                Toast.makeText(getContext(), "Choose Services!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(getContext(), PromoFragment.class));
            }
        });

    }
}
