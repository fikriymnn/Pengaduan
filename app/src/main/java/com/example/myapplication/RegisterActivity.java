package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etConfirmPassword, etPhoneNumber;
    private RadioGroup rgRole;
    private RadioButton rbUser, rbAdmin;
    private Button btnRegister, btnBackToLogin;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        setupListeners();
        setupBackPressHandler();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        rgRole = findViewById(R.id.rg_role);
        rbUser = findViewById(R.id.rb_user);
        rbAdmin = findViewById(R.id.rb_admin);
        btnRegister = findViewById(R.id.btn_register);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        // Set default role to user
        rbUser.setChecked(true);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegister());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        String role = rbUser.isChecked() ? "user" : "admin";

        // Validation (keep existing validation code)
        if (name.isEmpty()) {
            etName.setError("Nama harus diisi");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email harus diisi");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password harus diisi");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password tidak sama");
            etConfirmPassword.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Nomor telepon harus diisi");
            etPhoneNumber.requestFocus();
            return;
        }

        if (phoneNumber.length() < 10) {
            etPhoneNumber.setError("Nomor telepon tidak valid");
            etPhoneNumber.requestFocus();
            return;
        }

        // Start registration with retry mechanism
        attemptRegistration(name, email, password, role, phoneNumber, 0);
    }

    private void attemptRegistration(String name, String email, String password,
                                     String role, String phoneNumber, int retryCount) {
        final int MAX_RETRIES = 3;

        btnRegister.setEnabled(false);

        // Show loading message
        String loadingMessage = retryCount > 0 ?
                "Mencoba lagi... (" + (retryCount + 1) + "/" + (MAX_RETRIES + 1) + ")" :
                "Mendaftarkan akun...";
        Toast.makeText(this, loadingMessage, Toast.LENGTH_SHORT).show();

        // Create Firebase Auth account with timeout handling
        firebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Create user document in Firestore
                        User user = new User(name, email, role, phoneNumber);
                        firebaseHelper.createUser(user, userTask -> {
                            btnRegister.setEnabled(true);
                            if (userTask.isSuccessful()) {
                                Toast.makeText(this, "Registrasi berhasil! Silakan login.",
                                        Toast.LENGTH_SHORT).show();

                                // Sign out user so they need to login
                                firebaseHelper.getAuth().signOut();

                                // Go back to login
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            } else {
                                handleUserCreationError(userTask.getException());
                            }
                        });
                    } else {
                        handleRegistrationError(task.getException(), name, email, password,
                                role, phoneNumber, retryCount, MAX_RETRIES);
                    }
                });
    }

    private void handleRegistrationError(Exception exception, String name, String email,
                                         String password, String role, String phoneNumber,
                                         int retryCount, int maxRetries) {
        btnRegister.setEnabled(true);

        String errorMessage = exception.getMessage();

        // Check if it's a network-related error that can be retried
        boolean isNetworkError = errorMessage.contains("network error") ||
                errorMessage.contains("timeout") ||
                errorMessage.contains("unreachable host") ||
                errorMessage.contains("RecaptchaAction");

        if (isNetworkError && retryCount < maxRetries) {
            // Show retry dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Koneksi Bermasalah")
                    .setMessage("Terjadi masalah jaringan. Apakah Anda ingin mencoba lagi?")
                    .setPositiveButton("Coba Lagi", (dialog, which) -> {
                        // Wait a bit before retrying
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            attemptRegistration(name, email, password, role, phoneNumber, retryCount + 1);
                        }, 2000); // Wait 2 seconds
                    })
                    .setNegativeButton("Batal", (dialog, which) -> {
                        Toast.makeText(this, "Registrasi dibatalkan", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            // Show final error message
            String finalMessage;
            if (isNetworkError) {
                finalMessage = "Registrasi gagal karena masalah jaringan. Silakan coba lagi nanti atau periksa koneksi internet Anda.";
            } else {
                finalMessage = "Registrasi gagal: " + errorMessage;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Registrasi Gagal")
                    .setMessage(finalMessage)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void handleUserCreationError(Exception exception) {
        Toast.makeText(this, "Gagal menyimpan data user: " + exception.getMessage(),
                Toast.LENGTH_LONG).show();

        // Delete the auth account if user creation failed
        if (firebaseHelper.getAuth().getCurrentUser() != null) {
            firebaseHelper.getAuth().getCurrentUser().delete()
                    .addOnCompleteListener(deleteTask -> {
                        if (!deleteTask.isSuccessful()) {
                            // Log the error but don't show to user
                            android.util.Log.e("RegisterActivity",
                                    "Failed to delete auth user after Firestore error",
                                    deleteTask.getException());
                        }
                    });
        }
    }

    private void setupBackPressHandler() {
        // Handle back press with new OnBackPressedDispatcher (API 33+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to login
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}