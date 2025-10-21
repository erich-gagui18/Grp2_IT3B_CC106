package com.example.beteranos;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.beteranos.databinding.ActivityForgotPasswordBinding;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.resetButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            resetPassword(email);
        });
    }

    private void resetPassword(String email) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String message;
            String newPassword = null; // Variable to hold the new password

            try (Connection conn = new ConnectionClass().CONN()) {
                String checkQuery = "SELECT COUNT(*) FROM customers WHERE email = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, email);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            newPassword = generateRandomPassword();
                            String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

                            String updateQuery = "UPDATE customers SET password = ? WHERE email = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setString(1, hashedPassword);
                                updateStmt.setString(2, email);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ForgotPassword", "DB Error: " + e.getMessage(), e);
            }

            // --- THIS IS THE FIX ---
            // Show the result on the UI thread
            String finalNewPassword = newPassword;
            runOnUiThread(() -> {
                if (finalNewPassword != null) {
                    // If a new password was generated, show it in a dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Password Reset")
                            .setMessage("Your new temporary password is: " + finalNewPassword)
                            .setPositiveButton("OK", (dialog, which) -> finish()) // Close activity on OK
                            .setCancelable(false)
                            .show();
                } else {
                    // If the email was not found, show a generic message
                    Toast.makeText(this, "If an account exists, a new password has been sent.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String generateRandomPassword() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}