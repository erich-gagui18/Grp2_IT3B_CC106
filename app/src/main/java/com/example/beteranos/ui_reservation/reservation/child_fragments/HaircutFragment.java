package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentHaircutBinding;

public class HaircutFragment extends Fragment {

    private FragmentHaircutBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHaircutBinding.inflate(inflater, container, false);

        binding.backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.selectBuzz.setOnClickListener(v -> showToast("Buzz Cut selected"));
        binding.selectLowfade.setOnClickListener(v -> showToast("Low Fade selected"));
        binding.selectMidtaper.setOnClickListener(v -> showToast("Mid Taper selected"));
        binding.selectMullet.setOnClickListener(v -> showToast("Mullet selected"));

        binding.btnNext.setOnClickListener(v -> {
            // Navigate to the next step, e.g., the Barbers selection
            // This would be similar to the navigation in ServicesFragment
        });

        return binding.getRoot();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}