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

import com.bumptech.glide.Glide; // --- Make sure Glide is imported ---

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

        sharedViewModel.allPromos.observe(getViewLifecycleOwner(), this::populatePromosList);

        sharedViewModel.selectedPromo.observe(getViewLifecycleOwner(), selectedPromo -> {
            if (sharedViewModel.allPromos.getValue() != null) {
                populatePromosList(sharedViewModel.allPromos.getValue());
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (getParentFragment() instanceof ReservationFragment) {
                ((ReservationFragment) getParentFragment()).navigateToSchedule();
            }
        });

        return binding.getRoot();
    }

    private void populatePromosList(List<Promo> promos) {
        if (binding == null || getContext() == null) return;

        binding.promosContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        Promo currentlySelected = sharedViewModel.selectedPromo.getValue();

        for (Promo promo : promos) {
            // Ensure you are inflating "item_selectable_promo.xml"
            View promoView = inflater.inflate(R.layout.item_selectable_promo, binding.promosContainer, false);

            TextView nameText = promoView.findViewById(R.id.promo_name_text);
            TextView descText = promoView.findViewById(R.id.promo_description_text);
            ImageView checkMark = promoView.findViewById(R.id.check_mark_icon);
            ImageView promoImage = promoView.findViewById(R.id.promo_image);

            nameText.setText(promo.getPromoName());

            String description = promo.getDescription();
            if (description != null && !description.isEmpty()) {
                descText.setText(description);
                descText.setVisibility(View.VISIBLE);
            } else {
                descText.setText(promo.getDiscountPercentage() + "% Off");
                descText.setVisibility(View.VISIBLE);
            }

            // --- UPDATED IMAGE LOADING LOGIC ---
            byte[] imageBytes = promo.getImage();

            if (imageBytes != null && imageBytes.length > 0) {
                // Case 1: Load valid byte array data as a Bitmap
                Glide.with(getContext())
                        .asBitmap() // Good addition to force bitmap decoding
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_image_broken)
                        .error(R.drawable.ic_image_broken)
                        .into(promoImage);
            } else {
                // Case 2: Load the default placeholder directly, bypassing data decoding issues
                Glide.with(getContext())
                        .load(R.drawable.ic_image_broken)
                        .into(promoImage);
            }
            // --- END OF UPDATED IMAGE LOADING LOGIC ---

            boolean isThisOneSelected = currentlySelected != null && currentlySelected.getPromoId() == promo.getPromoId();
            checkMark.setVisibility(isThisOneSelected ? View.VISIBLE : View.INVISIBLE);

            promoView.setOnClickListener(v -> {
                Promo currentInOnClick = sharedViewModel.selectedPromo.getValue();

                if (currentInOnClick != null && currentInOnClick.getPromoId() == promo.getPromoId()) {
                    sharedViewModel.selectedPromo.setValue(null);
                } else {
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