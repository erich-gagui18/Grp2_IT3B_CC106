package com.example.beteranos.ui_admin.management.barbers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.beteranos.R;
import com.example.beteranos.databinding.DialogAddBarberBinding;
import com.example.beteranos.databinding.FragmentManageBarbersBinding;
import com.example.beteranos.models.Barber;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;

public class ManageBarbersFragment extends Fragment implements BarbersManagementAdapter.OnBarberActionListener {

    private static final String TAG = "ManageBarbersFragment";

    private FragmentManageBarbersBinding binding;
    // NOTE: Assuming AdminManagementBarbersViewModel has been created/updated
    private AdminManagementBarbersViewModel viewModel;
    private BarbersManagementAdapter adapter;

    // Stores the temporary URI of a newly selected image, or the existing image URL
    private Uri tempImageUri = null;

    // Launcher for selecting an image from the gallery
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // CRITICAL: Holds the reference to the ImageView inside the currently open dialog
    private ImageView profileImageView;

    // Define the fixed list of Day Off choices
    private final String[] DAY_OFF_CHOICES = new String[]{"No day off", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBarbersBinding.inflate(inflater, container, false);
        setupImagePickerLauncher();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminManagementBarbersViewModel.class);
        setupRecyclerView();

        binding.fabAddBarber.setOnClickListener(v -> {
            tempImageUri = null; // Clear image URI for a new addition
            showAddOrEditDialog(null);
        });

        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new BarbersManagementAdapter(this);
        binding.barbersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.barbersRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // ... (Observation logic remains efficient) ...
        viewModel.allBarbers.observe(getViewLifecycleOwner(), barbers -> {
            adapter.submitList(barbers);
            boolean isEmpty = (barbers == null || barbers.isEmpty());
            binding.emptyListText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.barbersRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.fabAddBarber.setEnabled(!isLoading);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearToastMessage();
            }
        });
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                imageResult -> {
                    if (imageResult.getResultCode() != Activity.RESULT_OK) {
                        return;
                    }

                    Intent data = imageResult.getData();
                    Uri uri = (data != null) ? data.getData() : null;

                    if (uri == null) {
                        Log.w(TAG, "Image selection succeeded but URI was null.");
                        return;
                    }

                    // --- URI PERSISTENCE & UI UPDATE BLOCK ---
                    try {
                        // Request persistent read access
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        tempImageUri = uri;

                        // ✅ IMMEDIATE UI UPDATE using the stored profileImageView reference
                        if (profileImageView != null) {
                            Glide.with(this)
                                    .load(tempImageUri)
                                    .centerCrop()
                                    .into(profileImageView);

                            Toast.makeText(getContext(), "Image selected. Press 'Save' to confirm.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (SecurityException e) {
                        Log.e(TAG, "Error securing persistent URI permission for " + uri, e);
                        Toast.makeText(getContext(), "Error: Failed to secure permanent image access.", Toast.LENGTH_LONG).show();
                        tempImageUri = null;
                    }
                });
    }

    private void showAddOrEditDialog(@Nullable Barber existingBarber) {
        DialogAddBarberBinding dialogBinding = DialogAddBarberBinding.inflate(LayoutInflater.from(getContext()));

        // --- DAY OFF CHOICES SETUP ---
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                DAY_OFF_CHOICES
        );
        dialogBinding.dayOffEditText.setAdapter(arrayAdapter);

        // --- PRE-POPULATE DATA ---
        if (existingBarber != null) {
            dialogBinding.nameEditText.setText(existingBarber.getName());
            dialogBinding.specEditText.setText(existingBarber.getSpecialization());

            // Pre-populate Day Off
            String currentDayOff = existingBarber.getDayOff();
            // Display the actual string from the model (will be "No day off" or the day)
            dialogBinding.dayOffEditText.setText(currentDayOff, false);

            // Pre-populate Image (Omitted for brevity, no changes here)
            String currentImageUrl = existingBarber.getImageUrl();
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(currentImageUrl)
                        .placeholder(R.drawable.barber_sample)
                        .error(R.drawable.barber_sample)
                        .into(dialogBinding.barberImageView);
                tempImageUri = Uri.parse(currentImageUrl);
            } else {
                dialogBinding.barberImageView.setImageResource(R.drawable.barber_sample);
                tempImageUri = null;
            }
        } else {
            // Set default day off for a new barber
            dialogBinding.dayOffEditText.setText("No day off", false);
            dialogBinding.barberImageView.setImageResource(R.drawable.barber_sample);
            tempImageUri = null;
        }

        // --- IMAGE PICKER SETUP --- (Omitted for brevity, no changes here)
        dialogBinding.btnSelectImage.setOnClickListener(v -> {
            profileImageView = dialogBinding.barberImageView;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogBinding.getRoot());

        String positiveButtonText = existingBarber != null ? "Save" : "Add";
        String dialogTitle = existingBarber != null ? "Edit Barber" : "Add New Barber";

        builder.setTitle(dialogTitle);
        builder.setPositiveButton(positiveButtonText, null);

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            profileImageView = null;
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();

        // Override the positive button's click listener for validation
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameEditText.getText().toString().trim();
                String specialization = dialogBinding.specEditText.getText().toString().trim();
                String dayOffInput = dialogBinding.dayOffEditText.getText().toString().trim();

                // --- ⭐️ CRITICAL CHANGE HERE ⭐️ ---
                // Convert "No day off" string to null for database storage
                String finalDayOff = dayOffInput.equals("No day off") ? null : dayOffInput;

                // Determine the final image path to save
                String finalImageUrl = tempImageUri != null ?
                        tempImageUri.toString() :
                        (existingBarber != null ? existingBarber.getImageUrl() : null); // Use null if no existing URL and no new URI


                boolean isValid = true;
                if (TextUtils.isEmpty(name)) {
                    dialogBinding.nameLayout.setError("Name is required");
                    isValid = false;
                } else {
                    dialogBinding.nameLayout.setError(null);
                }

                if (TextUtils.isEmpty(specialization)) {
                    dialogBinding.specLayout.setError("Specialization is required");
                    isValid = false;
                } else {
                    dialogBinding.specLayout.setError(null);
                }

                // Day Off Validation: Check against the raw input string
                if (TextUtils.isEmpty(dayOffInput) || dayOffInput.equals("None")) {
                    dialogBinding.dayOffLayout.setError("Day off selection is required");
                    isValid = false;
                } else {
                    dialogBinding.dayOffLayout.setError(null);
                }


                if (isValid) {
                    if (existingBarber != null) {
                        viewModel.updateBarber(
                                existingBarber.getBarberId(),
                                name,
                                specialization,
                                finalImageUrl,
                                finalDayOff // Pass the potentially NULL value
                        );
                    } else {
                        viewModel.addBarber(name, specialization, finalImageUrl, finalDayOff); // Pass the potentially NULL value
                    }

                    profileImageView = null;
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // --- Adapter Click Listeners ---

    @Override
    public void onEditClick(Barber barber) {
        // Prepare tempImageUri from the existing barber's URL for editing
        String currentUrl = barber.getImageUrl();
        if (currentUrl != null && !currentUrl.isEmpty()) {
            tempImageUri = Uri.parse(currentUrl);
        } else {
            tempImageUri = null;
        }
        showAddOrEditDialog(barber);
    }

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
        // Crucial for memory management: clear all references tied to the view/dialog
        binding = null;
        tempImageUri = null;
        profileImageView = null;
    }
}