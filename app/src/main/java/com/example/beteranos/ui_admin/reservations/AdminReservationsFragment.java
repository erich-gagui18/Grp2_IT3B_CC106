package com.example.beteranos.ui_admin.reservations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.databinding.FragmentAdminReservationsBinding;

public class AdminReservationsFragment extends Fragment {

    private FragmentAdminReservationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AdminReservationsViewModel calendarViewModel =
                new ViewModelProvider(this).get(AdminReservationsViewModel.class);

        binding = FragmentAdminReservationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textAdminReservations;
        calendarViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}