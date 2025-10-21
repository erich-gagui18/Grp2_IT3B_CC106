package com.example.beteranos.ui_reservation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.beteranos.R;
import com.example.beteranos.databinding.ActivityReservationBinding;

public class ReservationActivity extends AppCompatActivity {

    private ActivityReservationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityReservationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- THIS IS THE FIX ---
        // This code now correctly uses the IDs from your XML files.

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_reservation, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_gallery)
                .build();

        // Use the ID from your activity_reservation.xml
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_reservation);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}