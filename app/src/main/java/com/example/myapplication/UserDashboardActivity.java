package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.User;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.atomic.AtomicInteger;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalPengaduan, tvPendingPengaduan, tvConfirmedPengaduan;
    private CardView cardBuatPengaduan, cardHistoryPengaduan, cardProfile;
    private FirebaseHelper firebaseHelper;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
        setupBackPressHandler();
        loadUserData();
        loadPengaduanStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats when returning to dashboard
        loadPengaduanStats();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvTotalPengaduan = findViewById(R.id.tv_total_pengaduan);
        tvPendingPengaduan = findViewById(R.id.tv_pending_pengaduan);
        tvConfirmedPengaduan = findViewById(R.id.tv_confirmed_pengaduan);

        cardBuatPengaduan = findViewById(R.id.card_buat_pengaduan);
        cardHistoryPengaduan = findViewById(R.id.card_history_pengaduan);
        cardProfile = findViewById(R.id.card_profile);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard User");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void setupClickListeners() {
        cardBuatPengaduan.setOnClickListener(v -> {
            checkUserDataBeforeCreatePengaduan();
        });

        cardHistoryPengaduan.setOnClickListener(v -> {
            startActivity(new Intent(this, RiwayatPengaduanActivity.class));
        });

        cardProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void checkUserDataBeforeCreatePengaduan() {
        firebaseHelper.checkUserDataComplete(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                boolean isDataComplete = firebaseHelper.isUserDataComplete(task.getResult());

                if (isDataComplete) {
                    // Data lengkap, bisa buat pengaduan
                    startActivity(new Intent(this, BuatPengaduanActivity.class));
                } else {
                    // Data belum lengkap, arahkan ke profile
                    showDataIncompleteDialog();
                }
            } else {
                Toast.makeText(this, "Error checking user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDataIncompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Data Belum Lengkap")
                .setMessage("Untuk membuat pengaduan, Anda harus melengkapi data berikut terlebih dahulu:\n\n• Nomor KTP\n• Nomor Kartu Keluarga\n• Alamat Lengkap\n\nApakah Anda ingin melengkapi data sekarang?")
                .setPositiveButton("Ya, Lengkapi", (dialog, which) -> {
                    startActivity(new Intent(this, ProfileActivity.class));
                })
                .setNegativeButton("Nanti", null)
                .setCancelable(false)
                .show();
    }

    private void setupBackPressHandler() {
        // Handle back press with new OnBackPressedDispatcher (API 33+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show confirmation dialog when back button is pressed
                new AlertDialog.Builder(UserDashboardActivity.this)
                        .setTitle("Keluar Aplikasi")
                        .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            finishAffinity(); // Close all activities
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });
    }

    private void loadUserData() {
        firebaseHelper.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                currentUser = task.getResult().toObject(User.class);
                if (currentUser != null) {
                    tvWelcome.setText("Selamat datang, " + currentUser.getName());
                }
            } else {
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                // If error loading user data, sign out and go to login
                firebaseHelper.getAuth().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }

    @SuppressLint("NewApi")
    private void loadPengaduanStats() {
        String userId = firebaseHelper.getAuth().getCurrentUser().getUid();

        firebaseHelper.getUserPengaduan(userId, task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                int totalPengaduan = querySnapshot.size();
                AtomicInteger pendingCount = new AtomicInteger();
                AtomicInteger confirmedCount = new AtomicInteger();

                // Count pengaduan by status
                querySnapshot.forEach(document -> {
                    String status = document.getString("status");
                    if ("pending".equals(status)) {
                        pendingCount.getAndIncrement();
                    } else if ("confirmed".equals(status)) {
                        confirmedCount.getAndIncrement();
                    }
                });

                // Update UI
                tvTotalPengaduan.setText(String.valueOf(totalPengaduan));
                tvPendingPengaduan.setText(String.valueOf(pendingCount.get()));
                tvConfirmedPengaduan.setText(String.valueOf(confirmedCount.get()));
            } else {
                Toast.makeText(this, "Error loading pengaduan stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            loadPengaduanStats();
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tentang Aplikasi")
                .setMessage("Aplikasi Pengaduan Masyarakat\n" +
                        "Kepolisian Daerah\n\n" +
                        "Versi 1.0.0\n" +
                        "© 2024 Kepolisian Daerah")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> performLogout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performLogout() {
        firebaseHelper.getAuth().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show();
    }
}