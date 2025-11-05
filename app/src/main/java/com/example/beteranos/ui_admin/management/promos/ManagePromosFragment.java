package com.example.beteranos.ui_admin.management.promos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.beteranos.databinding.FragmentManagePromosBinding;

public class ManagePromosFragment extends Fragment {

    private FragmentManagePromosBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagePromosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO:
        // 1. Set up a RecyclerView here (e.g., binding.promosRecyclerView.setLayoutManager(...))
        // 2. Create a "PromosManagementAdapter"
        // 3. Create a ViewModel (e.g., AdminManagementPromosViewModel)
        // 4. Fetch all promotions from the database and show in the RecyclerView.
        // 5. Add a "Add New Promo" button (e.g., a FloatingActionButton).
        // 6. When clicked, show an AlertDialog to enter promo details (name, description, discount, validity, etc.).
        // 7. Save the new promo to the database and refresh the list.
        // 8. Add "Edit" and "Delete" buttons to your RecyclerView items.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}