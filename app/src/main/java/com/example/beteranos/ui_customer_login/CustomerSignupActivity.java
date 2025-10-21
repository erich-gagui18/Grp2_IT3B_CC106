package com.example.beteranos.ui_customer_login;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.databinding.ActivityCustomerSignupBinding;
// --- ADD THIS IMPORT ---
import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerSignupActivity extends AppCompatActivity {

    private ActivityCustomerSignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signupButton.setOnClickListener(v -> {
            String firstName = binding.firstNameEditText.getText().toString().trim();
            String lastName = binding.lastNameEditText.getText().toString().trim();
            String phone = binding.phoneEditText.getText().toString().trim();
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                String message = "Sign up failed. Please try again.";
                try (Connection conn = new ConnectionClass().CONN()) {
                    String checkEmailQuery = "SELECT COUNT(*) FROM customers WHERE email = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailQuery)) {
                        checkStmt.setString(1, email);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                message = "This email is already registered.";
                            } else {
                                // --- THIS IS THE FIX ---
                                // Hash the password before storing it
                                String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

                                String insertQuery = "INSERT INTO customers (first_name, last_name, phone_number, email, password) VALUES (?, ?, ?, ?, ?)";
                                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                    insertStmt.setString(1, firstName);
                                    insertStmt.setString(2, lastName);
                                    insertStmt.setString(3, phone);
                                    insertStmt.setString(4, email);
                                    insertStmt.setString(5, hashedPassword); // Save the HASH, not the plain password

                                    if (insertStmt.executeUpdate() > 0) {
                                        message = "Sign up successful! You can now log in.";
                                        runOnUiThread(this::finish);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("CustomerSignup", "DB Error: " + e.getMessage(), e);
                }

                String finalMessage = message;
                runOnUiThread(() -> Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show());
            });
        });
    }
}