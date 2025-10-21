package com.example.beteranos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.beteranos.databinding.ActivityCustomerLoginBinding;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerLoginActivity extends AppCompatActivity {

    private ActivityCustomerLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            String plainTextPassword = binding.passwordEditText.getText().toString().trim();

            if (email.isEmpty() || plainTextPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.loadingOverlay.setVisibility(View.VISIBLE);
            binding.loginButton.setEnabled(false);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                boolean loginSuccess = false;
                Intent intent = null;

                try (Connection conn = new ConnectionClass().CONN()) {
                    String query = "SELECT customer_id, first_name, middle_name, last_name, phone_number, password FROM customers WHERE email = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, email);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                String hashedPasswordFromDB = rs.getString("password");
                                if (hashedPasswordFromDB != null) {
                                    BCrypt.Result result = BCrypt.verifyer().verify(plainTextPassword.toCharArray(), hashedPasswordFromDB);
                                    if (result.verified) {
                                        loginSuccess = true;
                                        intent = new Intent(CustomerLoginActivity.this, ReservationActivity.class);
                                        intent.putExtra("CUSTOMER_ID", rs.getInt("customer_id"));

                                        // --- THIS IS THE FIX ---
                                        // Check for null values before adding them to the intent.
                                        // If a value is null, pass an empty string instead.
                                        String firstName = rs.getString("first_name");
                                        String middleName = rs.getString("middle_name");
                                        String lastName = rs.getString("last_name");
                                        String phoneNumber = rs.getString("phone_number");

                                        intent.putExtra("FIRST_NAME", firstName != null ? firstName : "");
                                        intent.putExtra("MIDDLE_NAME", middleName != null ? middleName : "");
                                        intent.putExtra("LAST_NAME", lastName != null ? lastName : "");
                                        intent.putExtra("PHONE_NUMBER", phoneNumber != null ? phoneNumber : "");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("CustomerLogin", "DB Error: " + e.getMessage(), e);
                }

                boolean finalLoginSuccess = loginSuccess;
                Intent finalIntent = intent;
                runOnUiThread(() -> {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);
                    if (finalLoginSuccess) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        // Save user data to SharedPreferences after successful login
                        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = userPrefs.edit();

                        editor.putBoolean("isLoggedIn", true);
                        editor.putInt("customer_id", finalIntent.getIntExtra("CUSTOMER_ID", -1));
                        editor.putString("first_name", finalIntent.getStringExtra("FIRST_NAME"));
                        editor.putString("middle_name", finalIntent.getStringExtra("MIDDLE_NAME"));
                        editor.putString("last_name", finalIntent.getStringExtra("LAST_NAME"));
                        editor.putString("phone_number", finalIntent.getStringExtra("PHONE_NUMBER"));
                        editor.putString("email", email); // Save the email used to log in
                        editor.apply(); // Save the changes

                        startActivity(finalIntent);
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
                    }
                });
            });
        });

        binding.signupText.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerLoginActivity.this, CustomerSignupActivity.class);
            startActivity(intent);
        });

        binding.forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerLoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}