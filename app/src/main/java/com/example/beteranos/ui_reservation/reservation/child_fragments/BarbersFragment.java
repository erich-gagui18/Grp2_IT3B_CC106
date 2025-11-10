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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentBarbersBinding;
import com.example.beteranos.models.Barber;
import com.example.beteranos.ui_reservation.reservation.SharedReservationViewModel;
import com.example.beteranos.ui_reservation.reservation.parent_fragments.ReservationFragment;
import com.example.beteranos.ConnectionClass; // Added in case you need it, though not strictly required here
import com.bumptech.glide.Glide; // 🔑 NEW: Import for image loading

import java.util.List;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

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

        // 🔑 Get colors once for efficiency and clarity
        // Assuming R.color.black is available, or use android.R.color.black
        // NOTE: Changed prefixColor to R.color.black for visibility against a standard white background,
        // but if your layout background is dark, you can keep android.R.color.white.
        int prefixColor = getResources().getColor(android.R.color.white, null);
        int dayOffColor = getResources().getColor(android.R.color.holo_red_light, null);
        int availableColor = getResources().getColor(R.color.status_scheduled, null);
        // NOTE: We rely on your existing R.color.status_scheduled for green/available status.

        for (Barber barber : barbers) {
            View barberView = inflater.inflate(R.layout.item_selectable_barber, binding.barbersContainer, false);

            TextView nameText = barberView.findViewById(R.id.barber_name_text);
            TextView specializationText = barberView.findViewById(R.id.barber_specialization_text);
            TextView dayOffText = barberView.findViewById(R.id.barber_day_off_text);
            ImageView checkMark = barberView.findViewById(R.id.check_mark_icon);
            ImageView profileImage = barberView.findViewById(R.id.barber_profile_image); // 🔑 NEW: Reference to the image view

            // 🔑 NEW: Load Barber Profile Image using Glide
            if (barber.getImageUrl() != null && !barber.getImageUrl().isEmpty()) {
                Glide.with(requireContext())
                        .load(barber.getImageUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // ADD THIS
                        .skipMemoryCache(true)                     // ADD THIS
                        .placeholder(R.drawable.barber_sample)
                        .error(R.drawable.barber_sample)
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.barber_sample); // Set default if URL is missing
            }

            // 🔑 POPULATE DATA
            nameText.setText(barber.getName());
            specializationText.setText(barber.getSpecialization());

            // 🔑 OPTIMIZED LOGIC USING SPANNABLE STRING
            String barberDayOff = barber.getDayOff();

            if (barberDayOff != null && !barberDayOff.isEmpty() && !barberDayOff.equalsIgnoreCase("none")) {
                // Scenario 1: Barber has a specific day off (show prefix in black, day in red)

                final String prefix = "Day Off: ";
                String fullText = prefix + barberDayOff;

                SpannableString spannableString = new SpannableString(fullText);

                // 1. Set the color for the prefix "Day Off: " (from index 0 up to prefix length)
                spannableString.setSpan(
                        new ForegroundColorSpan(prefixColor),
                        0,
                        prefix.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                // 2. Set the color for the day off itself (from prefix length to end)
                spannableString.setSpan(
                        new ForegroundColorSpan(dayOffColor),
                        prefix.length(),
                        fullText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                dayOffText.setText(spannableString);

            } else {
                // Scenario 2: Barber is generally available (show text in green/scheduled color)
                dayOffText.setText("No day off");
                dayOffText.setTextColor(availableColor);
            }
            // ------------------

            // Observe the single selected barber to update the checkmark
            // NOTE: The List is populated once, but the checkmark observer runs for every item.
            // This is generally safe but can be optimized if performance is critical.
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