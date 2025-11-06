package com.example.beteranos.ui_admin;

import android.os.Bundle;
import android.view.Menu; // --- ADD THIS IMPORT ---
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions; // --- ADD THIS IMPORT ---
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.beteranos.R;
import com.example.beteranos.databinding.ActivityAdminDashboardBinding;
// Note: Do NOT import NavigationBarView.OnItemSelectedListener. We use the one from NavController.

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_admin_dashboard);
        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.admin_nav_home,
                R.id.admin_nav_calendar,
                R.id.admin_nav_dashboard,
                R.id.admin_nav_profile,
                R.id.admin_nav_management
        ).build();

        // Connect ActionBar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // --- THIS IS THE REAL FIX ---
        // We are replacing the default click listener with one that correctly
        // handles single-top navigation, just like the default implementation.

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            // 1. Build NavOptions to pop to the start destination.
            // This prevents building a deep stack of fragments.
            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true) // Don't re-create fragment if already on top
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false) // Pop to Home
                    .build();

            try {
                // 2. Navigate to the selected item's ID with these options
                navController.navigate(item.getItemId(), null, navOptions);
                return true;
            } catch (IllegalArgumentException e) {
                // This happens if the user clicks the invisible placeholder.
                // We just ignore it.
                return false;
            }
        });

        // 3. Add a listener to sync the NavController's state (from back button)
        //    back to the BottomNavigationView.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            MenuItem item = binding.bottomNavigationView.getMenu().findItem(destination.getId());
            if (item != null) {
                // Destination is in the bottom nav, check it.
                item.setChecked(true);
            } else {
                // Destination is NOT in the bottom nav (e.g., Management).
                // Clear all selections.
                clearBottomNavSelection();
            }
        });
        // --- END OF FIX ---

        // FAB navigates to management.
        // The listener above will automatically un-check the bottom nav items.
        binding.fabAdd.setOnClickListener(v -> {
            navController.navigate(R.id.admin_nav_management);
        });
    }

    /**
     * Helper method to un-check all items in the bottom navigation view.
     */
    private void clearBottomNavSelection() {
        Menu menu = binding.bottomNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                item.setChecked(false);
                break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}