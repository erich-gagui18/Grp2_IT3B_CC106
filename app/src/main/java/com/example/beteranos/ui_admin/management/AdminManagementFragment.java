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

        final NavController navController = Navigation.findNavController(view);

        // Set click listener for Manage Services
        binding.cardManageServices.setOnClickListener(v -> {
            navController.navigate(R.id.action_admin_nav_management_to_manageServicesFragment);
        });

        // Set click listener for Manage Barbers
        binding.cardManageBarbers.setOnClickListener(v -> {
            navController.navigate(R.id.action_admin_nav_management_to_manageBarbersFragment);
        });

        // Set click listener for Manage Promos
        binding.cardManagePromos.setOnClickListener(v -> {
            navController.navigate(R.id.action_admin_nav_management_to_managePromosFragment);
        });

        // --- ADD THIS CLICK LISTENER ---
        binding.cardTransactionReport.setOnClickListener(v -> {
            navController.navigate(R.id.action_admin_nav_management_to_transactionReportFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}