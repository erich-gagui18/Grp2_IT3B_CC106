package com.example.beteranos.ui_reservation.reservation.parent_fragments;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Locale;

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

        // Observe finalPrice to trigger all price updates
        sharedViewModel.finalPrice.observe(getViewLifecycleOwner(), finalPrice -> {
            updatePriceViews();
        });

        binding.btnDone.setOnClickListener(v -> {
            sharedViewModel.clearReservationDetails();

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_reservation);
            navController.popBackStack(R.id.navigation_home, false);
        });
    }

    private void populateDetails() {
        // --- CUSTOMER DETAILS (Ensuring we use the correct `phoneNumber` LiveData if available) ---
        String firstName = sharedViewModel.firstName.getValue();
        String middleName = sharedViewModel.middleName.getValue();
        String lastName = sharedViewModel.lastName.getValue();
        // Assuming your ViewModel uses 'phoneNumber' now based on previous discussions.
        String phone = sharedViewModel.phone.getValue();

        String fullName = (firstName != null ? firstName : "") +
                (middleName != null && !middleName.isEmpty() ? " " + middleName : "") +
                (lastName != null ? " " + lastName : "");
        binding.tvCustomerName.setText(fullName.trim());
        binding.tvCustomerPhone.setText(phone != null ? phone : "");

        // --- SCHEDULE & BARBER (Omitted for brevity, assumed correct) ---
        String date = sharedViewModel.selectedDate.getValue();
        String time = sharedViewModel.selectedTime.getValue();
        if (date != null && time != null) {
            binding.tvSchedule.setText(date + " at " + time);
        }

        if (sharedViewModel.selectedBarber.getValue() != null) {
            binding.tvBarberName.setText(sharedViewModel.selectedBarber.getValue().getName());
        }

        // --- SERVICES LIST (Omitted for brevity, assumed correct) ---
        List<Service> services = sharedViewModel.selectedServices.getValue();
        if (services != null && !services.isEmpty()) {
            StringBuilder servicesText = new StringBuilder();
            for (Service service : services) {
                servicesText.append("- ")
                        .append(service.getServiceName())
                        .append(String.format(Locale.US, " (₱%.2f)", service.getPrice()))
                        .append("\n");
            }
            binding.tvServicesList.setText(servicesText.toString().trim());
        }

        // --- PROMO (Omitted for brevity, assumed correct) ---
        Promo selectedPromo = sharedViewModel.selectedPromo.getValue();
        if (selectedPromo != null) {
            binding.tvPromoName.setText(String.format(Locale.US, "%s (%d%% Off)", selectedPromo.getPromoName(), selectedPromo.getDiscountPercentage()));
        } else {
            binding.tvPromoName.setText("No promo selected");
        }

        updatePriceViews();
    }

    // Helper method to update the price display
    private void updatePriceViews() {
        Double total = sharedViewModel.totalPrice.getValue();
        Double discount = sharedViewModel.promoDiscount.getValue();
        Double finalAmo = sharedViewModel.finalPrice.getValue();
        // ⭐️ GET DOWN PAYMENT ⭐️
        Double downPayment = sharedViewModel.downPaymentAmount.getValue();

        double totalAmount = total != null ? total : 0.0;
        double discountAmount = discount != null ? discount : 0.0;
        double finalAmount = finalAmo != null ? finalAmo : 0.0;
        double downPaymentAmount = downPayment != null ? downPayment : 0.0;

        // Calculate the remaining balance
        double remainingBalance = finalAmount - downPaymentAmount;

        try {
            // Price Summary
            binding.tvTotalAmount.setText(String.format(Locale.US, "₱%.2f", totalAmount));
            binding.tvDiscount.setText(String.format(Locale.US, "-₱%.2f", discountAmount));
            binding.tvFinalPrice.setText(String.format(Locale.US, "₱%.2f", finalAmount));

            // ⭐️ NEW Payment Details ⭐️
            // Display down payment as a negative value (money subtracted/paid)
            binding.tvDownPaymentPaid.setText(String.format(Locale.US, "-₱%.2f", downPaymentAmount));
            // Display remaining balance
            binding.tvRemainingBalance.setText(String.format(Locale.US, "₱%.2f", remainingBalance));

            // Check for discountLayout and set visibility
            if (binding.discountLayout != null) {
                binding.discountLayout.setVisibility(discountAmount > 0 ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e("ConfirmationFrag", "Error accessing price TextViews in XML. Check IDs exist: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}