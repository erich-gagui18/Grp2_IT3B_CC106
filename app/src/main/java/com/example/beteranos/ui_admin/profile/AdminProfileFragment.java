package com.example.beteranos.ui_admin.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
// --- REMOVED LinearLayoutManager import ---

import com.example.beteranos.databinding.FragmentAdminProfileBinding;
// --- REMOVED AdminAppointmentAdapter import ---
import com.example.beteranos.ui_admin_login.AdminLoginActivity;

public class AdminProfileFragment extends Fragment {

    private FragmentAdminProfileBinding binding;
    private AdminProfileViewModel viewModel;
    // --- REMOVED adapter variable ---

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminProfileViewModel.class);
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- REMOVED setupRecyclerView() call ---

        // Fetch admin details
        SharedPreferences prefs = requireActivity().getSharedPreferences("admin_prefs", Context.MODE_PRIVATE);
        int adminId = prefs.getInt("ADMIN_ID", -1);

        // Fetch data
        viewModel.fetchAdminDetails(adminId);
        // --- REMOVED viewModel.fetchAppointmentHistory() call ---

        observeViewModel();
        setupClickListeners();
    }

    // --- REMOVED setupRecyclerView() method ---

    private void observeViewModel() {
        // Observe the admin name
        viewModel.name.observe(getViewLifecycleOwner(), name -> {
            binding.tvAdminName.setText(name);
        });

        // Observe the loading state
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // --- SIMPLIFIED loading logic ---
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.tvAdminName.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        // --- REMOVED appointmentHistory observer ---

        // Observer for the logout navigation
        viewModel.navigateToLogin.observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate) {
                // (Your existing code is correct)
                Intent intent = new Intent(getActivity(), AdminLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
                viewModel.onLoginNavigationComplete();
            }
        });
    }

    private void setupClickListeners() {
        // (Your existing code is correct)
        binding.btnLogout.setOnClickListener(v -> {
            viewModel.logout(requireContext());
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}