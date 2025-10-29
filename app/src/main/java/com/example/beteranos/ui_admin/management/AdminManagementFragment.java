package com.example.beteranos.ui_admin.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beteranos.databinding.FragmentAdminManagementBinding;

public class AdminManagementFragment extends Fragment {

    private FragmentAdminManagementBinding binding;
    private AdminManagementViewModel managementViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // ✅ Use ViewBinding safely
        binding = FragmentAdminManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ Initialize ViewModel in onViewCreated (safer with view lifecycle)
        managementViewModel = new ViewModelProvider(this).get(AdminManagementViewModel.class);

        // ✅ Observe LiveData properly with lifecycle owner
        managementViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            binding.textManagement.setText(text);
        });

        // ✅ Optional: Add click listener or refresh logic for future expansion
        binding.textManagement.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Management section clicked!", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
