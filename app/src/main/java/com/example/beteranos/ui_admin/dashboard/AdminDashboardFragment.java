package com.example.beteranos.ui_admin.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.databinding.FragmentAdminDashboardBinding;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // ✅ Use descriptive variable name
        AdminDashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ✅ Observe LiveData properly
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // ✅ Optional: handle passed arguments (e.g. username)
        Bundle args = getArguments();
        if (args != null && args.containsKey("username")) {
            String username = args.getString("username");
            textView.setText("Welcome to the Dashboard, " + username + "!");
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
