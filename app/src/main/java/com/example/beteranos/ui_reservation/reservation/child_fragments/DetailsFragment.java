package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.content.DialogInterface; // Import DialogInterface
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import Button
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R; // Ensure R is imported
import com.example.beteranos.databinding.FragmentDetailsBinding;
import com.example.beteranos.databinding.DialogSetPasswordBinding; // Import dialog binding
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import com.google.android.material.textfield.TextInputEditText; // Import TextInputEditText

import static android.content.Context.MODE_PRIVATE;

public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private SharedReservationViewModel sharedViewModel;
    private boolean isGuest = false;
    private AlertDialog passwordDialog; // Keep reference to dismiss later


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        // Initialize ViewModel in onCreateView or onViewCreated, ensuring it's tied to the Activity lifecycle
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Determine if the user is a guest based on CUSTOMER_ID from the Activity's Intent
        int customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);
        isGuest = (customerId == -1);

        // Show email field only for guests
        binding.emailLayout.setVisibility(isGuest ? View.VISIBLE : View.GONE);

        populateFields(); // Populate fields from ViewModel or SharedPreferences
        setupObservers(); // Setup observers for ViewModel signals (password prompt, navigation, errors)

        // Set listener for the 'Next' button
        binding.btnNext.setOnClickListener(v -> handleNextClick());
    }

    // Handles click on the 'Next' button
    private void handleNextClick() {
        // Get input values
        String fName = binding.firstNameEditText.getText().toString().trim();
        String mName = binding.middleNameEditText.getText().toString().trim();
        String lName = binding.lastNameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim(); // Changed ID reference
        String email = ""; // Initialize email

        // Basic Validation: Check required fields
        if (fName.isEmpty() || lName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Please fill First Name, Last Name, and Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guest specific validation: Check email
        if (isGuest) {
            email = binding.emailEditText.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // For logged-in users, get email from ViewModel (should have been set on login/activity start)
            email = sharedViewModel.email.getValue();
            if (email == null || email.isEmpty()){
                // Fallback: Try getting from prefs if ViewModel somehow lost it
                SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
                email = prefs.getString("email", "");
                if (email.isEmpty()){
                    Toast.makeText(getContext(), "Error: User email not found.", Toast.LENGTH_SHORT).show();
                    Log.e("DetailsFragment", "Logged in user email is null or empty in ViewModel and Prefs.");
                    return;
                }
                sharedViewModel.email.setValue(email); // Re-set in ViewModel
            }
        }

        Log.d("DetailsFragment", "handleNextClick: Calling checkAndProcessDetails. isGuest=" + isGuest);
        // Disable button to prevent double clicks during processing
        binding.btnNext.setEnabled(false);
        // Trigger ViewModel to check customer and decide next step (show dialog or navigate)
        sharedViewModel.checkAndProcessDetails(fName, mName, lName, phone, email, isGuest);
    }

    // Sets up observers for LiveData signals from the SharedReservationViewModel
    private void setupObservers() {
        // Observer to show the password dialog when prompted
        sharedViewModel.promptSetPassword.observe(getViewLifecycleOwner(), customerId -> {
            // Check if fragment is still added to prevent state loss exceptions
            Log.d("DetailsFragment", "promptSetPassword Observer Fired! CustomerID: " + customerId);
            if (customerId != null && isAdded()) {
                showSetPasswordDialog(customerId);
                sharedViewModel.promptSetPassword.setValue(null); // Reset signal
            }
            // Re-enable button regardless, as the prompt is just an option
            if (binding != null) binding.btnNext.setEnabled(true);
        });

        // Observer for the result of saving the password
        sharedViewModel.passwordUpdateStatus.observe(getViewLifecycleOwner(), success -> {
            if (success != null && isAdded()) {
                // Dismiss the dialog if it's showing
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                // Show feedback toast
                if (success) {
                    Toast.makeText(getContext(), "Password saved successfully!", Toast.LENGTH_SHORT).show();
                    // Optional: You might want to update some UI element or internal state here
                } else {
                    Toast.makeText(getContext(), "Failed to save password. Please try again later.", Toast.LENGTH_LONG).show();
                }
                sharedViewModel.passwordUpdateStatus.setValue(null); // Reset signal
            }
            // Ensure button is enabled after password attempt finishes
            if (binding != null) binding.btnNext.setEnabled(true);
        });

        // Observer for errors during customer check/creation
        sharedViewModel.customerCheckError.observe(getViewLifecycleOwner(), error -> {
            if (error != null && isAdded()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                sharedViewModel.customerCheckError.setValue(null); // Reset signal
                if (binding != null) binding.btnNext.setEnabled(true); // Re-enable button on error
            }
        });

        // Observer to trigger navigation to the next step (ServicesFragment)
        sharedViewModel.navigateToServicesSignal.observe(getViewLifecycleOwner(), navigate -> {
            if (navigate != null && navigate && isAdded()) {
                navigateToServices(); // Call the navigation method
                sharedViewModel.navigateToServicesSignal.setValue(null); // Reset signal
            }
            // Ensure button is re-enabled AFTER navigation attempt or if signal is false/null
            if (binding != null) binding.btnNext.setEnabled(true);
        });
    }

    // --- Builds and shows the AlertDialog for setting a password ---
    // Inside DetailsFragment.java

    private void showSetPasswordDialog(int customerId) {
        DialogSetPasswordBinding dialogBinding = DialogSetPasswordBinding.inflate(getLayoutInflater());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set Password for Guest")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String password = dialogBinding.passwordEditText.getText().toString().trim();
                    String confirmPassword = dialogBinding.confirmPasswordEditText.getText().toString().trim();

                    if (password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!password.equals(confirmPassword)) {
                        Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("DetailsFragment", "Saving guest password via ViewModel...");
                    sharedViewModel.updateGuestPassword(customerId, password); // ✅ delegate to ViewModel
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        passwordDialog = builder.create();
        passwordDialog.show();
    }



    // Populates input fields based on ViewModel or SharedPreferences data
    private void populateFields() {
        // Try populating from ViewModel first (handles screen rotation, back navigation)
        binding.firstNameEditText.setText(sharedViewModel.firstName.getValue());
        binding.middleNameEditText.setText(sharedViewModel.middleName.getValue());
        binding.lastNameEditText.setText(sharedViewModel.lastName.getValue());
        binding.phoneEditText.setText(sharedViewModel.phone.getValue()); // Use correct ID
        if (isGuest) {
            binding.emailEditText.setText(sharedViewModel.email.getValue());
        }

        // If ViewModel is empty AND user is logged in (initial load), populate from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (!isGuest && sharedViewModel.firstName.getValue() == null) {
            binding.firstNameEditText.setText(prefs.getString("first_name", ""));
            binding.middleNameEditText.setText(prefs.getString("middle_name", ""));
            binding.lastNameEditText.setText(prefs.getString("last_name", ""));
            binding.phoneEditText.setText(prefs.getString("phone_number", "")); // Use correct ID
            // Set email in ViewModel if not already set (important for logged-in users)
            if (sharedViewModel.email.getValue() == null) {
                sharedViewModel.email.setValue(prefs.getString("email", ""));
            }
        }
    }

    // Navigates to the next step in the reservation flow (ServicesFragment)
    private void navigateToServices() {
        if (getParentFragment() instanceof ReservationFragment) {
            ((ReservationFragment) getParentFragment()).navigateToServices(); // Ensure this method exists and works
        } else {
            Log.e("DetailsFragment", "Parent fragment is not an instance of ReservationFragment. Cannot navigate.");
            // Show error to user or handle differently
            Toast.makeText(getContext(), "Navigation error occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dismiss the dialog if it's showing to prevent leaks
        if (passwordDialog != null && passwordDialog.isShowing()) {
            passwordDialog.dismiss();
        }
        passwordDialog = null; // Clear reference
        binding = null; // Important for ViewBinding
    }
}