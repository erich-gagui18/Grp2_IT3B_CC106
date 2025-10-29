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
import java.util.List;
import java.util.Locale; // Ensure Locale is imported

public class ServicesFragment extends Fragment {

    private FragmentServicesBinding binding;
    private SharedReservationViewModel sharedViewModel;

    private final String[] HAIRCUT_OPTIONS = {"Buzz Cut", "Undercut Fade", "Modern Mullet", "French Crop", "Taper", "Others"};
    private ArrayAdapter<String> haircutAdapter;

    private static final String HAIRCUT_SERVICE_NAME = "Haircut";
    // private static final int HAIRCUT_SERVICE_ID = 1; // Alternative

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentServicesBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupHaircutDropdown(); // Setup adapter and listener

        sharedViewModel.allServices.observe(getViewLifecycleOwner(), services -> {
            if (services != null && !services.isEmpty()) {
                // Ensure dropdown is removed from potential previous parent before repopulating
                removeHaircutDropdownFromParent();
                populateServicesList(services);
            }
        });

        // No separate observer for selectedServices needed for visibility control now

        binding.btnNext.setOnClickListener(v -> {
            List<Service> selected = sharedViewModel.selectedServices.getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one service", Toast.LENGTH_SHORT).show();
            } else {
                if (isHaircutSelected(selected) && (sharedViewModel.haircutChoice.getValue() == null || sharedViewModel.haircutChoice.getValue().isEmpty())) {
                    Toast.makeText(getContext(), "Please select a haircut style", Toast.LENGTH_SHORT).show();
                } else {
                    navigateToNext();
                }
            }
        });
    }

    private void setupHaircutDropdown() {
        // Ensure the dropdown is initially gone and removed from its XML parent if necessary
        removeHaircutDropdownFromParent(); // Call this early
        binding.haircutChoiceLayout.setVisibility(View.GONE);

        haircutAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, HAIRCUT_OPTIONS);
        binding.haircutChoiceDropdown.setAdapter(haircutAdapter);

        binding.haircutChoiceDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedHaircut = haircutAdapter.getItem(position);
            sharedViewModel.haircutChoice.setValue(selectedHaircut);
            Log.d("ServicesFragment", "Haircut selected: " + selectedHaircut);
        });

        // Restore saved value
        String savedHaircut = sharedViewModel.haircutChoice.getValue();
        if (savedHaircut != null) {
            binding.haircutChoiceDropdown.setText(savedHaircut, false);
        }
    }

    private void populateServicesList(List<Service> services) {
        binding.servicesContainer.removeAllViews(); // Clear previous items
        LayoutInflater inflater = LayoutInflater.from(getContext());
        List<Service> currentlySelectedList = sharedViewModel.selectedServices.getValue();

        // Make sure dropdown is not attached before the loop
        removeHaircutDropdownFromParent();
        binding.haircutChoiceLayout.setVisibility(View.GONE); // Default hide

        for (Service service : services) {
            View serviceView = inflater.inflate(R.layout.item_selectable_service, binding.servicesContainer, false);
            TextView nameText = serviceView.findViewById(R.id.service_name_text);
            ImageView checkMark = serviceView.findViewById(R.id.check_mark_icon);

            nameText.setText(String.format(Locale.getDefault(), "%s - ₱%.2f", service.getName(), service.getPrice()));

            boolean isSelected = currentlySelectedList != null &&
                    currentlySelectedList.stream().anyMatch(s -> s.getId() == service.getId());
            checkMark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // --- Add the service item view FIRST ---
            binding.servicesContainer.addView(serviceView);

            // --- Conditionally add the haircut dropdown AFTER the haircut service item ---
            if (isHaircutService(service)) {
                // Remove from potential previous parent *again* just in case (shouldn't be needed if done before loop)
                removeHaircutDropdownFromParent();
                // Add the single dropdown instance to the container
                binding.servicesContainer.addView(binding.haircutChoiceLayout);
                // Set its visibility based on current selection state
                binding.haircutChoiceLayout.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }

            // --- Setup Click Listener ---
            serviceView.setOnClickListener(v -> {
                boolean wasSelected = checkMark.getVisibility() == View.VISIBLE;
                if (wasSelected) { // Deselecting
                    checkMark.setVisibility(View.GONE);
                    sharedViewModel.removeService(service);
                    if (isHaircutService(service)) {
                        binding.haircutChoiceLayout.setVisibility(View.GONE); // Hide dropdown
                        sharedViewModel.haircutChoice.setValue(null);
                        binding.haircutChoiceDropdown.setText("", false);
                        Log.d("ServicesFragment", "Haircut deselected, choice cleared and dropdown hidden.");
                    }
                } else { // Selecting
                    checkMark.setVisibility(View.VISIBLE);
                    sharedViewModel.addService(service);
                    if (isHaircutService(service)) {
                        binding.haircutChoiceLayout.setVisibility(View.VISIBLE); // Show dropdown
                        Log.d("ServicesFragment", "Haircut selected, dropdown shown.");
                        // Ensure dropdown text reflects current ViewModel value (might be null initially)
                        String currentChoice = sharedViewModel.haircutChoice.getValue();
                        binding.haircutChoiceDropdown.setText(currentChoice != null ? currentChoice : "", false);
                    }
                }
            });
        }
    }

    // --- Helper to safely remove the dropdown from its current parent ---
    private void removeHaircutDropdownFromParent() {
        if (binding != null && binding.haircutChoiceLayout.getParent() instanceof ViewGroup) {
            ((ViewGroup) binding.haircutChoiceLayout.getParent()).removeView(binding.haircutChoiceLayout);
        }
    }

    // Checks if the "Haircut" service is present in the list of selected services
    private boolean isHaircutSelected(@Nullable List<Service> selectedServices) {
        if (selectedServices == null || selectedServices.isEmpty()) {
            return false;
        }
        return selectedServices.stream().anyMatch(this::isHaircutService);
    }

    // Checks if a specific service is the "Haircut" service
    private boolean isHaircutService(@Nullable Service service) {
        return service != null && HAIRCUT_SERVICE_NAME.equalsIgnoreCase(service.getName());
        // return service != null && service.getId() == HAIRCUT_SERVICE_ID; // Alternative
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
    }
}