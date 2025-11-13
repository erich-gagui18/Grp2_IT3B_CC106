package com.example.beteranos.ui_reservation.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
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

// --- Correct import paths ---
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
        View root = binding.getRoot();

        // Initialize NotificationsViewModel
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- User data and welcome message setup ---
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        String firstName = userPrefs.getString("first_name", "Guest");

        String welcomeText;
        if (isLoggedIn) {
            // Check if R.string.welcome_message exists and takes a String argument
            // If it doesn't, use the fallback: "Welcome, " + firstName + "!";
            try {
                welcomeText = getString(R.string.welcome_message, firstName);
            } catch (Exception e) {
                welcomeText = "Welcome, " + firstName + "!";
            }
        } else {
            welcomeText = "Welcome, Guest!";
        }
        binding.textHome.setText(welcomeText);
        // --- End welcome message setup ---


        // Handle back button press (Exits the app)
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finishAffinity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // Gallery navigation
        binding.galleryContainer.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_gallery);
        });

        // Barber Profile navigation
        LinearLayout barberContainer = view.findViewById(R.id.barber_container);
        if (barberContainer != null) {
            barberContainer.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                // Ensure R.id.action_home_to_barber_profile is in your nav graph
                navController.navigate(R.id.action_home_to_barber_profile);
            });
        }

        // ⭐ NEW: Product Navigation Functionality ⭐
        // In HomeFragment.java

        LinearLayout productContainer = view.findViewById(R.id.product_container);
        if (productContainer != null) {
            productContainer.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);

                // This line will now work correctly
                navController.navigate(R.id.action_navigation_home_to_navigation_products);
            });
        }

        // ⭐ END NEW ⭐

        // Setup notification icon click listener
        View notificationIcon = view.findViewById(R.id.iv_notifications);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> showNotificationDropdown(v));
        }
    }

    private void showNotificationDropdown(View anchorView) {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(getContext())
                .inflate(R.layout.notification_dropdown, null);

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

        // Get customerId from SharedPreferences
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        int customerId = userPrefs.getInt("customer_id", -1);

        if (isLoggedIn && customerId != -1) {
            // Observe notifications
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

            // Fetch notifications
            notificationsViewModel.fetchNotifications(customerId);
        } else {
            // Guest user
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

        notificationPopup.showAsDropDown(
                anchorView,
                -250, // X-offset for placement
                10,   // Y-offset
                Gravity.END
        );
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