package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.databinding.FragmentDetailsBinding;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import static android.content.Context.MODE_PRIVATE;

public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private SharedReservationViewModel sharedViewModel;
    private boolean isGuest = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        // --- THIS IS THE FIX ---
        // 1. Check if the user is a guest.
        int customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);
        isGuest = (customerId == -1);

        // 2. If it's a guest, make the email field visible.
        if (isGuest) {
            binding.emailLayout.setVisibility(View.VISIBLE);
        }

        populateFields();

        binding.btnNext.setOnClickListener(v -> handleNextClick());
    }

    private void handleNextClick() {
        String fName = binding.firstNameEditText.getText().toString().trim();
        String mName = binding.middleNameEditText.getText().toString().trim();
        String lName = binding.lastNameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String email;

        // 3. Get the email from the correct source.
        if (isGuest) {
            email = binding.emailEditText.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // For logged-in users, the email is already in the ViewModel.
            email = sharedViewModel.email.getValue();
        }

        if (fName.isEmpty() || lName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedViewModel.setCustomerDetails(fName, mName, lName, phone, email);

        if (getParentFragment() instanceof ReservationFragment) {
            ((ReservationFragment) getParentFragment()).navigateToServices();
        }
    }

    private void populateFields() {
        // Populate fields from ViewModel if they exist (e.g., when navigating back)
        binding.firstNameEditText.setText(sharedViewModel.firstName.getValue());
        binding.middleNameEditText.setText(sharedViewModel.middleName.getValue());
        binding.lastNameEditText.setText(sharedViewModel.lastName.getValue());
        binding.phoneEditText.setText(sharedViewModel.phone.getValue());
        if (isGuest) {
            binding.emailEditText.setText(sharedViewModel.email.getValue());
        }

        // If ViewModel is empty, populate from logged-in session
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (sharedViewModel.firstName.getValue() == null && !isGuest) {
            binding.firstNameEditText.setText(prefs.getString("first_name", ""));
            binding.middleNameEditText.setText(prefs.getString("middle_name", ""));
            binding.lastNameEditText.setText(prefs.getString("last_name", ""));
            binding.phoneEditText.setText(prefs.getString("phone_number", ""));
            // For logged-in users, set the email directly in the ViewModel
            sharedViewModel.email.setValue(prefs.getString("email", ""));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}