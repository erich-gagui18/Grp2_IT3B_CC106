package com.example.beteranos.ui_admin.calendar; // Or your package for this fragment

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.beteranos.databinding.FragmentAdminCalendarBinding; // This class is auto-generated

public class AdminCalendarFragment extends Fragment {

    private FragmentAdminCalendarBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AdminCalendarViewModel calendarViewModel =
                new ViewModelProvider(this).get(AdminCalendarViewModel.class);

        binding = FragmentAdminCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCalendar; // Uses ID from layout
        calendarViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}