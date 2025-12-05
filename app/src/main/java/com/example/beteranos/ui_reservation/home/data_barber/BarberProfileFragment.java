package com.example.beteranos.ui_reservation.home.data_barber;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import com.example.beteranos.databinding.FragmentBarberProfileBinding;

public class BarberProfileFragment extends Fragment {

    private FragmentBarberProfileBinding binding;
    private BarberProfileViewModel viewModel;
    private BarberProfileAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBarberProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle Custom Back Button Click
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        viewModel = new ViewModelProvider(this).get(BarberProfileViewModel.class);
        setupRecyclerView();
        observeViewModel();

        viewModel.fetchBarbers();
    }

    // --- Hide Action Bar Back Button when screen appears ---
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false); // Hide system arrow
                actionBar.setTitle("Barber Profiles");
            }
        }
    }

    // --- Restore Action Bar Back Button when leaving screen ---
    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                // ⭐️ FIX: Set this to TRUE so other screens have a back button
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("Beteranos");
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new BarberProfileAdapter();
        binding.rvBarbers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBarbers.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.barbers.observe(getViewLifecycleOwner(), barbers -> {
            if (barbers != null && !barbers.isEmpty()) {
                adapter.submitList(barbers);
                binding.rvBarbers.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setVisibility(View.GONE);
            } else {
                binding.rvBarbers.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}