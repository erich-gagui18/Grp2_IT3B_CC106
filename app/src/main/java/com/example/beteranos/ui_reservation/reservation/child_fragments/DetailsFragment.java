package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
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

public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        binding.btnNext.setOnClickListener(v -> {
            String firstName = binding.firstNameEditText.getText().toString().trim();
            String middleName = binding.middleNameEditText.getText().toString().trim();
            String lastName = binding.lastNameEditText.getText().toString().trim();
            String phone = binding.phoneEditText.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            } else {
                sharedViewModel.setCustomerDetails(firstName, middleName, lastName, phone);
                if (getParentFragment() instanceof ReservationFragment) {
                    ((ReservationFragment) getParentFragment()).navigateToServices();
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- THIS IS THE INTEGRATION ---
        // Get the arguments passed from the login activity.
        Bundle activityArgs = requireActivity().getIntent().getExtras();

        // Check if the arguments exist and if the ViewModel is currently empty.
        if (activityArgs != null && sharedViewModel.firstName.getValue() == null) {
            // Pre-fill the ViewModel with data from the logged-in user.
            String fName = activityArgs.getString("FIRST_NAME");
            String mName = activityArgs.getString("MIDDLE_NAME");
            String lName = activityArgs.getString("LAST_NAME");
            String phone = activityArgs.getString("PHONE_NUMBER");
            sharedViewModel.setCustomerDetails(fName, mName, lName, phone);
        }

        // Populate the fields from the ViewModel.
        populateFieldsFromViewModel();
    }

    private void populateFieldsFromViewModel() {
        if (sharedViewModel.firstName.getValue() != null) {
            binding.firstNameEditText.setText(sharedViewModel.firstName.getValue());
        }
        if (sharedViewModel.middleName.getValue() != null) {
            binding.middleNameEditText.setText(sharedViewModel.middleName.getValue());
        }
        if (sharedViewModel.lastName.getValue() != null) {
            binding.lastNameEditText.setText(sharedViewModel.lastName.getValue());
        }
        if (sharedViewModel.phone.getValue() != null) {
            binding.phoneEditText.setText(sharedViewModel.phone.getValue());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}