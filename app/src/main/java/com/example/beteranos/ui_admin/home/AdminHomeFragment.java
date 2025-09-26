package com.example.beteranos.ui_admin.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.databinding.FragmentAdminHomeBinding;
import java.util.Calendar;
import java.util.List;

public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        AdminHomeViewModel homeViewModel =
                new ViewModelProvider(this).get(AdminHomeViewModel.class);

        if (getArguments() != null) {
            String username = getArguments().getString("username");
            homeViewModel.setUsername(username);
        }

        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView welcomeTextView = binding.textWelcome;
        final TextView reservationsLabel = binding.reservationsLabel;
        final LinearLayout reservationsContainer = binding.reservationsContainer;

        homeViewModel.getWelcomeMessage().observe(getViewLifecycleOwner(), welcomeTextView::setText);
        homeViewModel.getReservationsLabel().observe(getViewLifecycleOwner(), reservationsLabel::setText);

        final CalendarView calendarView = binding.calendarView;
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            homeViewModel.loadReservationsForDate(selectedDate.getTimeInMillis());
        });

        homeViewModel.getSelectedDateReservations().observe(getViewLifecycleOwner(), reservations -> {
            reservationsContainer.removeAllViews();
            if (reservations.isEmpty()) {
                TextView noReservationsView = new TextView(getContext());
                noReservationsView.setText("No reservations for this day.");
                reservationsContainer.addView(noReservationsView);
            } else {
                for (String reservation : reservations) {
                    TextView reservationView = new TextView(getContext());
                    reservationView.setText(reservation);
                    reservationView.setTextSize(16);
                    reservationView.setPadding(0, 8, 0, 8);
                    reservationsContainer.addView(reservationView);
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}