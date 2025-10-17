package com.example.beteranos;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
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

        // 1. Get the username passed from the Login Activity
        String username = getIntent().getStringExtra("USERNAME_EXTRA");
        if (username == null || username.isEmpty()) {
            username = "Admin"; // Provide a default value
        }

        // 2. Set up NavController and pass the username as a start argument
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // --- THIS IS THE FIX ---
        // Pass the intent's extras (which includes the username) to the navigation graph
        navController.setGraph(navController.getGraph(), getIntent().getExtras());

        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.admin_nav_graph);

        Bundle startArgs = new Bundle();
        startArgs.putString("username", username);

        navController.setGraph(navGraph, startArgs);

        // 3. Connect the BottomNavigationView to the NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // FAB listener remains the same
        fab.setOnClickListener(v -> {
            navController.navigate(R.id.admin_nav_reservations);
        });
    }
}