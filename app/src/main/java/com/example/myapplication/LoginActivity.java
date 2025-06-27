package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseHelper = FirebaseHelper.getInstance();

        // Check if user already logged in
        FirebaseUser currentUser = firebaseHelper.getAuth().getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already logged in: " + currentUser.getEmail());
            checkUserRoleAndRedirect();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.btn_go_to_register);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (!validateInput(email, password)) {
            return;
        }

        // Disable button and show loading state
        setLoadingState(true);

        Log.d(TAG, "Attempting login for email: " + email);

        firebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Login successful");
                        FirebaseUser user = firebaseHelper.getAuth().getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "User authenticated: " + user.getEmail());
                            checkUserRoleAndRedirect();
                        } else {
                            Log.e(TAG, "User is null after successful login");
                            Toast.makeText(this, "Login berhasil tapi tidak dapat mengambil data user",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Login failed", task.getException());
                        handleLoginError(task.getException());
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        // Reset errors
        etEmail.setError(null);
        etPassword.setError(null);

        // Validate email
        if (email.isEmpty()) {
            etEmail.setError("Email harus diisi");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return false;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Password harus diisi");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoadingState(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "Masuk..." : "MASUK");
        btnGoToRegister.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }

    private void handleLoginError(Exception exception) {
        String errorMessage = "Login gagal";

        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();

            Log.e(TAG, "Firebase Auth Error Code: " + errorCode);

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    errorMessage = "Format email tidak valid";
                    etEmail.setError(errorMessage);
                    etEmail.requestFocus();
                    break;
                case "ERROR_WRONG_PASSWORD":
                    errorMessage = "Password salah";
                    etPassword.setError(errorMessage);
                    etPassword.requestFocus();
                    break;
                case "ERROR_USER_NOT_FOUND":
                    errorMessage = "Email tidak terdaftar";
                    etEmail.setError(errorMessage);
                    etEmail.requestFocus();
                    break;
                case "ERROR_USER_DISABLED":
                    errorMessage = "Akun telah dinonaktifkan";
                    break;
                case "ERROR_TOO_MANY_REQUESTS":
                    errorMessage = "Terlalu banyak percobaan login. Coba lagi nanti";
                    break;
                case "ERROR_NETWORK_REQUEST_FAILED":
                    errorMessage = "Masalah koneksi internet. Periksa koneksi Anda";
                    break;
                case "ERROR_OPERATION_NOT_ALLOWED":
                    errorMessage = "Login dengan email/password tidak diizinkan";
                    break;
                default:
                    errorMessage = "Login gagal: " + exception.getMessage();
                    Log.e(TAG, "Unhandled error code: " + errorCode);
            }
        } else {
            // Handle other types of exceptions
            if (exception != null) {
                String message = exception.getMessage();
                if (message != null) {
                    if (message.contains("network") || message.contains("timeout")) {
                        errorMessage = "Masalah koneksi internet. Periksa koneksi Anda";
                    } else if (message.contains("recaptcha")) {
                        errorMessage = "Verifikasi reCAPTCHA gagal. Coba lagi";
                    } else {
                        errorMessage = "Login gagal: " + message;
                    }
                }
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void checkUserRoleAndRedirect() {
        Log.d(TAG, "Checking user role...");

        firebaseHelper.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    Log.d(TAG, "User role: " + user.getRole());

                    Intent intent;
                    if ("admin".equals(user.getRole())) {
                        intent = new Intent(this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(this, UserDashboardActivity.class);
                    }

                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "User object is null");
                    Toast.makeText(this, "Error: Data user tidak valid", Toast.LENGTH_SHORT).show();
                    // Logout user jika data tidak valid
                    firebaseHelper.getAuth().signOut();
                }
            } else {
                Log.e(TAG, "Error getting user data: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                Toast.makeText(this, "Error mengambil data user", Toast.LENGTH_SHORT).show();
                // Logout user jika tidak bisa mengambil data
                firebaseHelper.getAuth().signOut();
            }
        });
    }
}