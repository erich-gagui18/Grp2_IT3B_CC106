package com.example.beteranos;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Example: Show a welcome toast when screen loads
        Toast.makeText(this, "Welcome to Beteranos Barbers Hub!", Toast.LENGTH_SHORT).show();
    }
}
