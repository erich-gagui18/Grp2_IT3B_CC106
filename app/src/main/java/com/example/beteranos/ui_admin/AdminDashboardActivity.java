package com.example.beteranos.ui_admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.beteranos.R;
import com.example.beteranos.databinding.ActivityAdminDashboardBinding;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get NavHostFragment
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_admin_dashboard);
        NavController navController = navHostFragment.getNavController();

        // ✅ Define top-level destinations (must match your nav_graph IDs)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.admin_nav_home,
                R.id.admin_nav_calendar,
                R.id.admin_nav_dashboard,
                R.id.admin_nav_profile
        ).build();

        // Connect ActionBar + BottomNav
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        // Optional: FAB navigates to management
        binding.fabAdd.setOnClickListener(v -> navController.navigate(R.id.admin_nav_management));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_admin_dashboard);
        NavController navController = navHostFragment.getNavController();
        // ✅ Use appBarConfiguration instead of null
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
