package com.ftp.manager;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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
    private TextView tvPage, tvName, tvZoom;
    private ProgressBar progress;
    private Button btnPrev, btnNext, btnZoomIn, btnZoomOut;
    private int currentPageIndex = 0;
    private int totalPages = 0;

    private float scaleFactor = 1.0f;
    private float lastX = 0, lastY = 0;
    private float translateX = 0, translateY = 0;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 5.0f;

    private ScaleGestureDetector scaleDetector;
    private Matrix matrix = new Matrix();
    private Bitmap currentBitmap;

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
        btnZoomIn = findViewById(R.id.btn_zoom_in);
        btnZoomOut = findViewById(R.id.btn_zoom_out);
        tvZoom = findViewById(R.id.tv_zoom);

        ivPage.setScaleType(ImageView.ScaleType.MATRIX);

        // Pinch-to-zoom + Pan
        scaleDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float prevScale = scaleFactor;
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                float scaleChange = scaleFactor / prevScale;
                translateX = focusX - scaleChange * (focusX - translateX);
                translateY = focusY - scaleChange * (focusY - translateY);

                applyMatrix();
                return true;
            }
        });

        ivPage.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);

            if (!scaleDetector.isInProgress()) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (scaleFactor > 1.0f) {
                            translateX += event.getX() - lastX;
                            translateY += event.getY() - lastY;
                            applyMatrix();
                        }
                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                }
            }
            return true;
        });

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
            if (currentPageIndex > 0) {
                resetZoom();
                showPage(currentPageIndex - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPageIndex < totalPages - 1) {
                resetZoom();
                showPage(currentPageIndex + 1);
            }
        });

        btnZoomIn.setOnClickListener(v -> {
            scaleFactor = Math.min(scaleFactor + 0.25f, MAX_SCALE);
            applyMatrix();
        });

        btnZoomOut.setOnClickListener(v -> {
            scaleFactor = Math.max(scaleFactor - 0.25f, MIN_SCALE);
            if (scaleFactor <= 1.0f) { translateX = 0; translateY = 0; }
            applyMatrix();
        });
    }

    private void showPage(int index) {
        progress.setVisibility(View.VISIBLE);

        if (currentPage != null) currentPage.close();
        currentPage = pdfRenderer.openPage(index);
        currentPageIndex = index;

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (width * currentPage.getHeight() / (float) currentPage.getWidth());

        currentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        currentBitmap.eraseColor(android.graphics.Color.WHITE);
        currentPage.render(currentBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        ivPage.setImageBitmap(currentBitmap);
        applyMatrix();

        tvPage.setText((index + 1) + " / " + totalPages);
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < totalPages - 1);
        progress.setVisibility(View.GONE);
    }

    private void applyMatrix() {
        matrix.reset();
        matrix.setScale(scaleFactor, scaleFactor);
        matrix.postTranslate(translateX, translateY);
        ivPage.setImageMatrix(matrix);
        tvZoom.setText((int)(scaleFactor * 100) + "%");
    }

    private void resetZoom() {
        scaleFactor = 1.0f;
        translateX = 0;
        translateY = 0;
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
