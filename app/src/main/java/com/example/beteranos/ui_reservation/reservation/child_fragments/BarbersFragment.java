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
import com.bumptech.glide.Glide;

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

        // Safe Color Retrieval
        int prefixColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white);
        int dayOffColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_red_light);
        int availableColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_scheduled);

        for (Barber barber : barbers) {
            View barberView = inflater.inflate(R.layout.item_selectable_barber, binding.barbersContainer, false);

            TextView nameText = barberView.findViewById(R.id.barber_name_text);
            TextView specializationText = barberView.findViewById(R.id.barber_specialization_text);
            specializationText.setTypeface(null, Typeface.ITALIC);
            TextView dayOffText = barberView.findViewById(R.id.barber_day_off_text);
            // ⭐️ NEW: Find the Schedule TextView
            TextView scheduleText = barberView.findViewById(R.id.barber_schedule_text);

            ImageView checkMark = barberView.findViewById(R.id.check_mark_icon);
            ImageView profileImage = barberView.findViewById(R.id.barber_profile_image);

            // ---------------------------------------------------------
            // 1. Image Loading
            // ---------------------------------------------------------
            String imageUrl = barber.getImageUrl();
            boolean isValidUrl = imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("/");

            try {
                Glide.with(requireContext())
                        .load(isValidUrl ? imageUrl : R.drawable.barber_sample)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.barber_sample)
                        .error(R.drawable.barber_sample)
                        .fallback(R.drawable.barber_sample)
                        .centerCrop()
                        .into(profileImage);
            } catch (Exception e) {
                profileImage.setImageResource(R.drawable.barber_sample);
            }

            // ---------------------------------------------------------
            // 2. Basic Data
            // ---------------------------------------------------------
            nameText.setText(barber.getName());
            specializationText.setText(barber.getSpecialization());

            // ---------------------------------------------------------
            // ⭐️ 3. NEW: Display Schedule (Start/End Time)
            // ---------------------------------------------------------
            if (scheduleText != null) {
                String start = barber.getStartTime();
                String end = barber.getEndTime();

                if (start != null && end != null) {
                    // Display: "Hours: 8:00 am - 7:00 pm"
                    scheduleText.setText(String.format("Time: %s - %s", start, end));
                    scheduleText.setVisibility(View.VISIBLE);
                } else {
                    scheduleText.setVisibility(View.GONE);
                }
            }

            // ---------------------------------------------------------
            // 4. Day Off Logic
            // ---------------------------------------------------------
            String barberDayOff = barber.getDayOff();
            boolean isInvalidData = barberDayOff != null && (barberDayOff.startsWith("content:") || barberDayOff.startsWith("/"));
            boolean hasSpecificDayOff = barberDayOff != null
                    && !barberDayOff.isEmpty()
                    && !barberDayOff.equalsIgnoreCase("none")
                    && !barberDayOff.equalsIgnoreCase("No day off")
                    && !isInvalidData;

            if (hasSpecificDayOff) {
                String prefix = "Day Off: ";
                String fullText = prefix + barberDayOff;
                SpannableString spannableString = new SpannableString(fullText);

                spannableString.setSpan(new ForegroundColorSpan(prefixColor), 0, prefix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(dayOffColor), prefix.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                dayOffText.setText(spannableString);
            } else {
                dayOffText.setText("No day off");
                dayOffText.setTextColor(availableColor);
            }

            // ---------------------------------------------------------
            // 5. Selection Logic
            // ---------------------------------------------------------
            sharedViewModel.selectedBarber.observe(getViewLifecycleOwner(), selected -> {
                boolean isThisOneSelected = selected != null && selected.getBarberId() == barber.getBarberId();
                checkMark.setVisibility(isThisOneSelected ? View.VISIBLE : View.GONE);
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