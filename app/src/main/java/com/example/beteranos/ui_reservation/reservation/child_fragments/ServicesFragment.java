package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
import android.text.Editable; // Import
import android.text.TextWatcher; // Import
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentServicesBinding;
import com.example.beteranos.models.Service;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ServicesFragment extends Fragment {

    private FragmentServicesBinding binding;
    private SharedReservationViewModel sharedViewModel;

    private final String[] HAIRCUT_OPTIONS = {"Buzz Cut", "Undercut Fade", "Modern Mullet", "French Crop", "Taper", "Others"};
    private ArrayAdapter<String> haircutAdapter;
    private static final String HAIRCUT_SERVICE_NAME = "Haircut";

    private static final String LOCATION_BARBERSHOP = "Barbershop";
    private static final String LOCATION_HOME_SERVICE = "Home Service";
    private final List<String> LOCATION_OPTIONS = Arrays.asList(LOCATION_BARBERSHOP, LOCATION_HOME_SERVICE);

    private View barbershopLocationView = null;
    private View homeServiceLocationView = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentServicesBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        populateLocationSelector();
        setupHaircutDropdown();
        setupAddressInputListener(); // ⭐️ NEW: Setup listener for address text

        // UI Refresh Observer
        sharedViewModel.allServices.observe(getViewLifecycleOwner(), services -> {
            if (services != null && !services.isEmpty()) {
                redrawServicesList();
            }
        });

        sharedViewModel.selectedServices.observe(getViewLifecycleOwner(), selected -> {
            redrawServicesList();
        });

        // Location Observer
        sharedViewModel.serviceLocation.observe(getViewLifecycleOwner(), location -> {
            Log.d("ServicesFragment", "Observed location change: " + location);
            redrawServicesList();
            updateLocationCheckmarks(location);

            // ⭐️ NEW: Show/Hide Address Field based on location ⭐️
            if (LOCATION_HOME_SERVICE.equals(location)) {
                binding.addressInputLayout.setVisibility(View.VISIBLE);
                // Pre-fill if value exists in ViewModel
                if (sharedViewModel.homeServiceAddress.getValue() != null) {
                    binding.addressEditText.setText(sharedViewModel.homeServiceAddress.getValue());
                }
            } else {
                binding.addressInputLayout.setVisibility(View.GONE);
                // Optional: Clear address in ViewModel if switching back to Barbershop?
                // sharedViewModel.homeServiceAddress.setValue(null);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            String location = sharedViewModel.serviceLocation.getValue();

            // 1. Validate Location Selection
            if (location == null || location.isEmpty()) {
                Toast.makeText(getContext(), "Please select a service location", Toast.LENGTH_SHORT).show();
                return;
            }

            // ⭐️ NEW: Validate Address if Home Service is selected ⭐️
            if (LOCATION_HOME_SERVICE.equals(location)) {
                String address = binding.addressEditText.getText().toString().trim();
                if (address.isEmpty()) {
                    binding.addressInputLayout.setError("Address is required for Home Service");
                    return; // Stop navigation
                } else {
                    binding.addressInputLayout.setError(null); // Clear error
                    sharedViewModel.homeServiceAddress.setValue(address); // Save to ViewModel
                }
            }

            // 2. Validate Services Selection
            List<Service> selected = sharedViewModel.selectedServices.getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one service", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Validate Haircut Selection
            if (isHaircutSelected(selected) && (sharedViewModel.haircutChoice.getValue() == null || sharedViewModel.haircutChoice.getValue().isEmpty())) {
                Toast.makeText(getContext(), "Please select a haircut style", Toast.LENGTH_SHORT).show();
                return;
            }

            navigateToNext();
        });
    }

    // ⭐️ NEW: Listen for text changes to save address in real-time (optional but good)
    private void setupAddressInputListener() {
        binding.addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user types
                binding.addressInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedViewModel.homeServiceAddress.setValue(s.toString());
            }
        });
    }

    private void populateLocationSelector() {
        if (binding == null) return;
        binding.locationSelectorContainer.removeAllViews();
        barbershopLocationView = null;
        homeServiceLocationView = null;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        String currentSelection = sharedViewModel.serviceLocation.getValue();

        for (String locationName : LOCATION_OPTIONS) {
            View locationView = inflater.inflate(R.layout.item_selectable_service, binding.locationSelectorContainer, false);
            TextView nameText = locationView.findViewById(R.id.service_name_text);
            ImageView checkMark = locationView.findViewById(R.id.check_mark_icon);

            nameText.setText(locationName);
            nameText.setPadding(30, 30, 30, 30);
            nameText.setTextSize(16f);

            checkMark.setVisibility(locationName.equals(currentSelection) ? View.VISIBLE : View.GONE);

            if (LOCATION_BARBERSHOP.equals(locationName)) {
                barbershopLocationView = locationView;
            } else {
                homeServiceLocationView = locationView;
            }

            locationView.setOnClickListener(v -> {
                if (!locationName.equals(sharedViewModel.serviceLocation.getValue())) {
                    sharedViewModel.serviceLocation.setValue(locationName);
                }
            });

            binding.locationSelectorContainer.addView(locationView);
        }
    }

    private void updateLocationCheckmarks(String selectedLocation) {
        if (barbershopLocationView != null) {
            ImageView check = barbershopLocationView.findViewById(R.id.check_mark_icon);
            if (check != null) {
                check.setVisibility(LOCATION_BARBERSHOP.equals(selectedLocation) ? View.VISIBLE : View.GONE);
            }
        }
        if (homeServiceLocationView != null) {
            ImageView check = homeServiceLocationView.findViewById(R.id.check_mark_icon);
            if (check != null) {
                check.setVisibility(LOCATION_HOME_SERVICE.equals(selectedLocation) ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void setupHaircutDropdown() {
        removeHaircutDropdownFromParent();
        if (binding == null) return;
        binding.haircutChoiceLayout.setVisibility(View.GONE);

        haircutAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, HAIRCUT_OPTIONS);
        binding.haircutChoiceDropdown.setAdapter(haircutAdapter);

        binding.haircutChoiceDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedHaircut = haircutAdapter.getItem(position);
            sharedViewModel.haircutChoice.setValue(selectedHaircut);
            Log.d("ServicesFragment", "Haircut selected: " + selectedHaircut);
        });

        String savedHaircut = sharedViewModel.haircutChoice.getValue();
        if (savedHaircut != null) {
            binding.haircutChoiceDropdown.setText(savedHaircut, false);
        }
    }

    private void redrawServicesList() {
        List<Service> services = sharedViewModel.allServices.getValue();
        List<Service> currentlySelectedList = sharedViewModel.selectedServices.getValue();
        String currentServiceLocation = sharedViewModel.serviceLocation.getValue();

        if (binding == null || services == null || getContext() == null) return;

        binding.servicesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        removeHaircutDropdownFromParent();
        binding.haircutChoiceLayout.setVisibility(View.GONE);

        for (Service service : services) {
            View serviceView = inflater.inflate(R.layout.item_selectable_service, binding.servicesContainer, false);
            TextView nameText = serviceView.findViewById(R.id.service_name_text);
            ImageView checkMark = serviceView.findViewById(R.id.check_mark_icon);

            double displayPrice = service.getPrice();
            double homeServiceFee = 50.0;
            if (LOCATION_HOME_SERVICE.equals(currentServiceLocation)) {
                displayPrice += homeServiceFee;
            }

            nameText.setText(String.format(Locale.getDefault(), "%s - ₱%.2f", service.getServiceName(), displayPrice));

            boolean isSelected = currentlySelectedList != null &&
                    currentlySelectedList.stream().anyMatch(s -> s.getServiceId() == service.getServiceId());
            checkMark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            binding.servicesContainer.addView(serviceView);

            if (isHaircutService(service)) {
                removeHaircutDropdownFromParent();
                binding.servicesContainer.addView(binding.haircutChoiceLayout);
                binding.haircutChoiceLayout.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }

            serviceView.setOnClickListener(v -> {
                boolean wasSelected = currentlySelectedList != null &&
                        currentlySelectedList.stream().anyMatch(s -> s.getServiceId() == service.getServiceId());

                if (wasSelected) {
                    sharedViewModel.removeService(service);
                    if (isHaircutService(service)) {
                        sharedViewModel.haircutChoice.setValue(null);
                        if (binding != null) {
                            binding.haircutChoiceDropdown.setText("", false);
                        }
                    }
                } else {
                    sharedViewModel.addService(service);
                }
            });
        }
    }

    private void removeHaircutDropdownFromParent() {
        if (binding != null && binding.haircutChoiceLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) binding.haircutChoiceLayout.getParent()).removeView(binding.haircutChoiceLayout);
        }
    }

    private boolean isHaircutSelected(@Nullable List<Service> selectedServices) {
        if (selectedServices == null || selectedServices.isEmpty()) {
            return false;
        }
        return selectedServices.stream().anyMatch(this::isHaircutService);
    }

    private boolean isHaircutService(@Nullable Service service) {
        return service != null && HAIRCUT_SERVICE_NAME.equalsIgnoreCase(service.getServiceName());
    }

    private void navigateToNext() {
        if (getParentFragment() instanceof ReservationFragment) {
            ((ReservationFragment) getParentFragment()).navigateToBarbers();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        barbershopLocationView = null;
        homeServiceLocationView = null;
    }
}