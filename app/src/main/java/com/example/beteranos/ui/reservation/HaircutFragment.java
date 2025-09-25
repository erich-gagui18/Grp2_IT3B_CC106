package com.example.beteranos.ui.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;

public class HaircutFragment extends Fragment {

    private HaircutViewModel viewModel;

    private LinearLayout selectBuzz, selectLowFade, selectMidTaper, selectMullet;
    private Button btnNext;
    private ImageButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_haircut, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HaircutViewModel.class);

        // Find views
        backButton = view.findViewById(R.id.back_button);
        selectBuzz = view.findViewById(R.id.select_buzz);
        selectLowFade = view.findViewById(R.id.select_lowfade);
        selectMidTaper = view.findViewById(R.id.select_midtaper);
        selectMullet = view.findViewById(R.id.select_mullet);
        btnNext = view.findViewById(R.id.btn_next);

        // Back button
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Observe selected haircut
        viewModel.getSelectedHaircut().observe(getViewLifecycleOwner(), haircut ->
                Toast.makeText(requireContext(), haircut + " selected", Toast.LENGTH_SHORT).show()
        );

        // Haircut selections
        selectBuzz.setOnClickListener(v -> viewModel.selectHaircut("Buzz Cut"));
        selectLowFade.setOnClickListener(v -> viewModel.selectHaircut("Low Fade"));
        selectMidTaper.setOnClickListener(v -> viewModel.selectHaircut("Mid Taper"));
        selectMullet.setOnClickListener(v -> viewModel.selectHaircut("Mullet"));

        // Next button
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BarbersActivity.class);
            startActivity(intent);
        });
    }
}
