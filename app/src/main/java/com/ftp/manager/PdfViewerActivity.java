package com.ftp.manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        WebView webView = findViewById(R.id.web_view);
        ProgressBar progress = findViewById(R.id.progress_pdf);
        TextView tvName = findViewById(R.id.tv_pdf_name);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        String path = getIntent().getStringExtra("file_path");
        if (path == null) { finish(); return; }

        File file = new File(path);
        tvName.setText(file.getName());

        // Önce sistem PDF uygulamasıyla aç
        try {
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            finish();
            return;
        } catch (Exception e) {
            // Sistem uygulaması yoksa WebView ile dene
        }

        // WebView ile aç
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                Toast.makeText(PdfViewerActivity.this,
                        "PDF açılamadı: " + description, Toast.LENGTH_LONG).show();
            }
        });

        try {
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file);
            webView.loadUrl(uri.toString());
        } catch (Exception e) {
            Toast.makeText(this, "PDF açılamadı", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
