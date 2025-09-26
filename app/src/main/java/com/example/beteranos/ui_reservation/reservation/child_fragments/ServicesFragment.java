package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.beteranos.databinding.FragmentServicesBinding;

public class ServicesFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentServicesBinding binding = FragmentServicesBinding.inflate(inflater, container, false);

        binding.btnHaircut.setOnClickListener(v -> Toast.makeText(getContext(), "Haircut Clicked", Toast.LENGTH_SHORT).show());
        binding.btnHaircolor.setOnClickListener(v -> Toast.makeText(getContext(), "Hair Color Clicked", Toast.LENGTH_SHORT).show());
        binding.btnHairwash.setOnClickListener(v -> Toast.makeText(getContext(), "Hairwash Clicked", Toast.LENGTH_SHORT).show());

        return binding.getRoot();
    }
}