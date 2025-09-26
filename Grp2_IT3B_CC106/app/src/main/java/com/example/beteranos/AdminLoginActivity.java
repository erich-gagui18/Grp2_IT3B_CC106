package com.example.beteranos;

import android.content.Intent;
// import android.net.Uri; // No longer needed for the pop-up
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // <-- ADD THIS IMPORT
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button continueButton;
    private TextView needHelpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        continueButton = findViewById(R.id.continue_button);
        needHelpTextView = findViewById(R.id.need_help_text_view);

        continueButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLoginActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                loginAdmin(username, password);
            }
        });

        // ## THIS IS THE MODIFIED CLICK LISTENER ##
        needHelpTextView.setOnClickListener(v -> {
            // Create a new AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminLoginActivity.this);
            builder.setTitle("Contact Support");

            // Set the message with the contact details
            builder.setMessage("For assistance, please contact IT Solutions:\n\nContact No.: +63 917 123 4567\nEmail: support@itsolutions.ph");

            // Add a button to close the dialog
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            // Create and show the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void loginAdmin(final String username, final String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Connection conn = null;
            String resultMessage;
            boolean loginSuccess = false;

            try {
                ConnectionClass connectionClass = new ConnectionClass();
                conn = connectionClass.CONN();

                if (conn == null) {
                    resultMessage = "Error: Could not connect to the database.";
                } else {
                    String sql = "SELECT password_hash FROM Admins WHERE username = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, username);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("password_hash");
                        if (password.equals(storedPassword)) {
                            resultMessage = "I have Logged in";
                            loginSuccess = true;
                        } else {
                            resultMessage = "Invalid credentials.";
                        }
                    } else {
                        resultMessage = "Invalid credentials.";
                    }
                    rs.close();
                    pstmt.close();
                }
            } catch (SQLException e) {
                Log.e("AdminLogin", "SQL Exception: " + e.getMessage());
                resultMessage = "Database query error.";
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            String finalResultMessage = resultMessage;
            boolean finalLoginSuccess = loginSuccess;

            handler.post(() -> {
                Toast.makeText(AdminLoginActivity.this, finalResultMessage, Toast.LENGTH_SHORT).show();
                if (finalLoginSuccess) {
                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }
}