package com.example.beteranos.ui_reservation.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// ⭐️ ADDED IMPORTS FOR ACTION BAR CONTROL ⭐️
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beteranos.R;
import com.example.beteranos.databinding.FragmentHomeBinding;
import com.example.beteranos.ui_reservation.home.notifications.NotificationAdapter;
import com.example.beteranos.ui_reservation.home.notifications.NotificationsViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PopupWindow notificationPopup;
    private NotificationsViewModel notificationsViewModel;
    private NotificationAdapter notificationAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        // Initialize NotificationsViewModel
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. User data and welcome message setup ---
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        String firstName = userPrefs.getString("first_name", "Guest");

        String welcomeText;
        if (isLoggedIn) {
            try {
                welcomeText = getString(R.string.welcome_message, firstName);
            } catch (Exception e) {
                welcomeText = "Welcome, " + firstName + "!";
            }
        } else {
            welcomeText = "Welcome, Guest!";
        }
        binding.textHome.setText(welcomeText);

        // --- 2. Handle back button press (Exits the app from Home) ---
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finishAffinity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // --- 3. Search Bar Toast ---
        if (binding.etSearch != null) {
            binding.etSearch.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // --- 4. Navigation Logic (Using ViewBinding) ---

        // A. Gallery Navigation
        if (binding.galleryContainer != null) {
            binding.galleryContainer.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.action_home_to_gallery);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Navigation Error: Gallery", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // B. Barber Profile Navigation
        if (binding.barberContainer != null) {
            binding.barberContainer.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.action_home_to_barber_profile);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Navigation Error: Barbers", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // C. Product Navigation
        if (binding.productContainer != null) {
            binding.productContainer.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_navigation_products);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Navigation Error: Products", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- 5. Notification Setup ---
        if (binding.ivNotifications != null) {
            binding.ivNotifications.setOnClickListener(v -> showNotificationDropdown(v));
        }
    }

    // ⭐️ ADDED: FIX FOR ACTION BAR BACK BUTTON PERSISTENCE ⭐️
    // This ensures the back arrow is hidden every time you return to HomeFragment

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false); // Hide the arrow
                actionBar.setTitle("Beteranos"); // Reset title to App Name
            }
        }
    }

    private void showNotificationDropdown(View anchorView) {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.notification_dropdown, null);

        notificationPopup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        RecyclerView rvNotifications = popupView.findViewById(R.id.rv_notification_dropdown);
        TextView tvEmpty = popupView.findViewById(R.id.tv_empty_notifications);
        ProgressBar progressBar = popupView.findViewById(R.id.pb_dropdown_loading);
        TextView tvSeeAll = popupView.findViewById(R.id.tv_see_all);

        notificationAdapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(notificationAdapter);

        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        int customerId = userPrefs.getInt("customer_id", -1);

        if (isLoggedIn && customerId != -1) {
            notificationsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                if (notificationPopup != null && notificationPopup.isShowing()) {
                    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
            });

            notificationsViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
                if (notificationPopup != null && notificationPopup.isShowing()) {
                    if (notifications != null && !notifications.isEmpty()) {
                        int endIndex = Math.min(notifications.size(), 5);
                        notificationAdapter.submitList(notifications.subList(0, endIndex));
                        rvNotifications.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    } else {
                        rvNotifications.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            });

            notificationsViewModel.fetchNotifications(customerId);
        } else {
            tvEmpty.setText("Please login to view notifications");
            tvEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        tvSeeAll.setOnClickListener(v -> {
            notificationPopup.dismiss();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_notifications);
        });

        notificationPopup.setOutsideTouchable(true);
        notificationPopup.setFocusable(true);

        notificationPopup.showAsDropDown(anchorView, -250, 10, Gravity.END);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.dismiss();
        }
        binding = null;
    }
}