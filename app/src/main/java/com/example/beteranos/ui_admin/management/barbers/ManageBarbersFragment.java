package com.example.beteranos.ui_admin.management.barbers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.beteranos.databinding.FragmentManageBarbersBinding;

public class ManageBarbersFragment extends Fragment {

    private FragmentManageBarbersBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBarbersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO:
        // 1. Set up a RecyclerView here (e.g., binding.barbersRecyclerView.setLayoutManager(...))
        // 2. Create a "BarbersManagementAdapter"
        // 3. Create a ViewModel (e.g., AdminManagementBarbersViewModel)
        // 4. Fetch all barbers from the database and show in the RecyclerView.
        // 5. Add a "Add New Barber" button (e.g., a FloatingActionButton).
        // 6. When clicked, show an AlertDialog to enter barber details (name, specialization, etc.).
        // 7. Save the new barber to the database and refresh the list.
        // 8. Add "Edit" and "Delete" buttons to your RecyclerView items.
        // 9. Handle barber images if applicable.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}