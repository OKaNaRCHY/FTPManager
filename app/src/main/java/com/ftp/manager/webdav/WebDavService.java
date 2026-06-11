package com.ftp.manager.webdav;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ftp.manager.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

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
            statusText.setText("WebDAV çalışıyor: http://" + getLocalIpAddress() + ":8080");
        });

        stopButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, WebDavService.class);
            stopService(intent);
            statusText.setText("WebDAV durduruldu");
        });
    }

    /**
     * Cihazın yerel IP adresini döndürür
     */
    private String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "localhost";
    }
}
