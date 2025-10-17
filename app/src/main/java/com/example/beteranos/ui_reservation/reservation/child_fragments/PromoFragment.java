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
import com.example.beteranos.databinding.FragmentPromoBinding;
import com.example.beteranos.models.Promo;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import java.util.List;

public class PromoFragment extends Fragment {

    private FragmentPromoBinding binding;
    private SharedReservationViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPromoBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedReservationViewModel.class);

        // --- THIS IS THE FIX ---
        // We now have two observers: one for the list of all promos,
        // and one for the *selected* promo to refresh the checkmarks.

        // 1. Observer for the list of ALL promos from the database.
        sharedViewModel.allPromos.observe(getViewLifecycleOwner(), this::populatePromosList);

        // 2. Single observer for the SELECTED promo. This will refresh the UI when the selection changes.
        sharedViewModel.selectedPromo.observe(getViewLifecycleOwner(), selectedPromo -> {
            // When the selection changes, re-draw the list to update the checkmarks.
            if (sharedViewModel.allPromos.getValue() != null) {
                populatePromosList(sharedViewModel.allPromos.getValue());
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (sharedViewModel.selectedPromo.getValue() == null) {
                Toast.makeText(getContext(), "Please select a promo to continue", Toast.LENGTH_SHORT).show();
            } else {
                if (getParentFragment() instanceof ReservationFragment) {
                    ((ReservationFragment) getParentFragment()).navigateToSchedule();
                }
            }
        });

        return binding.getRoot();
    }

    private void populatePromosList(List<Promo> promos) {
        if (binding == null) return; // Prevents crashes if the view is destroyed

        binding.promosContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Get the currently selected promo from the ViewModel *before* the loop
        Promo currentlySelected = sharedViewModel.selectedPromo.getValue();

        for (Promo promo : promos) {
            View promoView = inflater.inflate(R.layout.item_selectable_promo, binding.promosContainer, false);

            TextView nameText = promoView.findViewById(R.id.promo_name_text);
            TextView descText = promoView.findViewById(R.id.promo_description_text);
            ImageView checkMark = promoView.findViewById(R.id.check_mark_icon);
            ImageView promoImage = promoView.findViewById(R.id.promo_image);

            nameText.setText(promo.getName());
            descText.setText(promo.getDescription());

            // Image loading logic
            String imageName = promo.getImageName();
            if (imageName != null && !imageName.isEmpty() && getContext() != null) {
                int imageResId = getContext().getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                promoImage.setImageResource(imageResId != 0 ? imageResId : R.drawable.barber_sample);
            } else {
                promoImage.setImageResource(R.drawable.barber_sample);
            }

            // Set the initial checkmark visibility based on the data from the ViewModel
            boolean isThisOneSelected = currentlySelected != null && currentlySelected.getId() == promo.getId();
            checkMark.setVisibility(isThisOneSelected ? View.VISIBLE : View.GONE);

            // Set the click listener to update the selection in the ViewModel
            promoView.setOnClickListener(v -> {
                Promo currentInOnClick = sharedViewModel.selectedPromo.getValue();
                if (currentInOnClick != null && currentInOnClick.getId() == promo.getId()) {
                    // If clicking the already selected one, deselect it
                    sharedViewModel.selectedPromo.setValue(null);
                } else {
                    // Otherwise, select the new one
                    sharedViewModel.selectedPromo.setValue(promo);
                }
            });
            binding.promosContainer.addView(promoView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}