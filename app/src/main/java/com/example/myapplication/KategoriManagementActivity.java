package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.KategoriAdapter;
import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Kategori;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KategoriManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private FirebaseHelper firebaseHelper;
    private List<Kategori> kategoriList = new ArrayList<>();
    private KategoriAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kategori_management);

        firebaseHelper = FirebaseHelper.getInstance();
        recyclerView = findViewById(R.id.recycler_view);
        fabAdd = findViewById(R.id.fab_add);

        adapter = new KategoriAdapter(kategoriList, new KategoriAdapter.KategoriActionListener() {
            @Override
            public void onEdit(Kategori kategori) {
                showAddEditDialog(kategori);
            }

            @Override
            public void onDelete(Kategori kategori) {
                showDeleteConfirmation(kategori);
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        loadKategori();
    }

    private void loadKategori() {
        firebaseHelper.getAllKategori(task -> {
            if (task.isSuccessful()) {
                kategoriList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    Kategori k = doc.toObject(Kategori.class);
                    k.setId(doc.getId());
                    kategoriList.add(k);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditDialog(Kategori kategori) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_kategori, null);
        EditText etNama = dialogView.findViewById(R.id.et_kategori_nama);
        EditText etDeskripsi = dialogView.findViewById(R.id.et_kategori_deskripsi);
        CheckBox cbAktif = dialogView.findViewById(R.id.cb_aktif);

        if (kategori != null) {
            etNama.setText(kategori.getName());
            etDeskripsi.setText(kategori.getDescription());
            cbAktif.setChecked(kategori.isActive());
        }

        new AlertDialog.Builder(this)
                .setTitle(kategori == null ? "Tambah Kategori" : "Edit Kategori")
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String nama = etNama.getText().toString().trim();
                    String deskripsi = etDeskripsi.getText().toString().trim();
                    boolean aktif = cbAktif.isChecked();

                    if (nama.isEmpty()) {
                        Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (kategori == null) {
                        // Tambah
                        Kategori newKategori = new Kategori();
                        newKategori.setName(nama);
                        newKategori.setDescription(deskripsi);
                        newKategori.setActive(aktif);
                        newKategori.setCreatedAt(new Date());
                        firebaseHelper.createKategori(newKategori, task -> loadKategori());
                    } else {
                        // Edit
                        kategori.setName(nama);
                        kategori.setDescription(deskripsi);
                        kategori.setActive(aktif);
                        firebaseHelper.updateKategori(kategori.getId(), kategori, task -> loadKategori());
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }


    private void showDeleteConfirmation(Kategori kategori) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Kategori")
                .setMessage("Yakin ingin menghapus kategori \"" + kategori.getName() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    firebaseHelper.deleteKategori(kategori.getId(), task -> loadKategori());
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
