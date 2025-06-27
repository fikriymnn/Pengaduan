package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapters.TindakanPengaduanAdapter;
import com.example.myapplication.fragments.TindakanDialogFragment;
import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Pengaduan;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TindakanPengaduanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TindakanPengaduanAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private FirebaseHelper firebaseHelper;
    private List<Pengaduan> pengaduanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tindakan_pengaduan);

        firebaseHelper = FirebaseHelper.getInstance();
        pengaduanList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadConfirmedPengaduan();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tindakan Pengaduan");
        }

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new TindakanPengaduanAdapter(pengaduanList, this::showTindakanDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadConfirmedPengaduan);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void loadConfirmedPengaduan() {
        swipeRefreshLayout.setRefreshing(true);

        firebaseHelper.getConfirmedPengaduanNotActioned(task -> {
            swipeRefreshLayout.setRefreshing(false);

            if (task.isSuccessful()) {
                pengaduanList.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Pengaduan pengaduan = document.toObject(Pengaduan.class);
                    pengaduan.setId(document.getId());
                    pengaduanList.add(pengaduan);
                }

                adapter.notifyDataSetChanged();
                updateEmptyState();
            } else {
                Toast.makeText(this, "Error loading data: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (pengaduanList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setText("Tidak ada pengaduan yang perlu ditindak");
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showTindakanDialog(Pengaduan pengaduan) {
        TindakanDialogFragment dialog = TindakanDialogFragment.newInstance(pengaduan);
        dialog.setOnTindakanListener(new TindakanDialogFragment.OnTindakanListener() {
            @Override
            public void onTindakanCreated() {
                loadConfirmedPengaduan(); // Refresh list
                Toast.makeText(TindakanPengaduanActivity.this,
                        "Tindakan berhasil dicatat", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TindakanPengaduanActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "TindakanDialog");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConfirmedPengaduan();
    }
}