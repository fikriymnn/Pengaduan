package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail;
    private EditText etNomorKTP, etNomorKK, etAlamat;
    private Button btnSimpan;
    private FirebaseHelper firebaseHelper;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        etNomorKTP = findViewById(R.id.et_nomor_ktp);
        etNomorKK = findViewById(R.id.et_nomor_kk);
        etAlamat = findViewById(R.id.et_alamat);
        btnSimpan = findViewById(R.id.btn_simpan);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnSimpan.setOnClickListener(v -> saveProfileData());
    }

    private void loadUserData() {
        firebaseHelper.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                currentUser = document.toObject(User.class);

                if (currentUser != null) {
                    // Display existing data
                    tvName.setText(currentUser.getName());
                    tvEmail.setText(currentUser.getEmail());

                    // Load KTP, KK, and Alamat if exists
                    String nomorKTP = document.getString("nomorKTP");
                    String nomorKK = document.getString("nomorKK");
                    String alamat = document.getString("alamat");

                    if (nomorKTP != null) {
                        etNomorKTP.setText(nomorKTP);
                    }

                    if (nomorKK != null) {
                        etNomorKK.setText(nomorKK);
                    }

                    if (alamat != null) {
                        etAlamat.setText(alamat);
                    }
                }
            } else {
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveProfileData() {
        String nomorKTP = etNomorKTP.getText().toString().trim();
        String nomorKK = etNomorKK.getText().toString().trim();
        String alamat = etAlamat.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(nomorKTP)) {
            etNomorKTP.setError("Nomor KTP tidak boleh kosong");
            etNomorKTP.requestFocus();
            return;
        }

        if (nomorKTP.length() != 16) {
            etNomorKTP.setError("Nomor KTP harus 16 digit");
            etNomorKTP.requestFocus();
            return;
        }

        if (!nomorKTP.matches("\\d+")) {
            etNomorKTP.setError("Nomor KTP hanya boleh berisi angka");
            etNomorKTP.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nomorKK)) {
            etNomorKK.setError("Nomor KK tidak boleh kosong");
            etNomorKK.requestFocus();
            return;
        }

        if (nomorKK.length() != 16) {
            etNomorKK.setError("Nomor KK harus 16 digit");
            etNomorKK.requestFocus();
            return;
        }

        if (!nomorKK.matches("\\d+")) {
            etNomorKK.setError("Nomor KK hanya boleh berisi angka");
            etNomorKK.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(alamat)) {
            etAlamat.setError("Alamat tidak boleh kosong");
            etAlamat.requestFocus();
            return;
        }

        if (alamat.length() < 10) {
            etAlamat.setError("Alamat minimal 10 karakter");
            etAlamat.requestFocus();
            return;
        }

        // Disable button to prevent multiple clicks
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Prepare user data update
        Map<String, Object> updates = new HashMap<>();
        updates.put("nomorKTP", nomorKTP);
        updates.put("nomorKK", nomorKK);
        updates.put("alamat", alamat);

        // Update user data
        firebaseHelper.updateUserProfile(updates, task -> {
            btnSimpan.setEnabled(true);
            btnSimpan.setText("Simpan");

            if (task.isSuccessful()) {
                Toast.makeText(this, "Data profile berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish(); // Go back to previous activity
            } else {
                Toast.makeText(this, "Gagal menyimpan data profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}