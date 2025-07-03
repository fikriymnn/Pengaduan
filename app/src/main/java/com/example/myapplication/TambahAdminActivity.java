package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Admin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TambahAdminActivity extends AppCompatActivity {

    private EditText etNama, etEmail, etPassword, etPhone;
    private Spinner spinnerRole;
    private Button btnSimpan;
    private FirebaseAuth auth;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_admin);

        auth = FirebaseAuth.getInstance();
        firebaseHelper = FirebaseHelper.getInstance();

        etNama = findViewById(R.id.et_nama);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPhone = findViewById(R.id.et_phone);
        spinnerRole = findViewById(R.id.spinner_role);
        btnSimpan = findViewById(R.id.btn_simpan);

        setupSpinner();

        btnSimpan.setOnClickListener(v -> tambahAdminBaru());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"admin-kejahatan", "admin-umum"});
        spinnerRole.setAdapter(adapter);
    }

    private void tambahAdminBaru() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (!validateInput(nama, email, password, phone)) return;

        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("SIMPAN");

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        Admin admin = new Admin(nama, email, role, phone);
                        admin.setId(firebaseUser.getUid());

                        firebaseHelper.getDb().collection("admins")
                                .document(admin.getId())
                                .set(admin)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Admin berhasil ditambahkan", Toast.LENGTH_LONG).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal menyimpan data admin", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Gagal membuat akun admin: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String nama, String email, String password, String phone) {
        if (TextUtils.isEmpty(nama)) {
            etNama.setError("Nama wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email tidak valid");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Nomor HP wajib diisi");
            return false;
        }
        return true;
    }
}
