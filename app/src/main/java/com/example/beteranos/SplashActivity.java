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
            intent = new Intent(this, ReservationActivity.class);

            // --- THIS IS THE FIX ---
            // Check for null values from SharedPreferences before putting them in the intent.
            // If a value is null, pass an empty string "" instead.
            intent.putExtra("CUSTOMER_ID", userPrefs.getInt("customer_id", -1));
            intent.putExtra("FIRST_NAME", userPrefs.getString("first_name", ""));
            intent.putExtra("MIDDLE_NAME", userPrefs.getString("middle_name", ""));
            intent.putExtra("LAST_NAME", userPrefs.getString("last_name", ""));
            intent.putExtra("PHONE_NUMBER", userPrefs.getString("phone_number", ""));

        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}