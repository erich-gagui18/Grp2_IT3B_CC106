package com.example.beteranos.ui_reservation.reservation.parent_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentReservationConfirmationBinding;
import com.example.beteranos.models.Promo;
import com.example.beteranos.models.Service;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;

public class ReservationConfirmationFragment extends Fragment {

    private FragmentReservationConfirmationBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReservationConfirmationBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateDetails();

        binding.btnDone.setOnClickListener(v -> {
            // Clear the ViewModel for the next reservation
            sharedViewModel.clearReservationDetails();
            // Clear the back stack and return to the reservation home
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }

    private void populateDetails() {
        // Set Customer Name and Phone
        String fullName = sharedViewModel.firstName.getValue() + " " + sharedViewModel.lastName.getValue();
        binding.tvCustomerName.setText(fullName);
        binding.tvCustomerPhone.setText(sharedViewModel.phone.getValue());

        // Set Schedule
        String schedule = sharedViewModel.selectedDate.getValue() + " at " + sharedViewModel.selectedTime.getValue();
        binding.tvSchedule.setText(schedule);

        // Set Barber
        if (sharedViewModel.selectedBarber.getValue() != null) {
            binding.tvBarberName.setText(sharedViewModel.selectedBarber.getValue().getName());
        }

        // Set Services
        if (sharedViewModel.selectedServices.getValue() != null) {
            StringBuilder servicesText = new StringBuilder();
            for (Service service : sharedViewModel.selectedServices.getValue()) {
                servicesText.append("- ")
                        .append(service.getName())
                        .append(String.format(" (₱%.2f)", service.getPrice()))
                        .append("\n");
            }
            binding.tvServicesList.setText(servicesText.toString().trim());
        }

        // Set Promo
        Promo selectedPromo = sharedViewModel.selectedPromo.getValue();
        if (selectedPromo != null) {
            binding.tvPromoName.setText(selectedPromo.getName());
        } else {
            binding.tvPromoName.setText("No promo selected");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}