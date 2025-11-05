package com.example.beteranos.ui_reservation.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout; // ⭐️ ADDED: Import for the LinearLayout container ⭐️
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Back Button Callback (Existing Code) ---
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finishAffinity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        // --- Click Listener for Gallery (Existing Code) ---
        binding.galleryContainer.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_gallery);
        });

        // ⭐️ NEW CODE: Click Listener for Barber Icon ⭐️
        // Access the LinearLayout using its ID
        LinearLayout barberContainer = view.findViewById(R.id.barber_container);

        if (barberContainer != null) {
            barberContainer.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);

                // Navigate using the action defined in mobile_navigation.xml
                navController.navigate(R.id.action_home_to_barber_profile);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}