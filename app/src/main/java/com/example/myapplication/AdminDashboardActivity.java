package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.User;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalPengaduan, tvPendingPengaduan, tvConfirmedPengaduan, tvRejectedPengaduan;
    private CardView cardKonfirmasiPengaduan, cardTindakanPengaduan, cardKelolaKategori, cardRiwayatPengaduan, cardManajemenUser;
    private Button btnLogout;
    private FirebaseHelper firebaseHelper;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupListeners();
        loadUserData();
        loadDashboardStats();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvTotalPengaduan = findViewById(R.id.tv_total_pengaduan);
        tvPendingPengaduan = findViewById(R.id.tv_pending_pengaduan);
        tvConfirmedPengaduan = findViewById(R.id.tv_confirmed_pengaduan);
        tvRejectedPengaduan = findViewById(R.id.tv_rejected_pengaduan);

        cardKonfirmasiPengaduan = findViewById(R.id.card_konfirmasi_pengaduan);
        cardTindakanPengaduan = findViewById(R.id.card_tindakan_pengaduan);
        cardKelolaKategori = findViewById(R.id.card_kelola_kategori);
        cardRiwayatPengaduan = findViewById(R.id.card_riwayat_pengaduan);
        //cardManajemenUser = findViewById(R.id.card_manajemen_user);

        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupListeners() {
        cardKonfirmasiPengaduan.setOnClickListener(v -> {
            startActivity(new Intent(this, KonfirmasiPengaduanActivity.class));
        });

        cardTindakanPengaduan.setOnClickListener(v -> {
            startActivity(new Intent(this, TindakanPengaduanActivity.class));
        });

        cardKelolaKategori.setOnClickListener(v -> {
            startActivity(new Intent(this, KategoriManagementActivity.class));
        });

        cardRiwayatPengaduan.setOnClickListener(v -> {
            startActivity(new Intent(this, RiwayatPengaduanAdminActivity.class));
        });

//        cardManajemenUser.setOnClickListener(v -> {
//            startActivity(new Intent(this, ManajemenUserActivity.class));
//        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        firebaseHelper.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                currentUser = task.getResult().toObject(User.class);
                tvWelcome.setText("Selamat datang, " + currentUser.getName());
            } else {
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardStats() {
        // Load total pengaduan
        firebaseHelper.getDb().collection("pengaduan")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalPengaduan = task.getResult().size();
                        int pendingCount = 0;
                        int confirmedCount = 0;
                        int rejectedCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String status = document.getString("status");
                            if (status != null) {
                                switch (status) {
                                    case "pending":
                                        pendingCount++;
                                        break;
                                    case "confirmed":
                                        confirmedCount++;
                                        break;
                                    case "rejected":
                                        rejectedCount++;
                                        break;
                                }
                            }
                        }

                        tvTotalPengaduan.setText(String.valueOf(totalPengaduan));
                        tvPendingPengaduan.setText(String.valueOf(pendingCount));
                        tvConfirmedPengaduan.setText(String.valueOf(confirmedCount));
                        tvRejectedPengaduan.setText(String.valueOf(rejectedCount));
                    } else {
                        Toast.makeText(this, "Error loading statistics", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    firebaseHelper.getAuth().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh statistics when returning to dashboard
        loadDashboardStats();
    }
}