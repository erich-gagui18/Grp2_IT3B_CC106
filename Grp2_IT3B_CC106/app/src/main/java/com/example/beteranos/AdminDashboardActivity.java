package com.example.beteranos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        logoutButton = findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> {
            // Create an Intent to go back to the AdminLoginActivity
            Intent intent = new Intent(AdminDashboardActivity.this, AdminLoginActivity.class);
            startActivity(intent);
            // Finish the dashboard activity so the user can't go back to it
            finish();
        });
    }
}