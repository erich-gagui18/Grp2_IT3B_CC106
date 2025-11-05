package com.example.beteranos.ui_admin.management.services;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.databinding.DialogAddServiceBinding;
import com.example.beteranos.databinding.FragmentManageServicesBinding;
import com.example.beteranos.models.Service;

public class ManageServicesFragment extends Fragment implements ServicesManagementAdapter.OnServiceActionListener {

    private FragmentManageServicesBinding binding;
    private AdminManagementServicesViewModel viewModel;
    private ServicesManagementAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageServicesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO #3: Create ViewModel
        viewModel = new ViewModelProvider(this).get(AdminManagementServicesViewModel.class);

        // TODO #1 & #2: Set up RecyclerView and Adapter
        setupRecyclerView();

        // TODO #5: Add "Add New Service" button listener
        binding.fabAddService.setOnClickListener(v -> {
            // TODO #6: Show dialog
            showAddOrEditDialog(null);
        });

        // TODO #4: Fetch and observe services
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ServicesManagementAdapter(this);
        binding.servicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.servicesRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.allServices.observe(getViewLifecycleOwner(), services -> {
            adapter.submitList(services);
            if (services == null || services.isEmpty()) {
                binding.emptyListText.setVisibility(View.VISIBLE);
                binding.servicesRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyListText.setVisibility(View.GONE);
                binding.servicesRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.fabAddService.setEnabled(!isLoading);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearToastMessage(); // Reset after showing
            }
        });
    }

    // TODO #6, #7, #8: Handle Add/Edit dialog
    private void showAddOrEditDialog(@Nullable Service existingService) {
        DialogAddServiceBinding dialogBinding = DialogAddServiceBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogBinding.getRoot());

        if (existingService != null) {
            builder.setTitle("Edit Service");
            dialogBinding.nameEditText.setText(existingService.getServiceName());
            dialogBinding.priceEditText.setText(String.format(java.util.Locale.US, "%.2f", existingService.getPrice()));
            builder.setPositiveButton("Save", null);
        } else {
            builder.setTitle("Add New Service");
            builder.setPositiveButton("Add", null);
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameEditText.getText().toString().trim();
                String priceStr = dialogBinding.priceEditText.getText().toString().trim();

                boolean isValid = true;
                if (TextUtils.isEmpty(name)) {
                    dialogBinding.nameLayout.setError("Name is required");
                    isValid = false;
                } else {
                    dialogBinding.nameLayout.setError(null);
                }

                if (TextUtils.isEmpty(priceStr)) {
                    dialogBinding.priceLayout.setError("Price is required");
                    isValid = false;
                } else {
                    dialogBinding.priceLayout.setError(null);
                }

                if (!isValid) return; // Stop if validation failed

                double price = 0.0;
                try {
                    price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        dialogBinding.priceLayout.setError("Price must be positive");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    dialogBinding.priceLayout.setError("Invalid number");
                    isValid = false;
                }

                if (isValid) {
                    if (existingService != null) {
                        viewModel.updateService(existingService.getServiceId(), name, price);
                    } else {
                        viewModel.addService(name, price);
                    }
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // TODO #8: Handle Edit click
    @Override
    public void onEditClick(Service service) {
        showAddOrEditDialog(service);
    }

    // TODO #8: Handle Delete click
    @Override
    public void onDeleteClick(Service service) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete '" + service.getServiceName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteService(service);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}