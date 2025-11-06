package com.example.beteranos.ui_admin.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation; // Import this

import com.example.beteranos.R; // Import R
import com.example.beteranos.databinding.FragmentAdminManagementBinding;

public class AdminManagementFragment extends Fragment {

    private FragmentAdminManagementBinding binding;
    private NavController navController; // Hold NavController as a class variable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the NavController once
        navController = Navigation.findNavController(view);

        // --- OPTIMIZED CODE ---
        // Use the helper method to set up all navigation clicks
        setupNavigationClick(binding.cardManageServices, R.id.action_admin_nav_management_to_manageServicesFragment);
        setupNavigationClick(binding.cardManageBarbers, R.id.action_admin_nav_management_to_manageBarbersFragment);
        setupNavigationClick(binding.cardManagePromos, R.id.action_admin_nav_management_to_managePromosFragment);
        setupNavigationClick(binding.cardTransactionReport, R.id.action_admin_nav_management_to_transactionReportFragment);
    }

    /**
     * --- NEW HELPER METHOD ---
     * A simple helper to set a click listener that navigates to a specific action ID.
     *
     * @param view     The View to attach the click listener to (e.g., a CardView)
     * @param actionId The Navigation Action ID to navigate to
     */
    private void setupNavigationClick(@NonNull View view, int actionId) {
        view.setOnClickListener(v -> {
            // Check if NavController is still valid to prevent rare crashes
            if (navController != null && navController.getCurrentDestination() != null) {
                navController.navigate(actionId);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        navController = null; // Clear the NavController
    }
}