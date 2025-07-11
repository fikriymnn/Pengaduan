package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.RiwayatPengaduanAdapter;
import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Admin;
import com.example.myapplication.models.Pengaduan;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RiwayatPengaduanAdminActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, tvEmptyState, tvLabelKategori;
    private Spinner spinnerStatus, spinnerKategori, spinnerTindakan;
    private RecyclerView rvRiwayat;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private RiwayatPengaduanAdapter adapter;
    private List<Pengaduan> pengaduanList;
    private List<Pengaduan> filteredList;
    private List<String> kategoriList;

    private String selectedStatus = "Semua";
    private String selectedKategori = "Semua";
    private String selectedTindakan = "Semua";
    private String currentRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_pengaduan_admin);

        firebaseHelper = FirebaseHelper.getInstance();
        pengaduanList = new ArrayList<>();
        filteredList = new ArrayList<>();
        kategoriList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupSpinners();
        setupListeners();
        loadRiwayatPengaduan();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        spinnerStatus = findViewById(R.id.spinner_status);
        spinnerKategori = findViewById(R.id.spinner_kategori);
        spinnerTindakan = findViewById(R.id.spinner_tindakan);
        tvLabelKategori = findViewById(R.id.tv_label_kategori); // pastikan ini ada di layout XML
        rvRiwayat = findViewById(R.id.rv_riwayat);
        progressBar = findViewById(R.id.progress_bar);

        tvTitle.setText("Riwayat Pengaduan");
    }

    private void setupRecyclerView() {
        adapter = new RiwayatPengaduanAdapter(filteredList, pengaduan -> {
            Intent intent = new Intent(this, DetailPengaduanActivity.class);
            intent.putExtra("pengaduan_id", pengaduan.getId());
            intent.putExtra("from_admin", true);
            startActivity(intent);
        });

        rvRiwayat.setLayoutManager(new LinearLayoutManager(this));
        rvRiwayat.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Status Spinner
        List<String> statusList = new ArrayList<>();
        statusList.add("Semua");
        statusList.add("Pending");
        statusList.add("Dikonfirmasi");
        statusList.add("Ditolak");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Kategori Spinner (akan disembunyikan jika admin-kejahatan)
        kategoriList.add("Semua");
        ArrayAdapter<String> kategoriAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, kategoriList);
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(kategoriAdapter);

        // Tindakan Spinner
        List<String> tindakanList = new ArrayList<>();
        tindakanList.add("Semua");
        tindakanList.add("Sudah Ditindak");
        tindakanList.add("Belum Ditindak");
        ArrayAdapter<String> tindakanAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tindakanList);
        tindakanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTindakan.setAdapter(tindakanAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = parent.getItemAtPosition(position).toString();
                filterPengaduan();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerKategori.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedKategori = parent.getItemAtPosition(position).toString();
                filterPengaduan();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTindakan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTindakan = parent.getItemAtPosition(position).toString();
                filterPengaduan();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadKategori() {
        if ("admin-kejahatan".equals(currentRole)) {
            // Tidak perlu load kategori
            return;
        } else if ("admin-dumas".equals(currentRole)) {
            return;
        }

        firebaseHelper.getDb().collection("kategori")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        kategoriList.clear();
                        kategoriList.add("Semua");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nama = document.getString("name");
                            if (nama != null) {
                                kategoriList.add(nama);
                            }
                        }

                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerKategori.getAdapter();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadRiwayatPengaduan() {
        showLoading(true);

        firebaseHelper.getCurrentAdmin(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Admin admin = task.getResult().toObject(Admin.class);
                currentRole = admin.getRole();

                // Sembunyikan kategori jika admin-kejahatan atau admin-dumas
                if ("admin-kejahatan".equals(currentRole) || "admin-dumas".equals(currentRole)) {
                    spinnerKategori.setVisibility(View.GONE);
                    tvLabelKategori.setVisibility(View.GONE);
                }

                loadKategori();

                Query query = firebaseHelper.getDb().collection("pengaduan")
                        .orderBy("createdAt", Query.Direction.DESCENDING);

                if ("admin-umum".equals(currentRole)) {
                    query = query.whereEqualTo("jenisPengaduan", "umum");
                } else if ("admin-kejahatan".equals(currentRole)) {
                    query = query.whereEqualTo("jenisPengaduan", "kejahatan");
                } else if ("admin-dumas".equals(currentRole)) {
                    query = query.whereEqualTo("jenisPengaduan", "dumas");
                }

                query.get().addOnCompleteListener(pengaduanTask -> {
                    showLoading(false);

                    if (pengaduanTask.isSuccessful()) {
                        pengaduanList.clear();

                        for (QueryDocumentSnapshot document : pengaduanTask.getResult()) {
                            Pengaduan pengaduan = document.toObject(Pengaduan.class);
                            pengaduan.setId(document.getId());
                            pengaduanList.add(pengaduan);
                        }

                        filterPengaduan();
                    } else {
                        Toast.makeText(this, "Error loading data: " + pengaduanTask.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                showLoading(false);
                Toast.makeText(this, "Gagal mengambil data admin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterPengaduan() {
        filteredList.clear();

        for (Pengaduan pengaduan : pengaduanList) {
            boolean statusMatch = selectedStatus.equals("Semua") ||
                    matchStatus(pengaduan.getStatus(), selectedStatus);

            boolean kategoriMatch = selectedKategori.equals("Semua") ||
                    matchKategori(pengaduan.getKategoriName(), selectedKategori);

            boolean tindakanMatch = selectedTindakan.equals("Semua") ||
                    matchTindakan(pengaduan.isActionTaken(), selectedTindakan);

            if (statusMatch && kategoriMatch && tindakanMatch) {
                filteredList.add(pengaduan);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean matchStatus(String status, String selectedStatus) {
        switch (selectedStatus) {
            case "Pending":
                return "pending".equals(status);
            case "Dikonfirmasi":
                return "confirmed".equals(status);
            case "Ditolak":
                return "rejected".equals(status);
            default:
                return true;
        }
    }

    private boolean matchKategori(String kategoriName, String selectedKategori) {
        if (kategoriName == null) return false;
        return kategoriName.equals(selectedKategori);
    }

    private boolean matchTindakan(boolean actionTaken, String selectedTindakan) {
        switch (selectedTindakan) {
            case "Sudah Ditindak": return actionTaken;
            case "Belum Ditindak": return !actionTaken;
            default: return true;
        }
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvRiwayat.setVisibility(View.GONE);

            if (selectedStatus.equals("Semua") && selectedKategori.equals("Semua") && selectedTindakan.equals("Semua")) {
                tvEmptyState.setText("Belum ada pengaduan");
            } else {
                tvEmptyState.setText("Tidak ada pengaduan dengan filter yang dipilih");
            }
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvRiwayat.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvRiwayat.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRiwayatPengaduan();
    }
}
