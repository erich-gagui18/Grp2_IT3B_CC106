package com.example.beteranos.ui_reservation.reservation.parent_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentReservationConfirmationBinding;
import com.example.beteranos.models.Promo;
import com.example.beteranos.models.Service;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;

import java.util.List;

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
            sharedViewModel.clearReservationDetails();

            // --- THIS IS THE FIX ---
            // Find the NavController and pop back to the home screen defined in mobile_navigation.xml
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_reservation);
            navController.popBackStack(R.id.navigation_home, false);
        });
    }

    private void populateDetails() {
        // ... (your existing populateDetails method remains the same)
        String firstName = sharedViewModel.firstName.getValue();
        String middleName = sharedViewModel.middleName.getValue();
        String lastName = sharedViewModel.lastName.getValue();
        String phone = sharedViewModel.phone.getValue();

        String fullName = (firstName != null ? firstName : "") +
                (middleName != null && !middleName.isEmpty() ? " " + middleName : "") +
                (lastName != null ? " " + lastName : "");
        binding.tvCustomerName.setText(fullName.trim());
        binding.tvCustomerPhone.setText(phone != null ? phone : "");

        String date = sharedViewModel.selectedDate.getValue();
        String time = sharedViewModel.selectedTime.getValue();
        if (date != null && time != null) {
            binding.tvSchedule.setText(date + " at " + time);
        }

        if (sharedViewModel.selectedBarber.getValue() != null) {
            binding.tvBarberName.setText(sharedViewModel.selectedBarber.getValue().getName());
        }

        List<Service> services = sharedViewModel.selectedServices.getValue();
        if (services != null && !services.isEmpty()) {
            StringBuilder servicesText = new StringBuilder();
            for (Service service : services) {
                servicesText.append("- ")
                        .append(service.getName())
                        .append(String.format(" (₱%.2f)", service.getPrice()))
                        .append("\n");
            }
            binding.tvServicesList.setText(servicesText.toString().trim());
        }

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