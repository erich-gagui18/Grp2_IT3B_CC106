package com.example.beteranos.ui_reservation.reservation.parent_fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentReservationBinding;

import com.example.beteranos.ui_reservation.reservation.child_fragments.ServicesFragment;
import com.example.beteranos.ui_reservation.reservation.child_fragments.BarbersFragment;
import com.example.beteranos.ui_reservation.reservation.child_fragments.PromoFragment;
import com.example.beteranos.ui_reservation.reservation.child_fragments.DetailsFragment;
import com.example.beteranos.ui_reservation.reservation.child_fragments.ScheduleFragment;

public class ReservationFragment extends Fragment {

    public FragmentReservationBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReservationBinding.inflate(inflater, container, false);

        if (savedInstanceState == null) {
            replaceFragment(new DetailsFragment());
            updateButtonStyles(binding.btnReserve);
        }

        binding.btnReserve.setOnClickListener(v -> {
            replaceFragment(new DetailsFragment());
            updateButtonStyles(v);
        });
        binding.btnServices.setOnClickListener(v -> {
            replaceFragment(new ServicesFragment());
            updateButtonStyles(v);
        });
        binding.btnBarbers.setOnClickListener(v -> {
            replaceFragment(new BarbersFragment());
            updateButtonStyles(v);
        });
        binding.btnPromo.setOnClickListener(v -> {
            replaceFragment(new PromoFragment());
            updateButtonStyles(v);
        });

        return binding.getRoot();
    }

    public void navigateToServices() {
        if (binding != null) {
            binding.btnServices.performClick();
        }
    }

    public void navigateToBarbers() {
        if (binding != null) {
            binding.btnBarbers.performClick();
        }
    }

    // --- ADD THIS PUBLIC METHOD ---
    public void navigateToPromo() {
        if (binding != null) {
            binding.btnPromo.performClick();
        }
    }

    // --- ADD THIS PUBLIC METHOD ---
    public void navigateToSchedule() {
        // This navigates to the full-screen schedule fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, new ScheduleFragment()) // Use your main activity's container ID
                .addToBackStack(null)
                .commit();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.child_fragment_container, fragment);
        ft.commit();
    }

    private void updateButtonStyles(View selectedButton) {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.button_active_gray);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.button_inactive_black);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.black);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.white);

        binding.btnReserve.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
        binding.btnReserve.setTextColor(inactiveTextColor);
        binding.btnServices.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
        binding.btnServices.setTextColor(inactiveTextColor);
        binding.btnBarbers.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
        binding.btnBarbers.setTextColor(inactiveTextColor);
        binding.btnPromo.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
        binding.btnPromo.setTextColor(inactiveTextColor);

        if (selectedButton instanceof Button) {
            Button clickedButton = (Button) selectedButton;
            clickedButton.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            clickedButton.setTextColor(activeTextColor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}