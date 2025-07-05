// Final version of BuatPengaduanActivity.java with "Dumas" support and role filtering

package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Kategori;
import com.example.myapplication.models.Pengaduan;
import com.example.myapplication.models.User;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuatPengaduanActivity extends AppCompatActivity {

    private RadioGroup rgJenisPengaduan;
    private RadioButton rbKejahatan, rbUmum, rbDumas;
    private View layoutKategori;
    private Spinner spinnerKategori;
    private TextInputEditText etJudul, etDeskripsi, etLokasi;
    private View btnSubmit;

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
        setupListeners();
        loadUserData();
        loadKategori();
    }

    private void initViews() {
        rgJenisPengaduan = findViewById(R.id.rg_jenis_pengaduan);
        rbKejahatan = findViewById(R.id.rb_kejahatan);
        rbUmum = findViewById(R.id.rb_umum);
        rbDumas = findViewById(R.id.rb_dumas);
        layoutKategori = findViewById(R.id.layout_kategori);
        spinnerKategori = findViewById(R.id.spinner_kategori);
        etJudul = findViewById(R.id.et_judul);
        etDeskripsi = findViewById(R.id.et_deskripsi);
        etLokasi = findViewById(R.id.et_lokasi);
        btnSubmit = findViewById(R.id.btn_submit);

        rbKejahatan.setChecked(true);
        layoutKategori.setVisibility(View.GONE);
    }

    private void setupListeners() {
        rgJenisPengaduan.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_umum) {
                layoutKategori.setVisibility(View.VISIBLE);
            } else {
                layoutKategori.setVisibility(View.GONE);
            }
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmitPengaduan());
    }

    private void loadUserData() {
        firebaseHelper.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                currentUser = task.getResult().toObject(User.class);
            } else {
                Toast.makeText(this, "Gagal memuat data user", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadKategori() {
        firebaseHelper.getKategori(task -> {
            if (task.isSuccessful()) {
                kategoriList.clear();
                List<String> kategoriNames = new ArrayList<>();
                kategoriNames.add("Pilih Kategori");

                for (Kategori k : task.getResult().toObjects(Kategori.class)) {
                    kategoriList.add(k);
                    kategoriNames.add(k.getName());
                }

                kategoriAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, kategoriNames);
                kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerKategori.setAdapter(kategoriAdapter);
            }
        });
    }

    private void validateAndSubmitPengaduan() {
        String judul = etJudul.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String lokasi = etLokasi.getText().toString().trim();

        String jenisPengaduan;
        if (rbKejahatan.isChecked()) {
            jenisPengaduan = "kejahatan";
        } else if (rbUmum.isChecked()) {
            jenisPengaduan = "umum";
        } else if (rbDumas.isChecked()) {
            jenisPengaduan = "dumas";
        } else {
            Toast.makeText(this, "Pilih jenis pengaduan", Toast.LENGTH_SHORT).show();
            return;
        }

        if (judul.isEmpty() || judul.length() < 5) {
            etJudul.setError("Judul minimal 5 karakter");
            etJudul.requestFocus();
            return;
        }

        if (deskripsi.isEmpty() || deskripsi.length() < 10) {
            etDeskripsi.setError("Deskripsi minimal 10 karakter");
            etDeskripsi.requestFocus();
            return;
        }

        if (lokasi.isEmpty()) {
            etLokasi.setError("Lokasi harus diisi");
            etLokasi.requestFocus();
            return;
        }

        String kategoriId = null;
        String kategoriName = null;
        if ("umum".equals(jenisPengaduan)) {
            int pos = spinnerKategori.getSelectedItemPosition();
            if (pos <= 0) {
                Toast.makeText(this, "Pilih kategori pengaduan", Toast.LENGTH_SHORT).show();
                return;
            }
            Kategori selected = kategoriList.get(pos - 1);
            kategoriId = selected.getId();
            kategoriName = selected.getName();
        }

        if (currentUser == null) {
            Toast.makeText(this, "Data user belum dimuat", Toast.LENGTH_SHORT).show();
            return;
        }

        Pengaduan p = new Pengaduan();
        p.setUserId(currentUser.getId());
        p.setUserName(currentUser.getName());
        p.setUserPhone(currentUser.getPhoneNumber());
        p.setUserKtp(currentUser.getNomorKTP());
        p.setUserKk(currentUser.getNomorKK());
        p.setUserAlamat(currentUser.getAlamat());
        p.setJenisPengaduan(jenisPengaduan);
        p.setJudul(judul);
        p.setDeskripsi(deskripsi);
        p.setLokasi(lokasi);
        p.setStatus("pending");
        p.setCreatedAt(new Date());
        p.setUpdatedAt(new Date());
        p.setActionTaken(false);
        p.setKategoriId(kategoriId);
        p.setKategoriName(kategoriName);

        firebaseHelper.createPengaduan(p, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Pengaduan berhasil dikirim", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal mengirim pengaduan", Toast.LENGTH_SHORT).show();
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
