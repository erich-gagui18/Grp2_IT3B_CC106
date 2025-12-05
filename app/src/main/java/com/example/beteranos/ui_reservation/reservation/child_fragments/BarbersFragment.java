package com.example.beteranos.ui_reservation.reservation.child_fragments;

import android.graphics.Typeface;
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

        if (barbers == null || barbers.isEmpty()) {
            return;
        }

        // ⭐️ Safe Color Retrieval (Fixed deprecation & visibility)
        int prefixColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white);
        int dayOffColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_red_light);
        int availableColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_scheduled);

        for (Barber barber : barbers) {
            View barberView = inflater.inflate(R.layout.item_selectable_barber, binding.barbersContainer, false);

            TextView nameText = barberView.findViewById(R.id.barber_name_text);
            TextView specializationText = barberView.findViewById(R.id.barber_specialization_text);
            specializationText.setTypeface(null, Typeface.ITALIC);
            TextView dayOffText = barberView.findViewById(R.id.barber_day_off_text);
            ImageView checkMark = barberView.findViewById(R.id.check_mark_icon);
            ImageView profileImage = barberView.findViewById(R.id.barber_profile_image);

            // ---------------------------------------------------------
            // ⭐️ FIX 1: Robust Image Loading (Prevents Crashes & Logs)
            // ---------------------------------------------------------
            String imageUrl = barber.getImageUrl();

            // Check if URL is valid (not null, not empty, and not a garbage path like "/Monday")
            boolean isValidUrl = imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("/");
            if (isValidUrl) {
                try {
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            // ⭐️ SET DEFAULT TO BARBER SAMPLE ⭐️
                            .placeholder(R.drawable.barber_sample)
                            .error(R.drawable.barber_sample)
                            .fallback(R.drawable.barber_sample)
                            .centerCrop()
                            .into(profileImage);
                } catch (Exception e) {
                    // Safety catch -> Show Barber Sample
                    profileImage.setImageResource(R.drawable.barber_sample);
                }
            } else {
                // Invalid/Missing URL -> Show Barber Sample
                profileImage.setImageResource(R.drawable.barber_sample);
            }

            // ---------------------------------------------------------
            // ⭐️ Data Population
            // ---------------------------------------------------------
            nameText.setText(barber.getName());
            specializationText.setText(barber.getSpecialization());

            // ---------------------------------------------------------
            // ⭐️ FIX 2: Day Off Logic + Garbage Data Filter
            // ---------------------------------------------------------
            String barberDayOff = barber.getDayOff();

            // Detect if data is corrupted (e.g. contains file path/url instead of day name)
            boolean isInvalidData = barberDayOff != null && (barberDayOff.startsWith("content:") || barberDayOff.startsWith("/"));

            boolean hasSpecificDayOff = barberDayOff != null
                    && !barberDayOff.isEmpty()
                    && !barberDayOff.equalsIgnoreCase("none")
                    && !barberDayOff.equalsIgnoreCase("No day off")
                    && !isInvalidData;

            if (hasSpecificDayOff) {
                // Scenario 1: Valid Specific Day Off -> Red
                String prefix = "Day Off: ";
                String fullText = prefix + barberDayOff;
                SpannableString spannableString = new SpannableString(fullText);

                spannableString.setSpan(new ForegroundColorSpan(prefixColor), 0, prefix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(dayOffColor), prefix.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                dayOffText.setText(spannableString);
            } else {
                // Scenario 2: Available (or cleaning up garbage data) -> Green
                dayOffText.setText("No day off");
                dayOffText.setTextColor(availableColor);
            }

            // ---------------------------------------------------------
            // ⭐️ Selection Logic
            // ---------------------------------------------------------
            sharedViewModel.selectedBarber.observe(getViewLifecycleOwner(), selected -> {
                boolean isThisOneSelected = selected != null && selected.getBarberId() == barber.getBarberId();
                checkMark.setVisibility(isThisOneSelected ? View.VISIBLE : View.GONE);

                // Optional: Visual feedback on the card itself
                barberView.setAlpha(isThisOneSelected ? 1.0f : 0.9f);
            });

            barberView.setOnClickListener(v -> {
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