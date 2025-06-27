package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Pengaduan;

public class DetailKonfirmasiActivity extends AppCompatActivity {

    private TextView tvJudul, tvDeskripsi, tvLokasi;
    private Button btnKonfirmasi, btnTolak;
    private ProgressBar progressBar;

    private String pengaduanId;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_konfirmasi);

        tvJudul = findViewById(R.id.tv_judul);
        tvDeskripsi = findViewById(R.id.tv_deskripsi);
        tvLokasi = findViewById(R.id.tv_lokasi);
        btnKonfirmasi = findViewById(R.id.btn_konfirmasi);
        btnTolak = findViewById(R.id.btn_tolak);
        progressBar = findViewById(R.id.progress_bar);

        firebaseHelper = FirebaseHelper.getInstance();
        pengaduanId = getIntent().getStringExtra("pengaduan_id");

        loadPengaduanDetail();

        btnKonfirmasi.setOnClickListener(v -> {
            firebaseHelper.updatePengaduanStatus(pengaduanId, "confirmed", task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Pengaduan dikonfirmasi", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });

        btnTolak.setOnClickListener(v -> showRejectionDialog());
    }

    private void showRejectionDialog() {
        final EditText input = new EditText(this);
        input.setHint("Alasan penolakan");

        new AlertDialog.Builder(this)
                .setTitle("Tolak Pengaduan")
                .setMessage("Masukkan alasan:")
                .setView(input)
                .setPositiveButton("Tolak", (dialog, which) -> {
                    String alasan = input.getText().toString().trim();
                    firebaseHelper.updatePengaduanStatusWithReason(pengaduanId, "rejected", alasan, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Pengaduan ditolak", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void loadPengaduanDetail() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getDb().collection("pengaduan").document(pengaduanId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Pengaduan p = task.getResult().toObject(Pengaduan.class);
                        if (p != null) {
                            tvJudul.setText(p.getJudul());
                            tvDeskripsi.setText(p.getDeskripsi());
                            tvLokasi.setText(p.getLokasi());
                        }
                    } else {
                        Toast.makeText(this, "Gagal memuat detail", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
