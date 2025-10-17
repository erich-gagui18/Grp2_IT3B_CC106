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

        // --- THIS IS THE FIX ---
        // Populate the fields with any existing data from the ViewModel
        populateFieldsFromViewModel();

        binding.btnNext.setOnClickListener(v -> {
            String firstName = binding.firstNameEditText.getText().toString().trim();
            String middleName = binding.middleNameEditText.getText().toString().trim();
            String lastName = binding.lastNameEditText.getText().toString().trim();
            String phone = binding.phoneEditText.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save the details to the ViewModel
                sharedViewModel.setCustomerDetails(firstName, middleName, lastName, phone);

                if (getParentFragment() instanceof ReservationFragment) {
                    ((ReservationFragment) getParentFragment()).navigateToServices();
                }
            }
        });

        return binding.getRoot();
    }

    private void populateFieldsFromViewModel() {
        // Get data from the ViewModel's LiveData
        String firstName = sharedViewModel.firstName.getValue();
        String middleName = sharedViewModel.middleName.getValue();
        String lastName = sharedViewModel.lastName.getValue();
        String phone = sharedViewModel.phone.getValue();

        // Set the text if the data is not null
        if (firstName != null) {
            binding.firstNameEditText.setText(firstName);
        }
        if (middleName != null) {
            binding.middleNameEditText.setText(middleName);
        }
        if (lastName != null) {
            binding.lastNameEditText.setText(lastName);
        }
        if (phone != null) {
            binding.phoneEditText.setText(phone);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}