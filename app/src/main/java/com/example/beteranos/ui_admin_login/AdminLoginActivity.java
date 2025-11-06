package com.example.beteranos.ui_admin_login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback; // --- ADDED THIS IMPORT ---
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beteranos.ConnectionClass;
import com.example.beteranos.MainActivity; // --- ADDED THIS IMPORT ---
import com.example.beteranos.R;
import com.example.beteranos.ui_admin.AdminDashboardActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// --- REMOVED BCrypt and SharedPreferences imports ---

public class AdminLoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button continueButton;
    private TextView needHelpTextView;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        continueButton = findViewById(R.id.continue_button);
        needHelpTextView = findViewById(R.id.need_help_text_view);
        backButton = findViewById(R.id.back_button);

        continueButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLoginActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                loginAdmin(username, password);
            }
        });

        needHelpTextView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminLoginActivity.this);
            builder.setTitle("Contact Support");
            builder.setMessage("For assistance, please contact IT Solutions:\n\nContact No.: +63 917 123 4567\nEmail: support@itsolutions.ph");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // --- THIS IS THE FIX ---
        // 1. Handle the top-left <ImageButton>
        backButton.setOnClickListener(v -> {
            goToMainActivity();
        });

        // 2. Handle the system navigation back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goToMainActivity();
            }
        });
        // --- END OF FIX ---
    }

    // --- NEW HELPER METHOD FOR THE FIX ---
    private void goToMainActivity() {
        Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
        // These flags start MainActivity as a new task and clear the old one.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish AdminLoginActivity
    }


    // --- YOUR ORIGINAL loginAdmin METHOD (UNCHANGED) ---
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
                        // This is your original plain-text comparison
                        if (password.equals(storedPassword)) {
                            resultMessage = "Login Successful";
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

                    // This is your original line
                    intent.putExtra("USERNAME_EXTRA", username);

                    startActivity(intent);
                    finish();
                }
            });
        });
    }

    // --- OVERRIDE for the back button fix ---
    @Override
    public void onBackPressed() {
        // This will be caught by the OnBackPressedCallback in onCreate,
        // but this serves as a robust fallback.
        goToMainActivity();
        // We do not call super.onBackPressed() here
    }
}