package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Pengaduan;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TindakanPengaduanAdapter extends RecyclerView.Adapter<TindakanPengaduanAdapter.ViewHolder> {

    private List<Pengaduan> pengaduanList;
    private OnItemClickListener onItemClickListener;
    private SimpleDateFormat dateFormat;

    public interface OnItemClickListener {
        void onItemClick(Pengaduan pengaduan);
    }

    public TindakanPengaduanAdapter(List<Pengaduan> pengaduanList, OnItemClickListener listener) {
        this.pengaduanList = pengaduanList;
        this.onItemClickListener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tindakan_pengaduan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pengaduan pengaduan = pengaduanList.get(position);

        holder.tvJudul.setText(pengaduan.getJudul());
        holder.tvDeskripsi.setText(pengaduan.getDeskripsi());
        holder.tvLokasi.setText(pengaduan.getLokasi());
        holder.tvUserName.setText("Dilaporkan oleh: " + pengaduan.getUserName());
        holder.tvJenisPengaduan.setText(pengaduan.getJenisPengaduanDisplayName());

        if (pengaduan.getKategoriName() != null && !pengaduan.getKategoriName().isEmpty()) {
            holder.tvKategori.setVisibility(View.VISIBLE);
            holder.tvKategori.setText("Kategori: " + pengaduan.getKategoriName());
        } else {
            holder.tvKategori.setVisibility(View.GONE);
        }

        holder.tvTanggal.setText("Dilaporkan: " + dateFormat.format(pengaduan.getCreatedAt()));

        holder.btnTindak.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(pengaduan);
            }
        });

        holder.cardView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(pengaduan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pengaduanList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvJudul, tvDeskripsi, tvLokasi, tvUserName, tvJenisPengaduan, tvKategori, tvTanggal;
        Button btnTindak;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvJudul = itemView.findViewById(R.id.tv_judul);
            tvDeskripsi = itemView.findViewById(R.id.tv_deskripsi);
            tvLokasi = itemView.findViewById(R.id.tv_lokasi);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvJenisPengaduan = itemView.findViewById(R.id.tv_jenis_pengaduan);
            tvKategori = itemView.findViewById(R.id.tv_kategori);
            tvTanggal = itemView.findViewById(R.id.tv_tanggal);
            btnTindak = itemView.findViewById(R.id.btn_tindak);
        }
    }
}