package com.example.beteranos.ui_admin.management.services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.beteranos.databinding.FragmentManageServicesBinding; // This will be created

public class ManageServicesFragment extends Fragment {

    private FragmentManageServicesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageServicesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO:
        // 1. Set up a RecyclerView here (e.g., binding.servicesRecyclerView.setLayoutManager(...))
        // 2. Create a "ServicesManagementAdapter"
        // 3. Create a ViewModel (e.g., AdminManagementViewModel)
        // 4. Fetch all services from the database and show in the RecyclerView.
        // 5. Add a "Add New Service" button (e.g., a FloatingActionButton).
        // 6. When clicked, show an AlertDialog to enter a new service name and price.
        // 7. Save the new service to the database and refresh the list.
        // 8. Add "Edit" and "Delete" buttons to your RecyclerView items.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}