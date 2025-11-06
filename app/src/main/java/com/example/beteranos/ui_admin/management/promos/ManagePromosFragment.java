package com.example.beteranos.ui_admin.management.promos;

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

import com.example.beteranos.databinding.DialogAddPromoBinding;
import com.example.beteranos.databinding.FragmentManagePromosBinding;
import com.example.beteranos.models.Promo;


public class ManagePromosFragment extends Fragment implements PromosManagementAdapter.OnPromoActionListener {

    private FragmentManagePromosBinding binding;
    private AdminManagementPromosViewModel viewModel;
    private PromosManagementAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagePromosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO #3: Create ViewModel
        viewModel = new ViewModelProvider(this).get(AdminManagementPromosViewModel.class);

        // TODO #1 & #2: Set up RecyclerView and Adapter
        setupRecyclerView();

        // TODO #5: Add "Add New Promo" button listener
        binding.fabAddPromo.setOnClickListener(v -> {
            // TODO #6: Show dialog
            showAddOrEditDialog(null);
        });

        // TODO #4: Fetch and observe promos
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new PromosManagementAdapter(this);
        binding.promosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.promosRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.allPromos.observe(getViewLifecycleOwner(), promos -> {
            adapter.submitList(promos);
            if (promos == null || promos.isEmpty()) {
                binding.emptyListText.setVisibility(View.VISIBLE);
                binding.promosRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyListText.setVisibility(View.GONE);
                binding.promosRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.fabAddPromo.setEnabled(!isLoading);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearToastMessage(); // Reset after showing
            }
        });
    }

    // TODO #6, #7, #8: Handle Add/Edit dialog
    private void showAddOrEditDialog(@Nullable Promo existingPromo) {
        DialogAddPromoBinding dialogBinding = DialogAddPromoBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogBinding.getRoot());

        if (existingPromo != null) {
            builder.setTitle("Edit Promo");
            dialogBinding.nameEditText.setText(existingPromo.getPromoName());
            dialogBinding.descEditText.setText(existingPromo.getDescription());
            dialogBinding.discountEditText.setText(String.valueOf(existingPromo.getDiscountPercentage()));
            builder.setPositiveButton("Save", null);
        } else {
            builder.setTitle("Add New Promo");
            builder.setPositiveButton("Add", null);
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameEditText.getText().toString().trim();
                String description = dialogBinding.descEditText.getText().toString().trim();
                String discountStr = dialogBinding.discountEditText.getText().toString().trim();

                boolean isValid = true;
                if (TextUtils.isEmpty(name)) {
                    dialogBinding.nameLayout.setError("Name is required");
                    isValid = false;
                } else {
                    dialogBinding.nameLayout.setError(null);
                }

                if (TextUtils.isEmpty(discountStr)) {
                    dialogBinding.discountLayout.setError("Discount is required");
                    isValid = false;
                } else {
                    dialogBinding.discountLayout.setError(null);
                }

                if (!isValid) return; // Stop if validation failed

                int discount = 0;
                try {
                    discount = Integer.parseInt(discountStr);
                    if (discount < 0 || discount > 99) {
                        dialogBinding.discountLayout.setError("Must be between 0-99");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    dialogBinding.discountLayout.setError("Invalid number");
                    isValid = false;
                }

                if (isValid) {
                    if (existingPromo != null) {
                        viewModel.updatePromo(existingPromo.getPromoId(), name, description, discount);
                    } else {
                        viewModel.addPromo(name, description, discount);
                    }
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // TODO #8: Handle Edit click
    @Override
    public void onEditClick(Promo promo) {
        showAddOrEditDialog(promo);
    }

    // TODO #8: Handle Delete click
    @Override
    public void onDeleteClick(Promo promo) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Promo")
                .setMessage("Are you sure you want to delete '" + promo.getPromoName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deletePromo(promo);
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