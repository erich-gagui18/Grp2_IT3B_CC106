package com.example.beteranos.ui_customer_login;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.databinding.ActivityCustomerSignupBinding;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types; // ⭐️ Needed for handling NULL values
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
            // 1. Get Inputs
            String firstName = binding.firstNameEditText.getText().toString().trim();
            String middleName = binding.middleNameEditText.getText().toString().trim(); // ⭐️ New Input
            String lastName = binding.lastNameEditText.getText().toString().trim();
            String phone = binding.phoneEditText.getText().toString().trim();
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String guestCode = binding.guestCodeEditText.getText().toString().trim();

            // 2. Validate (Middle Name is OPTIONAL, so we don't check it here)
            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || guestCode.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                String message = "Sign up failed. Please try again.";
                try (Connection conn = new ConnectionClass().CONN()) {
                    if (conn == null) throw new Exception("DB Connection Failed");

                    // A. Check Email
                    String checkEmailQuery = "SELECT COUNT(*) FROM customers WHERE email = ?";
                    boolean emailExists = false;
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailQuery)) {
                        checkStmt.setString(1, email);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                emailExists = true;
                                message = "This email is already registered.";
                            }
                        }
                    }

                    if (!emailExists) {
                        // B. Verify Code
                        String checkCodeQuery = "SELECT code_id FROM guest_codes WHERE code_value = ? AND is_used = 0";
                        boolean codeIsValid = false;
                        try (PreparedStatement codeStmt = conn.prepareStatement(checkCodeQuery)) {
                            codeStmt.setString(1, guestCode);
                            try (ResultSet rsCode = codeStmt.executeQuery()) {
                                if (rsCode.next()) {
                                    codeIsValid = true;
                                } else {
                                    message = "Invalid Guest Code.";
                                }
                            }
                        }

                        // C. Insert Customer
                        if (codeIsValid) {
                            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

                            // ⭐️ UPDATED QUERY: Included middle_name
                            String insertQuery = "INSERT INTO customers (first_name, middle_name, last_name, phone_number, email, password) VALUES (?, ?, ?, ?, ?, ?)";

                            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                insertStmt.setString(1, firstName);

                                // ⭐️ Handle Optional Middle Name
                                if (middleName.isEmpty()) {
                                    insertStmt.setNull(2, Types.VARCHAR);
                                } else {
                                    insertStmt.setString(2, middleName);
                                }

                                insertStmt.setString(3, lastName);
                                insertStmt.setString(4, phone);
                                insertStmt.setString(5, email);
                                insertStmt.setString(6, hashedPassword);

                                if (insertStmt.executeUpdate() > 0) {
                                    message = "Sign up successful! You can now log in.";
                                    runOnUiThread(this::finish);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("CustomerSignup", "DB Error: " + e.getMessage(), e);
                    message = "Error: " + e.getMessage();
                }

                String finalMessage = message;
                runOnUiThread(() -> Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show());
            });
        });
    }
}