package com.example.beteranos.ui_admin.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentAdminManagementBinding;

public class AdminManagementFragment extends Fragment {

    private FragmentAdminManagementBinding binding;
    private NavController navController;

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

        navController = Navigation.findNavController(view);

        // --- Existing Buttons ---
        setupNavigationClick(binding.cardManageServices, R.id.action_admin_nav_management_to_manageServicesFragment);
        setupNavigationClick(binding.cardManageBarbers, R.id.action_admin_nav_management_to_manageBarbersFragment);
        setupNavigationClick(binding.cardManagePromos, R.id.action_admin_nav_management_to_managePromosFragment);

        // ⭐️ NEW: Manage Products Button ⭐️
        // (Make sure you add card_manage_products to your XML and the action to your nav graph)
        setupNavigationClick(binding.cardManageProducts, R.id.action_admin_nav_management_to_manageProductsFragment);

        setupNavigationClick(binding.cardTransactionReport, R.id.action_admin_nav_management_to_transactionReportFragment);
    }

    private void setupNavigationClick(@NonNull View view, int actionId) {
        view.setOnClickListener(v -> {
            if (navController != null && navController.getCurrentDestination() != null) {
                navController.navigate(actionId);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        navController = null;
    }
}