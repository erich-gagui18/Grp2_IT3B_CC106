package com.example.beteranos.ui_reservation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.OnBackPressedCallback; // ⭐️ Import This
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.beteranos.R;
import com.example.beteranos.ui_customer_login.CustomerLoginActivity;
import com.example.beteranos.databinding.ActivityReservationBinding;

public class ReservationActivity extends AppCompatActivity {

    private ActivityReservationBinding binding;
    private static final String TAG = "ReservationActivity";
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityReservationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_reservation,
                R.id.navigation_dashboard, R.id.navigation_notifications,
                R.id.navigation_profile, R.id.navigation_reviews,
                R.id.navigation_gallery)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_reservation);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }

        // ⭐️ NEW: Register the OnBackPressedDispatcher Callback ⭐️
        // This replaces the old onBackPressed() override
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCustomBackNavigation();
            }
        });
    }

    // ⭐️ Helper method to handle the logic ⭐️
    private void handleCustomBackNavigation() {
        // 1. Check if user is a Guest
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isGuest = userPrefs.getBoolean("isGuest", false);

        Log.d(TAG, "Back pressed. Is Guest: " + isGuest);

        if (isGuest) {
            // **PRIORITY: ALWAYS EXIT TO LOGIN IF GUEST**
            SharedPreferences.Editor editor = userPrefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ReservationActivity.this, CustomerLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // **HANDLE LOGGED-IN USER NAVIGATION**
            if (navController != null) {
                int currentDestId = navController.getCurrentDestination().getId();

                // Check if we are ALREADY at the Home Fragment
                if (currentDestId == R.id.navigation_home) {
                    // If at Home, exit the app (close activity)
                    finish();
                } else {
                    // If on any other screen (Profile, Reviews, etc.), GO BACK TO HOME

                    // Attempt to pop the stack until we reach Home
                    boolean popped = navController.popBackStack(R.id.navigation_home, false);

                    if (!popped) {
                        // Fallback: If Home wasn't in the stack for some reason, navigate explicitly
                        navController.navigate(R.id.navigation_home);
                    }
                }
            } else {
                // Fallback if navController is null
                finish();
            }
        }
    }
}