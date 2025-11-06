package com.example.beteranos.ui_reservation.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout; // Import for the LinearLayout container
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
import com.example.beteranos.ui_reservation.notifications.NotificationAdapter;
import com.example.beteranos.ui_reservation.notifications.NotificationsViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PopupWindow notificationPopup;
    private NotificationsViewModel notificationsViewModel;
    private NotificationAdapter notificationAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Initialize NotificationsViewModel
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle back button press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Exits the app (same as pressing home)
                requireActivity().finishAffinity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // Gallery navigation
        binding.galleryContainer.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_gallery);
        });

        // --- ADDED: Barber Profile navigation ---
        // Access the LinearLayout using its ID from the layout
        LinearLayout barberContainer = view.findViewById(R.id.barber_container);
        if (barberContainer != null) {
            barberContainer.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                // Navigate using the action defined in mobile_navigation.xml
                navController.navigate(R.id.action_home_to_barber_profile);
            });
        }
        // --- END OF ADDED CODE ---

        // Setup notification icon click listener
        View notificationIcon = view.findViewById(R.id.iv_notifications);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> showNotificationDropdown(v));
        }
    }

    private void showNotificationDropdown(View anchorView) {
        // Dismiss existing popup if open
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.dismiss();
            return;
        }

        // Inflate dropdown layout
        View popupView = LayoutInflater.from(getContext())
                .inflate(R.layout.notification_dropdown, null);

        // Create popup window
        notificationPopup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Setup RecyclerView
        RecyclerView rvNotifications = popupView.findViewById(R.id.rv_notification_dropdown);
        TextView tvEmpty = popupView.findViewById(R.id.tv_empty_notifications);
        ProgressBar progressBar = popupView.findViewById(R.id.pb_dropdown_loading);
        TextView tvSeeAll = popupView.findViewById(R.id.tv_see_all);

        notificationAdapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(notificationAdapter);

        // Get customer ID (Ensure this is correct for your app flow)
        int customerId = requireActivity().getIntent().getIntExtra("CUSTOMER_ID", -1);

        if (customerId != -1) {
            // Observe notifications
            notificationsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                if (notificationPopup != null && notificationPopup.isShowing()) {
                    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
            });

            notificationsViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
                if (notificationPopup != null && notificationPopup.isShowing()) {
                    if (notifications != null && !notifications.isEmpty()) {
                        // Show only last 5 notifications in dropdown
                        int endIndex = Math.min(notifications.size(), 5);
                        notificationAdapter.setNotifications(notifications.subList(0, endIndex));
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

        // See All button - navigate to notifications fragment
        tvSeeAll.setOnClickListener(v -> {
            notificationPopup.dismiss();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_notifications);
        });

        // Dismiss popup when clicked outside
        notificationPopup.setOutsideTouchable(true);
        notificationPopup.setFocusable(true);

        // Show popup below the notification icon
        notificationPopup.showAsDropDown(
                anchorView,
                -250, // X offset (adjust to align properly)
                10,   // Y offset
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