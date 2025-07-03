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
import com.example.myapplication.models.Admin;
import com.example.myapplication.models.User;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalPengaduan, tvPendingPengaduan, tvConfirmedPengaduan, tvRejectedPengaduan;
    private CardView cardKonfirmasiPengaduan, cardTindakanPengaduan, cardKelolaKategori, cardRiwayatPengaduan, cardTambahAdmin;
    private Button btnLogout;
    private FirebaseHelper firebaseHelper;
    private Admin currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard); // Layout tetap sama

        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupListeners();
        loadAdminData();
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
        cardTambahAdmin = findViewById(R.id.card_tambah_admin);

        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupListeners() {
        cardKonfirmasiPengaduan.setOnClickListener(v ->
                startActivity(new Intent(this, KonfirmasiPengaduanActivity.class))
        );

        cardTindakanPengaduan.setOnClickListener(v ->
                startActivity(new Intent(this, TindakanPengaduanActivity.class))
        );

        cardKelolaKategori.setOnClickListener(v ->
                startActivity(new Intent(this, KategoriManagementActivity.class))
        );

        cardRiwayatPengaduan.setOnClickListener(v ->
                startActivity(new Intent(this, RiwayatPengaduanAdminActivity.class))
        );

        cardTambahAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, TambahAdminActivity.class))
        );

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadAdminData() {
        firebaseHelper.getCurrentAdmin(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                currentAdmin = task.getResult().toObject(Admin.class);
                tvWelcome.setText("Selamat datang, " + currentAdmin.getName());
            } else {
                Toast.makeText(this, "Error loading admin data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardStats() {
        firebaseHelper.getDb().collection("pengaduan")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int total = 0, pending = 0, confirmed = 0, rejected = 0;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            total++;
                            String status = doc.getString("status");
                            if ("pending".equals(status)) pending++;
                            else if ("confirmed".equals(status)) confirmed++;
                            else if ("rejected".equals(status)) rejected++;
                        }
                        tvTotalPengaduan.setText(String.valueOf(total));
                        tvPendingPengaduan.setText(String.valueOf(pending));
                        tvConfirmedPengaduan.setText(String.valueOf(confirmed));
                        tvRejectedPengaduan.setText(String.valueOf(rejected));
                    } else {
                        Toast.makeText(this, "Error loading stats", Toast.LENGTH_SHORT).show();
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
        loadDashboardStats();
    }
}