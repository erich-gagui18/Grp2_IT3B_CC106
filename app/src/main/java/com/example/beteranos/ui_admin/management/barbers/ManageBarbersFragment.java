package com.example.beteranos.ui_admin.management.barbers;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ManageBarbersFragment extends Fragment implements BarbersManagementAdapter.OnBarberActionListener {

    private static final String TAG = "ManageBarbersFragment";
    private FragmentManageBarbersBinding binding;
    private AdminManagementBarbersViewModel viewModel;
    private BarbersManagementAdapter adapter;
    private Uri tempImageUri = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView profileImageView;
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
            tempImageUri = null;
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
                    if (imageResult.getResultCode() != Activity.RESULT_OK) return;
                    Intent data = imageResult.getData();
                    Uri uri = (data != null) ? data.getData() : null;
                    if (uri == null) return;

                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (SecurityException e) {
                        Log.e(TAG, "Failed to take permission: " + e.getMessage());
                    }

                    tempImageUri = uri;
                    if (profileImageView != null) {
                        Glide.with(this).load(tempImageUri).centerCrop().into(profileImageView);
                    }
                });
    }

    // ⭐️ UPDATED: Logic to force 12-hour format (AM/PM) ⭐️
    private void showTimePicker(EditText targetView) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    // Create a calendar object with the selected time
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minuteOfHour);

                    // ⭐️ Format to "8:00 am" or "7:00 pm"
                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                    String formattedTime = sdf.format(selectedTime.getTime()).toLowerCase();

                    targetView.setText(formattedTime);
                },
                hour,
                minute,
                false); // ⭐️ FALSE = Show AM/PM toggle (Standard Time)

        timePickerDialog.show();
    }

    private void showAddOrEditDialog(@Nullable Barber existingBarber) {
        DialogAddBarberBinding dialogBinding = DialogAddBarberBinding.inflate(LayoutInflater.from(getContext()));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, DAY_OFF_CHOICES);
        dialogBinding.dayOffEditText.setAdapter(arrayAdapter);

        // ⭐️ Set up Time Pickers Click Listeners
        dialogBinding.startTimeEditText.setOnClickListener(v -> showTimePicker(dialogBinding.startTimeEditText));
        dialogBinding.endTimeEditText.setOnClickListener(v -> showTimePicker(dialogBinding.endTimeEditText));

        if (existingBarber != null) {
            dialogBinding.nameEditText.setText(existingBarber.getName());
            dialogBinding.specEditText.setText(existingBarber.getSpecialization());
            dialogBinding.dayOffEditText.setText(existingBarber.getDayOff(), false);

            // ⭐️ Pre-populate Times (Default to standard if null)
            dialogBinding.startTimeEditText.setText(existingBarber.getStartTime() != null ? existingBarber.getStartTime() : "9:00 am");
            dialogBinding.endTimeEditText.setText(existingBarber.getEndTime() != null ? existingBarber.getEndTime() : "5:00 pm");

            String currentImageUrl = existingBarber.getImageUrl();
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                Glide.with(this).load(currentImageUrl)
                        .placeholder(R.drawable.barber_sample)
                        .error(R.drawable.barber_sample)
                        .into(dialogBinding.barberImageView);
                tempImageUri = Uri.parse(currentImageUrl);
            } else {
                dialogBinding.barberImageView.setImageResource(R.drawable.barber_sample);
                tempImageUri = null;
            }
        } else {
            dialogBinding.dayOffEditText.setText("No day off", false);
            dialogBinding.barberImageView.setImageResource(R.drawable.barber_sample);

            // ⭐️ Default Times for new barber (AM/PM format)
            dialogBinding.startTimeEditText.setText("9:00 am");
            dialogBinding.endTimeEditText.setText("5:00 pm");
            tempImageUri = null;
        }

        dialogBinding.btnSelectImage.setOnClickListener(v -> {
            profileImageView = dialogBinding.barberImageView;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogBinding.getRoot());
        builder.setTitle(existingBarber != null ? "Edit Barber" : "Add New Barber");
        builder.setPositiveButton(existingBarber != null ? "Save" : "Add", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            profileImageView = null;
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameEditText.getText().toString().trim();
                String specialization = dialogBinding.specEditText.getText().toString().trim();
                String dayOffInput = dialogBinding.dayOffEditText.getText().toString().trim();

                // ⭐️ Get Time Inputs
                String startTime = dialogBinding.startTimeEditText.getText().toString().trim();
                String endTime = dialogBinding.endTimeEditText.getText().toString().trim();

                String finalDayOff = dayOffInput.equals("No day off") ? null : dayOffInput;
                String finalImageUrl = tempImageUri != null ? tempImageUri.toString() : (existingBarber != null ? existingBarber.getImageUrl() : null);

                boolean isValid = true;
                if (TextUtils.isEmpty(name)) { dialogBinding.nameLayout.setError("Required"); isValid = false; }
                if (TextUtils.isEmpty(specialization)) { dialogBinding.specLayout.setError("Required"); isValid = false; }

                // ⭐️ Validate Time Inputs
                if (TextUtils.isEmpty(startTime)) { dialogBinding.startTimeLayout.setError("Required"); isValid = false; }
                if (TextUtils.isEmpty(endTime)) { dialogBinding.endTimeLayout.setError("Required"); isValid = false; }

                if (isValid) {
                    if (existingBarber != null) {
                        viewModel.updateBarber(
                                existingBarber.getBarberId(),
                                name,
                                specialization,
                                finalImageUrl,
                                finalDayOff,
                                startTime, // ⭐️ Pass Time
                                endTime    // ⭐️ Pass Time
                        );
                    } else {
                        viewModel.addBarber(name, specialization, finalImageUrl, finalDayOff, startTime, endTime);
                    }
                    profileImageView = null;
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    @Override
    public void onEditClick(Barber barber) {
        String currentUrl = barber.getImageUrl();
        tempImageUri = (currentUrl != null && !currentUrl.isEmpty()) ? Uri.parse(currentUrl) : null;
        showAddOrEditDialog(barber);
    }

    @Override
    public void onDeleteClick(Barber barber) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Barber")
                .setMessage("Delete " + barber.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteBarber(barber))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleVisibilityClick(Barber barber) {
        String status = barber.isActive() ? "Hide" : "Show";
        new AlertDialog.Builder(requireContext())
                .setTitle(status + " Barber")
                .setMessage("Do you want to " + status.toLowerCase() + " this barber?")
                .setPositiveButton("Yes", (dialog, which) -> viewModel.toggleBarberVisibility(barber))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        tempImageUri = null;
        profileImageView = null;
    }
}