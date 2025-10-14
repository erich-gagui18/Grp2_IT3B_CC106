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

            nameText.setText(promo.getName());
            descText.setText(promo.getDescription());

            List<Promo> selectedList = sharedViewModel.selectedPromos.getValue();
            boolean isSelected = selectedList != null && selectedList.stream().anyMatch(p -> p.getId() == promo.getId());
            checkMark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            promoView.setOnClickListener(v -> {
                boolean currentlySelected = checkMark.getVisibility() == View.VISIBLE;
                if (currentlySelected) {
                    checkMark.setVisibility(View.GONE);
                    sharedViewModel.removePromo(promo);
                } else {
                    checkMark.setVisibility(View.VISIBLE);
                    sharedViewModel.addPromo(promo);
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