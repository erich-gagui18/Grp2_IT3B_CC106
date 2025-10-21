package com.example.beteranos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            // If logged in, go directly to the Reservation screen
            intent = new Intent(this, ReservationActivity.class);
            // Pass the saved user data to the next activity
            intent.putExtra("CUSTOMER_ID", userPrefs.getInt("customer_id", -1));
            intent.putExtra("FIRST_NAME", userPrefs.getString("first_name", ""));
            intent.putExtra("MIDDLE_NAME", userPrefs.getString("middle_name", ""));
            intent.putExtra("LAST_NAME", userPrefs.getString("last_name", ""));
            intent.putExtra("PHONE_NUMBER", userPrefs.getString("phone_number", ""));
        } else {
            // If not logged in, go to the MainActivity (with login/admin buttons)
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish(); // Close the splash screen so the user can't go back to it
    }
}