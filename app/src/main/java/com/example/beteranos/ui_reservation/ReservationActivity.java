package com.example.beteranos.ui_reservation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // 🔙 Override the Back Button behavior
    @Override
    public void onBackPressed() {
        // 1. Retrieve SharedPreferences to check user state
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isGuest = userPrefs.getBoolean("isGuest", false);

        Log.d(TAG, "onBackPressed called. Is Guest: " + isGuest);

        if (isGuest) {
            // **PRIORITY: ALWAYS EXIT TO LOGIN IF GUEST**

            // Clear guest state/all session data
            SharedPreferences.Editor editor = userPrefs.edit();
            editor.clear();
            editor.apply();

            // Navigate back to the Customer Login Activity
            Intent intent = new Intent(this, CustomerLoginActivity.class);

            // Clear the activity stack (closing ReservationActivity and ensuring LoginActivity is the root)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Close this activity immediately
            finish();
            return; // Stop further processing
        } else {
            // **HANDLE LOGGED-IN USER NAVIGATION**

            // 2. Check if the Navigation Component can handle the back press (i.e., pop a fragment)
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_reservation);
            if (navHostFragment != null && navHostFragment.getNavController().popBackStack()) {
                // NavController handled the back press (popped a fragment)
                return;
            }

            // 3. Fallback for default system behavior (close activity if at root)
            super.onBackPressed();
        }
    }
}