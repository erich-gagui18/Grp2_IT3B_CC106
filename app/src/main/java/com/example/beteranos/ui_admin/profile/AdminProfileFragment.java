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

import com.example.beteranos.databinding.FragmentAdminProfileBinding;
import com.example.beteranos.ui_admin_login.AdminLoginActivity;

public class AdminProfileFragment extends Fragment {

    private FragmentAdminProfileBinding binding;
    private AdminProfileViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminProfileViewModel.class);
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetch admin details
        SharedPreferences prefs = requireActivity().getSharedPreferences("admin_prefs", Context.MODE_PRIVATE);
        // Get the ADMIN_ID saved during login
        int adminId = prefs.getInt("ADMIN_ID", -1);
        viewModel.fetchAdminDetails(adminId);

        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        // Observe the admin name
        viewModel.name.observe(getViewLifecycleOwner(), name -> {
            binding.tvAdminName.setText(name);
        });

        // --- FIX: Removed the observer for viewModel.email ---

        // Observe the loading state
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.tvAdminName.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
            // --- REMOVED: tvAdminEmail visibility toggle ---
        });

        // Observer for the logout navigation
        viewModel.navigateToLogin.observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate) {
                // Create intent for the Admin Login Screen
                Intent intent = new Intent(getActivity(), AdminLoginActivity.class);
                // Clear the back stack and start a new task
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Finish the current AdminDashboardActivity
                requireActivity().finish();

                // Reset the event
                viewModel.onLoginNavigationComplete();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            // Call the logout method in the ViewModel
            viewModel.logout(requireContext());
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            // TODO: Navigate to a new ChangePasswordFragment
            Toast.makeText(getContext(), "Change Password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}