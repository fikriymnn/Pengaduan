package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Kategori;
import com.example.myapplication.models.Pengaduan;
import com.example.myapplication.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuatPengaduanActivity extends AppCompatActivity {

    private RadioGroup rgJenisPengaduan;
    private RadioButton rbKejahatan, rbUmum;
    private View layoutKategori;
    private Spinner spinnerKategori;
    private TextInputEditText etJudul, etDeskripsi, etLokasi;
    private Button btnSubmit;

    private FirebaseHelper firebaseHelper;
    private User currentUser;
    private List<Kategori> kategoriList;
    private ArrayAdapter<String> kategoriAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_pengaduan);

        firebaseHelper = FirebaseHelper.getInstance();
        kategoriList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupListeners();
        setupBackPressHandler();
        loadUserData();
        loadKategori();
    }

    private void initViews() {
        rgJenisPengaduan = findViewById(R.id.rg_jenis_pengaduan);
        rbKejahatan = findViewById(R.id.rb_kejahatan);
        rbUmum = findViewById(R.id.rb_umum);
        layoutKategori = findViewById(R.id.layout_kategori);
        spinnerKategori = findViewById(R.id.spinner_kategori);
        etJudul = findViewById(R.id.et_judul);
        etDeskripsi = findViewById(R.id.et_deskripsi);
        etLokasi = findViewById(R.id.et_lokasi);
        btnSubmit = findViewById(R.id.btn_submit);

        // Set default selection
        rbKejahatan.setChecked(true);
        layoutKategori.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Buat Pengaduan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        rgJenisPengaduan.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_kejahatan) {
                layoutKategori.setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_umum) {
                layoutKategori.setVisibility(View.VISIBLE);
            }
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmitPengaduan());
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });
    }

    private void loadUserData() {
        firebaseHelper.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                currentUser = task.getResult().toObject(User.class);
            } else {
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadKategori() {
        // Tambahkan log untuk debugging
        Log.d("BuatPengaduan", "Starting to load kategori...");

        firebaseHelper.getKategori(task -> {
            Log.d("BuatPengaduan", "Firebase task completed. Success: " + task.isSuccessful());

            if (task.isSuccessful()) {
                kategoriList.clear();
                List<String> kategoriNames = new ArrayList<>();
                kategoriNames.add("Pilih Kategori"); // Default item

                QuerySnapshot querySnapshot = task.getResult();
                Log.d("BuatPengaduan", "Query result size: " + querySnapshot.size());

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Log.d("BuatPengaduan", "Processing document: " + document.getId());

                    Kategori kategori = document.toObject(Kategori.class);
                    if (kategori != null) {
                        Log.d("BuatPengaduan", "Kategori name: " + kategori.getName() +
                                ", isActive: " + kategori.isActive());

                        kategori.setId(document.getId());
                        kategoriList.add(kategori);
                        kategoriNames.add(kategori.getName());
                    } else {
                        Log.e("BuatPengaduan", "Failed to convert document to Kategori object");
                    }
                }

                Log.d("BuatPengaduan", "Total kategori loaded: " + kategoriList.size());
                Log.d("BuatPengaduan", "Kategori names: " + kategoriNames.toString());

                // Setup spinner adapter
                kategoriAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, kategoriNames);
                kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerKategori.setAdapter(kategoriAdapter);

                // Notify adapter about data change
                kategoriAdapter.notifyDataSetChanged();

                Log.d("BuatPengaduan", "Spinner adapter set successfully");

            } else {
                Exception exception = task.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                Log.e("BuatPengaduan", "Error loading kategori: " + errorMessage);
                Toast.makeText(this, "Error loading kategori: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Method untuk test koneksi Firestore (tambahkan ini juga)
    private void testFirestoreConnection() {
        Log.d("BuatPengaduan", "Testing Firestore connection...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("kategori")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("BuatPengaduan", "Direct Firestore test successful. Documents: " +
                                task.getResult().size());
                        for (DocumentSnapshot doc : task.getResult()) {
                            Log.d("BuatPengaduan", "Doc ID: " + doc.getId() + ", Data: " + doc.getData());
                        }
                    } else {
                        Log.e("BuatPengaduan", "Direct Firestore test failed", task.getException());
                    }
                });
    }

    private void validateAndSubmitPengaduan() {
        // Get input values
        String judul = etJudul.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String lokasi = etLokasi.getText().toString().trim();

        boolean isKejahatan = rbKejahatan.isChecked();
        String jenisPengaduan = isKejahatan ? "kejahatan" : "umum";

        // Validation
        if (judul.isEmpty()) {
            etJudul.setError("Judul pengaduan harus diisi");
            etJudul.requestFocus();
            return;
        }

        if (judul.length() < 5) {
            etJudul.setError("Judul pengaduan minimal 5 karakter");
            etJudul.requestFocus();
            return;
        }

        if (deskripsi.isEmpty()) {
            etDeskripsi.setError("Deskripsi pengaduan harus diisi");
            etDeskripsi.requestFocus();
            return;
        }

        if (deskripsi.length() < 10) {
            etDeskripsi.setError("Deskripsi pengaduan minimal 10 karakter");
            etDeskripsi.requestFocus();
            return;
        }

        if (lokasi.isEmpty()) {
            etLokasi.setError("Lokasi kejadian harus diisi");
            etLokasi.requestFocus();
            return;
        }

        // Validate kategori for "umum" type
        String kategoriId = null;
        String kategoriName = null;
        if (!isKejahatan) {
            int selectedKategoriIndex = spinnerKategori.getSelectedItemPosition();
            if (selectedKategoriIndex <= 0) {
                Toast.makeText(this, "Silakan pilih kategori pengaduan", Toast.LENGTH_SHORT).show();
                return;
            }

            Kategori selectedKategori = kategoriList.get(selectedKategoriIndex - 1); // -1 because first item is "Pilih Kategori"
            kategoriId = selectedKategori.getId();
            kategoriName = selectedKategori.getName();
        }

        // Validate current user
        if (currentUser == null) {
            Toast.makeText(this, "Error: User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        showSubmitConfirmation(judul, deskripsi, lokasi, jenisPengaduan, kategoriId, kategoriName);
    }

    private void showSubmitConfirmation(String judul, String deskripsi, String lokasi,
                                        String jenisPengaduan, String kategoriId, String kategoriName) {
        String message = "Apakah Anda yakin ingin mengirim pengaduan ini?\n\n" +
                "Jenis: " + (jenisPengaduan.equals("kejahatan") ? "Kejahatan" : "Umum") + "\n" +
                (kategoriName != null ? "Kategori: " + kategoriName + "\n" : "") +
                "Judul: " + judul + "\n" +
                "Lokasi: " + lokasi;

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pengaduan")
                .setMessage(message)
                .setPositiveButton("Ya, Kirim", (dialog, which) ->
                        submitPengaduan(judul, deskripsi, lokasi, jenisPengaduan, kategoriId, kategoriName))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void submitPengaduan(String judul, String deskripsi, String lokasi,
                                 String jenisPengaduan, String kategoriId, String kategoriName) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Mengirim...");

        // Buat objek kosong, lalu set semua field satu per satu agar konsisten dengan Firestore
        Pengaduan pengaduan = new Pengaduan();
        pengaduan.setUserId(currentUser.getId());
        pengaduan.setUserName(currentUser.getName());
        pengaduan.setUserPhone(currentUser.getPhoneNumber());
        pengaduan.setUserKtp(currentUser.getNomorKTP());
        pengaduan.setUserKk(currentUser.getNomorKK());
        pengaduan.setUserAlamat(currentUser.getAlamat());
        pengaduan.setJenisPengaduan(jenisPengaduan);
        pengaduan.setJudul(judul);
        pengaduan.setDeskripsi(deskripsi);
        pengaduan.setLokasi(lokasi);
        pengaduan.setStatus("pending"); // default
        pengaduan.setCreatedAt(new Date());
        pengaduan.setUpdatedAt(new Date());
        pengaduan.setActionTaken(false);

        if ("umum".equals(jenisPengaduan)) {
            pengaduan.setKategoriId(kategoriId);
            pengaduan.setKategoriName(kategoriName);
        }

        firebaseHelper.createPengaduan(pengaduan, task -> {
            btnSubmit.setEnabled(true);
            btnSubmit.setText("KIRIM PENGADUAN");

            if (task.isSuccessful()) {
                String documentId = task.getResult().getId();
                pengaduan.setId(documentId);

                firebaseHelper.updatePengaduanId(documentId, documentId, updateTask -> {
                    showSuccessDialog(); // tetap tampilkan
                });
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Gagal mengirim pengaduan: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Pengaduan Berhasil Dikirim")
                .setMessage("Pengaduan Anda telah berhasil dikirim dan akan segera diproses oleh petugas.\n\n" +
                        "Anda dapat memantau status pengaduan di menu Riwayat Pengaduan.")
                .setPositiveButton("OK", (dialog, which) -> {
                    setResult(RESULT_OK); // Notify calling activity that pengaduan was created
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showExitConfirmation() {
        // Check if user has entered any data
        boolean hasData = !etJudul.getText().toString().trim().isEmpty() ||
                !etDeskripsi.getText().toString().trim().isEmpty() ||
                !etLokasi.getText().toString().trim().isEmpty();

        if (hasData) {
            new AlertDialog.Builder(this)
                    .setTitle("Keluar dari Form")
                    .setMessage("Data yang sudah diisi akan hilang. Apakah Anda yakin ingin keluar?")
                    .setPositiveButton("Ya, Keluar", (dialog, which) -> finish())
                    .setNegativeButton("Batal", null)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showExitConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}