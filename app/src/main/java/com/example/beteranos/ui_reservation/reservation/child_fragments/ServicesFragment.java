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
import com.example.beteranos.databinding.FragmentServicesBinding;
import com.example.beteranos.models.Service;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import java.util.List;

public class ServicesFragment extends Fragment {

    private FragmentServicesBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentServicesBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        sharedViewModel.allServices.observe(getViewLifecycleOwner(), services -> {
            if (services != null && !services.isEmpty()) {
                populateServicesList(services);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            List<Service> selected = sharedViewModel.selectedServices.getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one service", Toast.LENGTH_SHORT).show();
            } else {
                if (getParentFragment() instanceof ReservationFragment) {
                    ((ReservationFragment) getParentFragment()).navigateToBarbers();
                }
            }
        });

        return binding.getRoot();
    }

    private void populateServicesList(List<Service> services) {
        binding.servicesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Service service : services) {
            // Inflate the custom layout for each service
            View serviceView = inflater.inflate(R.layout.item_selectable_service, binding.servicesContainer, false);

            TextView nameText = serviceView.findViewById(R.id.service_name_text);
            ImageView checkMark = serviceView.findViewById(R.id.check_mark_icon);

            nameText.setText(String.format("%s - ₱%.2f", service.getName(), service.getPrice()));

            // Restore the selected state
            List<Service> selectedList = sharedViewModel.selectedServices.getValue();
            boolean isSelected = selectedList != null && selectedList.stream().anyMatch(s -> s.getId() == service.getId());
            checkMark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Handle clicks on the entire item
            serviceView.setOnClickListener(v -> {
                boolean currentlySelected = checkMark.getVisibility() == View.VISIBLE;
                if (currentlySelected) {
                    checkMark.setVisibility(View.GONE);
                    sharedViewModel.removeService(service);
                } else {
                    checkMark.setVisibility(View.VISIBLE);
                    sharedViewModel.addService(service);
                }
            });
            binding.servicesContainer.addView(serviceView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}