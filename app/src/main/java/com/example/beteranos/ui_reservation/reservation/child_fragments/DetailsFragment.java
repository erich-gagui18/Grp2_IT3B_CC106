package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.databinding.FragmentDetailsBinding;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;

// --- THIS IS THE FIX ---
// The import now correctly points to the 'parent_fragments' package
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;


public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);

        // Initialize the SHARED ViewModel, scoped to the activity
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        binding.btnNext.setOnClickListener(v -> {
            String firstName = binding.editTextFirstName.getText().toString().trim();
            String lastName = binding.editTextLastName.getText().toString().trim();
            String phone = binding.editTextPhone.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save the details to the shared ViewModel
                sharedViewModel.setCustomerDetails(firstName, lastName, phone);
                Toast.makeText(getContext(), "Details captured!", Toast.LENGTH_SHORT).show();

                // Tell the parent fragment to navigate to the Services screen
                if (getParentFragment() instanceof ReservationFragment) {
                    ((ReservationFragment) getParentFragment()).navigateToServices();
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}