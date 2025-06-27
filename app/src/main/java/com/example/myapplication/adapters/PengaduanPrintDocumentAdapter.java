package com.example.myapplication.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import com.example.myapplication.models.Pengaduan;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PengaduanPrintDocumentAdapter extends PrintDocumentAdapter {

    private Context context;
    private Pengaduan pengaduan;
    private PdfDocument pdfDocument;
    private int pageHeight;
    private int pageWidth;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));

    public PengaduanPrintDocumentAdapter(Context context, Pengaduan pengaduan) {
        this.context = context;
        this.pengaduan = pengaduan;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, Bundle extras) {

        pdfDocument = new PdfDocument();
        pageHeight = newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
        pageWidth = newAttributes.getMediaSize().getWidthMils() / 1000 * 72;

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo info = new PrintDocumentInfo
                .Builder("Pengaduan_" + pengaduan.getId() + ".pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build();
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal,
                        WriteResultCallback callback) {

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        int y = 50;

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(18);
        canvas.drawText("DETAIL PENGADUAN", 50, y, paint);
        y += 40;

        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(12);

        canvas.drawText("Judul: " + pengaduan.getJudul(), 50, y, paint); y += 20;
        canvas.drawText("Kategori: " + (pengaduan.getKategoriName() != null ? pengaduan.getKategoriName() : pengaduan.getJenisPengaduan()), 50, y, paint); y += 20;
        canvas.drawText("Jenis: " + pengaduan.getJenisPengaduanDisplayName(), 50, y, paint); y += 20;
        canvas.drawText("Lokasi: " + pengaduan.getLokasi(), 50, y, paint); y += 20;
        canvas.drawText("Status: " + pengaduan.getStatusDisplayName(), 50, y, paint); y += 20;
        canvas.drawText("Nama Pelapor: " + pengaduan.getUserName(), 50, y, paint); y += 20;
        canvas.drawText("Telepon: " + pengaduan.getUserPhone(), 50, y, paint); y += 20;
        if (pengaduan.getCreatedAt() != null) {
            canvas.drawText("Tanggal: " + sdf.format(pengaduan.getCreatedAt()), 50, y, paint);
            y += 20;
        }
        canvas.drawText("Deskripsi:", 50, y, paint); y += 20;
        String[] lines = pengaduan.getDeskripsi() != null ? pengaduan.getDeskripsi().split("\\n") : new String[]{"-"};
        for (String line : lines) {
            canvas.drawText(line, 70, y, paint);
            y += 20;
        }

        pdfDocument.finishPage(page);

        try (FileOutputStream fos = new FileOutputStream(destination.getFileDescriptor())) {
            pdfDocument.writeTo(fos);
        } catch (IOException e) {
            Log.e("PrintAdapter", "Error writing PDF", e);
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            pdfDocument.close();
            pdfDocument = null;
        }

        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
    }
}
