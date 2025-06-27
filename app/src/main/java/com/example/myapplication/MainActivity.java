package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseHelper = FirebaseHelper.getInstance();

        // Show splash screen for 2 seconds then check auth status
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthenticationStatus, 2000);
    }

    private void checkAuthenticationStatus() {
        FirebaseUser currentUser = firebaseHelper.getAuth().getCurrentUser();

        if (currentUser != null) {
            // User is logged in, check their role and redirect accordingly
            checkUserRoleAndRedirect();
        } else {
            // User is not logged in, go to login screen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void checkUserRoleAndRedirect() {
        firebaseHelper.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                Intent intent;

                if ("admin".equals(user.getRole())) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, UserDashboardActivity.class);
                }

                startActivity(intent);
                finish();
            } else {
                // Error getting user data or user document doesn't exist
                // Sign out and go to login
                firebaseHelper.getAuth().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }
}