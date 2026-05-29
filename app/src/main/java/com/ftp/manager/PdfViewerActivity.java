package com.ftp.manager;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class PdfViewerActivity extends AppCompatActivity {

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor fileDescriptor;
    private ImageView ivPage;
    private TextView tvPage, tvName;
    private ProgressBar progress;
    private Button btnPrev, btnNext;
    private int currentPageIndex = 0;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        ivPage = findViewById(R.id.iv_pdf_page);
        tvPage = findViewById(R.id.tv_page);
        tvName = findViewById(R.id.tv_pdf_name);
        progress = findViewById(R.id.progress_pdf);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        String path = getIntent().getStringExtra("file_path");
        if (path == null) { finish(); return; }

        File file = new File(path);
        tvName.setText(file.getName());

        try {
            fileDescriptor = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fileDescriptor);
            totalPages = pdfRenderer.getPageCount();
            showPage(0);
        } catch (IOException e) {
            Toast.makeText(this, "PDF açılamadı: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnPrev.setOnClickListener(v -> {
            if (currentPageIndex > 0) showPage(currentPageIndex - 1);
        });

        btnNext.setOnClickListener(v -> {
            if (currentPageIndex < totalPages - 1) showPage(currentPageIndex + 1);
        });
    }

    private void showPage(int index) {
        progress.setVisibility(View.VISIBLE);

        if (currentPage != null) currentPage.close();

        currentPage = pdfRenderer.openPage(index);
        currentPageIndex = index;

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (width * currentPage.getHeight() / (float) currentPage.getWidth());

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(android.graphics.Color.WHITE);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        ivPage.setImageBitmap(bitmap);
        tvPage.setText((index + 1) + " / " + totalPages);

        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < totalPages - 1);

        progress.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (fileDescriptor != null) fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
