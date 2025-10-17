package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

        sharedViewModel.allPromos.observe(getViewLifecycleOwner(), this::populatePromosList);

        binding.btnNext.setOnClickListener(v -> {
            if (getParentFragment() instanceof ReservationFragment) {
                ((ReservationFragment) getParentFragment()).navigateToSchedule();
            }
        });

        return binding.getRoot();
    }

    private void populatePromosList(List<Promo> promos) {
        binding.promosContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Promo promo : promos) {
            View promoView = inflater.inflate(R.layout.item_selectable_promo, binding.promosContainer, false);

            TextView nameText = promoView.findViewById(R.id.promo_name_text);
            TextView descText = promoView.findViewById(R.id.promo_description_text);
            ImageView checkMark = promoView.findViewById(R.id.check_mark_icon);
            ImageView promoImage = promoView.findViewById(R.id.promo_image);

            nameText.setText(promo.getName());
            descText.setText(promo.getDescription());

            String imageName = promo.getImageName();
            if (imageName != null && !imageName.isEmpty()) {
                int imageResId = getContext().getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                if (imageResId != 0) {
                    promoImage.setImageResource(imageResId);
                } else {
                    // --- THIS IS THE FIX ---
                    // Changed to an existing drawable to prevent the crash
                    promoImage.setImageResource(R.drawable.barber_sample);
                }
            } else {
                // --- THIS IS THE FIX ---
                // Changed to an existing drawable to prevent the crash
                promoImage.setImageResource(R.drawable.barber_sample);
            }

            sharedViewModel.selectedPromo.observe(getViewLifecycleOwner(), selected -> {
                boolean isThisOneSelected = selected != null && selected.getId() == promo.getId();
                checkMark.setVisibility(isThisOneSelected ? View.VISIBLE : View.GONE);
            });

            promoView.setOnClickListener(v -> {
                Promo currentSelection = sharedViewModel.selectedPromo.getValue();
                if (currentSelection != null && currentSelection.getId() == promo.getId()) {
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