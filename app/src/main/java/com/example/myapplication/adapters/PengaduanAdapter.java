package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Pengaduan;
import com.example.myapplication.R;

import java.util.List;

public class PengaduanAdapter extends RecyclerView.Adapter<PengaduanAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Pengaduan pengaduan);
    }

    private List<Pengaduan> list;
    private OnItemClickListener listener;

    public PengaduanAdapter(List<Pengaduan> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvDeskripsi, tvJenisPengaduan, tvPhone, tvTanggal, tvNamaPelapor, tvLokasi;
        public ViewHolder(View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tv_judul);
            tvDeskripsi = itemView.findViewById(R.id.tv_deskripsi);
            tvJenisPengaduan = itemView.findViewById(R.id.tv_jenis_pengaduan);
            tvNamaPelapor = itemView.findViewById(R.id.tv_nama_pelapor);
            tvLokasi = itemView.findViewById(R.id.tv_lokasi);
            tvTanggal = itemView.findViewById(R.id.tv_tanggal);
            tvPhone = itemView.findViewById(R.id.tv_phone);
        }

        public void bind(Pengaduan pengaduan, OnItemClickListener listener) {
            tvJudul.setText(pengaduan.getJudul());
            tvDeskripsi.setText(pengaduan.getDeskripsi());
            tvJenisPengaduan.setText(pengaduan.getJenisPengaduan());
            tvNamaPelapor.setText(pengaduan.getUserName());
            tvLokasi.setText(pengaduan.getLokasi());
            tvTanggal.setText(pengaduan.getCreatedAt().toString());
            tvPhone.setText(pengaduan.getUserPhone());
            itemView.setOnClickListener(v -> listener.onItemClick(pengaduan));
        }
    }

    @NonNull
    @Override
    public PengaduanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pengaduan_pending, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
