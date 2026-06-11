package com.ftp.manager.webdav;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ftp.manager.R;

public class WebDavActivity extends AppCompatActivity {

    private TextView statusText;
    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webdav);

        statusText = findViewById(R.id.webdav_status);
        startButton = findViewById(R.id.webdav_start);
        stopButton = findViewById(R.id.webdav_stop);

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, WebDavService.class);
            startService(intent);
            statusText.setText("WebDAV çalışıyor: http://localhost:8080");
        });

        stopButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, WebDavService.class);
            stopService(intent);
            statusText.setText("WebDAV durduruldu");
        });
    }
}
