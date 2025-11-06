package com.example.beteranos.ui_admin.management.barbers;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beteranos.R;
import com.example.beteranos.databinding.DialogAddBarberBinding;
import com.example.beteranos.databinding.FragmentManageBarbersBinding;
import com.example.beteranos.models.Barber;
import com.google.android.material.textfield.TextInputLayout;

public class ManageBarbersFragment extends Fragment implements BarbersManagementAdapter.OnBarberActionListener {

    private FragmentManageBarbersBinding binding;
    private AdminManagementBarbersViewModel viewModel;
    private BarbersManagementAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBarbersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO #3: Create ViewModel
        viewModel = new ViewModelProvider(this).get(AdminManagementBarbersViewModel.class);

        // TODO #1 & #2: Set up RecyclerView and Adapter
        setupRecyclerView();

        // TODO #5: Add "Add New Barber" button listener
        binding.fabAddBarber.setOnClickListener(v -> {
            // TODO #6: Show dialog
            showAddOrEditDialog(null);
        });

        // TODO #4: Fetch and observe barbers
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new BarbersManagementAdapter(this);
        binding.barbersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.barbersRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.allBarbers.observe(getViewLifecycleOwner(), barbers -> {
            adapter.submitList(barbers);
            if (barbers == null || barbers.isEmpty()) {
                binding.emptyListText.setVisibility(View.VISIBLE);
                binding.barbersRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyListText.setVisibility(View.GONE);
                binding.barbersRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.fabAddBarber.setEnabled(!isLoading);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearToastMessage(); // Reset after showing
            }
        });
    }

    // TODO #6, #7, #8: Handle Add/Edit dialog
    private void showAddOrEditDialog(@Nullable Barber existingBarber) {
        // Inflate the custom dialog layout
        DialogAddBarberBinding dialogBinding = DialogAddBarberBinding.inflate(LayoutInflater.from(getContext()));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogBinding.getRoot());

        // Check if this is an "Add" or "Edit" dialog
        if (existingBarber != null) {
            builder.setTitle("Edit Barber");
            dialogBinding.nameEditText.setText(existingBarber.getName());
            dialogBinding.specEditText.setText(existingBarber.getSpecialization());
            builder.setPositiveButton("Save", null); // Set to null, we will override later
        } else {
            builder.setTitle("Add New Barber");
            builder.setPositiveButton("Add", null); // Set to null, we will override later
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Override the positive button's click listener for validation
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameEditText.getText().toString().trim();
                String specialization = dialogBinding.specEditText.getText().toString().trim();

                boolean isValid = true;
                if (TextUtils.isEmpty(name)) {
                    dialogBinding.nameLayout.setError("Name is required");
                    isValid = false;
                } else {
                    dialogBinding.nameLayout.setError(null);
                }

                if (isValid) {
                    if (existingBarber != null) {
                        // This is an UPDATE
                        viewModel.updateBarber(existingBarber.getBarberId(), name, specialization);
                    } else {
                        // This is an ADD
                        viewModel.addBarber(name, specialization);
                    }
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // TODO #8: Handle Edit click from adapter
    @Override
    public void onEditClick(Barber barber) {
        showAddOrEditDialog(barber);
    }

    // TODO #8: Handle Delete click from adapter
    @Override
    public void onDeleteClick(Barber barber) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Barber")
                .setMessage("Are you sure you want to delete " + barber.getName() + "? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteBarber(barber);
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