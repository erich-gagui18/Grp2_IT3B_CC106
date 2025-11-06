package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
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

import java.util.Arrays; // Import Arrays
import java.util.List;
import java.util.Locale;

public class ServicesFragment extends Fragment {

    private FragmentServicesBinding binding;
    private SharedReservationViewModel sharedViewModel;

    private final String[] HAIRCUT_OPTIONS = {"Buzz Cut", "Undercut Fade", "Modern Mullet", "French Crop", "Taper", "Others"};
    private ArrayAdapter<String> haircutAdapter;
    private static final String HAIRCUT_SERVICE_NAME = "Haircut";

    // Define location options
    private static final String LOCATION_BARBERSHOP = "Barbershop";
    private static final String LOCATION_HOME_SERVICE = "Home Service";
    private final List<String> LOCATION_OPTIONS = Arrays.asList(LOCATION_BARBERSHOP, LOCATION_HOME_SERVICE);

    // Keep track of the location item views to manage checkmarks
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

        populateLocationSelector(); // Populate location choices first
        setupHaircutDropdown();

        // --- FIX: Use a single, combined observer for UI refreshes ---
        // This observer will re-draw the list if EITHER the list of services OR
        // the list of *selected* services changes.
        sharedViewModel.allServices.observe(getViewLifecycleOwner(), services -> {
            if (services != null && !services.isEmpty()) {
                redrawServicesList();
            }
        });

        sharedViewModel.selectedServices.observe(getViewLifecycleOwner(), selected -> {
            redrawServicesList();
        });
        // --- END FIX ---

        // Observe location changes if needed for dynamic pricing, etc.
        sharedViewModel.serviceLocation.observe(getViewLifecycleOwner(), location -> {
            Log.d("ServicesFragment", "Observed location change: " + location);
            // Re-populate services if prices depend on location
            redrawServicesList(); // Re-render services to potentially show different prices
            // Update the checkmarks for location selector
            updateLocationCheckmarks(location);
        });


        binding.btnNext.setOnClickListener(v -> {
            // Check if a location is selected (it defaults, but good practice)
            if (sharedViewModel.serviceLocation.getValue() == null || sharedViewModel.serviceLocation.getValue().isEmpty()) {
                Toast.makeText(getContext(), "Please select a service location", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Service> selected = sharedViewModel.selectedServices.getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one service", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isHaircutSelected(selected) && (sharedViewModel.haircutChoice.getValue() == null || sharedViewModel.haircutChoice.getValue().isEmpty())) {
                Toast.makeText(getContext(), "Please select a haircut style", Toast.LENGTH_SHORT).show();
                return;
            }

            navigateToNext();
        });
    }

    // --- NEW: Populate Location Selector Items ---
    private void populateLocationSelector() {
        if (binding == null) return; // Ensure binding is valid
        binding.locationSelectorContainer.removeAllViews(); // Clear previous
        barbershopLocationView = null; // Reset view references
        homeServiceLocationView = null;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        String currentSelection = sharedViewModel.serviceLocation.getValue();

        for (String locationName : LOCATION_OPTIONS) {
            // Inflate using the same item layout as services
            View locationView = inflater.inflate(R.layout.item_selectable_service, binding.locationSelectorContainer, false);
            TextView nameText = locationView.findViewById(R.id.service_name_text);
            ImageView checkMark = locationView.findViewById(R.id.check_mark_icon);

            nameText.setText(locationName);
            // Optional: Adjust style slightly
            nameText.setPadding(30, 30, 30, 30);
            nameText.setTextSize(16f);

            // Show checkmark based on ViewModel state
            checkMark.setVisibility(locationName.equals(currentSelection) ? View.VISIBLE : View.GONE);

            // Store references
            if (LOCATION_BARBERSHOP.equals(locationName)) {
                barbershopLocationView = locationView;
            } else {
                homeServiceLocationView = locationView;
            }

            // Set click listener to update ViewModel and UI
            locationView.setOnClickListener(v -> {
                // Update ViewModel only if selection changes
                if (!locationName.equals(sharedViewModel.serviceLocation.getValue())) {
                    sharedViewModel.serviceLocation.setValue(locationName);
                    // The observer will handle updating checkmarks now
                }
            });

            binding.locationSelectorContainer.addView(locationView);
        }
    }

    // --- UPDATED: Helper to update checkmarks (called by observer) ---
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

    // --- RENAMED from populateServicesList to redrawServicesList ---
    private void redrawServicesList() {
        // Get the most up-to-date data from the ViewModel
        List<Service> services = sharedViewModel.allServices.getValue();
        List<Service> currentlySelectedList = sharedViewModel.selectedServices.getValue();
        String currentServiceLocation = sharedViewModel.serviceLocation.getValue();

        if (binding == null || services == null || getContext() == null) return; // Check binding and services list

        binding.servicesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        removeHaircutDropdownFromParent();
        binding.haircutChoiceLayout.setVisibility(View.GONE); // Hide by default

        for (Service service : services) {
            View serviceView = inflater.inflate(R.layout.item_selectable_service, binding.servicesContainer, false);
            TextView nameText = serviceView.findViewById(R.id.service_name_text);
            ImageView checkMark = serviceView.findViewById(R.id.check_mark_icon);

            // --- Example: Adjust price display based on location ---
            double displayPrice = service.getPrice();
            // Define your home service fee logic (e.g., a fixed amount or fetch from DB)
            double homeServiceFee = 50.0; // Example fee
            if (LOCATION_HOME_SERVICE.equals(currentServiceLocation)) {
                displayPrice += homeServiceFee;
            }

            // --- FIX: Use getServiceName() ---
            nameText.setText(String.format(Locale.getDefault(), "%s - ₱%.2f", service.getServiceName(), displayPrice));
            // --- End of price adjustment ---

            // --- FIX: Use getServiceId() ---
            boolean isSelected = currentlySelectedList != null &&
                    currentlySelectedList.stream().anyMatch(s -> s.getServiceId() == service.getServiceId());
            checkMark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            binding.servicesContainer.addView(serviceView);

            // --- FIX: Use getServiceName() ---
            if (isHaircutService(service)) {
                // This service is a haircut, add the dropdown layout *after* it
                removeHaircutDropdownFromParent(); // Ensure it's not elsewhere
                binding.servicesContainer.addView(binding.haircutChoiceLayout);
                // Only show dropdown if the haircut is selected
                binding.haircutChoiceLayout.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }

            serviceView.setOnClickListener(v -> {
                // We don't need to check visibility. Just check the ViewModel state.
                boolean wasSelected = currentlySelectedList != null &&
                        currentlySelectedList.stream().anyMatch(s -> s.getServiceId() == service.getServiceId());

                if (wasSelected) {
                    sharedViewModel.removeService(service);
                    if (isHaircutService(service)) {
                        // Clear haircut choice when haircut is deselected
                        sharedViewModel.haircutChoice.setValue(null);
                        if (binding != null) { // Check binding
                            binding.haircutChoiceDropdown.setText("", false);
                        }
                        Log.d("ServicesFragment", "Haircut deselected, choice cleared.");
                    }
                } else {
                    sharedViewModel.addService(service);
                    if (isHaircutService(service)) {
                        Log.d("ServicesFragment", "Haircut selected, dropdown shown.");
                    }
                }
                // The observers in onViewCreated will catch this change and call redrawServicesList()
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
        // --- FIX: Use getServiceName() ---
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
        binding = null; // Nullify binding
        barbershopLocationView = null; // Clear view references
        homeServiceLocationView = null;
    }
}