package com.example.beteranos;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        // Find the navigation controller
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // Connect the BottomNavigationView to the NavController
        // This single line handles all navigation between your fragments!
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // The FAB listener remains separate, as its action is independent
        fab.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Scissors button clicked!", Toast.LENGTH_SHORT).show();
        });
    }
}