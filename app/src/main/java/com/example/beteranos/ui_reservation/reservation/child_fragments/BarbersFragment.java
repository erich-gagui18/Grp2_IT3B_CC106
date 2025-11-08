package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentBarbersBinding;
import com.example.beteranos.models.Barber;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import java.util.List;

public class BarbersFragment extends Fragment {

    private FragmentBarbersBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBarbersBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        sharedViewModel.allBarbers.observe(getViewLifecycleOwner(), this::populateBarbersList);

        binding.btnNext.setOnClickListener(v -> {
            if (sharedViewModel.selectedBarber.getValue() == null) {
                Toast.makeText(getContext(), "Please select a barber", Toast.LENGTH_SHORT).show();
            } else {
                if (getParentFragment() instanceof ReservationFragment) {
                    // Navigate to the next step, e.g., Promo
                    ((ReservationFragment) getParentFragment()).navigateToPromo();
                }
            }
        });

        return binding.getRoot();
    }

    private void populateBarbersList(List<Barber> barbers) {
        binding.barbersContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Barber barber : barbers) {
            View barberView = inflater.inflate(R.layout.item_selectable_barber, binding.barbersContainer, false);

            TextView nameText = barberView.findViewById(R.id.barber_name_text);
            // 🔑 NEW: Find TextView for Specialization
            TextView specializationText = barberView.findViewById(R.id.barber_specialization_text);
            // 🔑 NEW: Find TextView for Day Off
            TextView dayOffText = barberView.findViewById(R.id.barber_day_off_text);

            ImageView checkMark = barberView.findViewById(R.id.check_mark_icon);

            // 🔑 POPULATE DATA
            nameText.setText(barber.getName());

            // Set Specialization
            specializationText.setText(barber.getSpecialization());

            // Set Day Off with special formatting if needed
            String dayOff = barber.getDayOff();
            if (dayOff != null && !dayOff.isEmpty() && !dayOff.equalsIgnoreCase("none")) {
                dayOffText.setText("Day Off: " + dayOff);
            } else {
                dayOffText.setText("Available today");
                dayOffText.setTextColor(getResources().getColor(R.color.status_scheduled, null)); // Assuming you have a green color defined
            }
            // ------------------

            // Observe the single selected barber to update the checkmark
            sharedViewModel.selectedBarber.observe(getViewLifecycleOwner(), selected -> {
                boolean isThisOneSelected = selected != null && selected.getBarberId() == barber.getBarberId();
                checkMark.setVisibility(isThisOneSelected ? View.VISIBLE : View.GONE);
            });

            barberView.setOnClickListener(v -> {
                // Set this barber as the single selected one in the ViewModel
                sharedViewModel.selectedBarber.setValue(barber);
            });
            binding.barbersContainer.addView(barberView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}