package com.example.beteranos.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // 👈 New Import
import androidx.navigation.Navigation;   // 👈 New Import

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 1. Add the click listener for the Gallery icon container
        // This uses the Navigation Component to move to the new GalleryFragment.
        root.findViewById(R.id.gallery_container).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            // Navigate to the ID defined in mobile_navigation.xml
            navController.navigate(R.id.navigation_gallery);
        });

        // The rest of your original code follows:
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}