package com.ftp.manager;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
            }
        });

        // Google Docs ile PDF görüntüle
        String url = "https://docs.google.com/gview?embedded=true&url=file://" + path;
        webView.loadUrl(url);
    }
}
