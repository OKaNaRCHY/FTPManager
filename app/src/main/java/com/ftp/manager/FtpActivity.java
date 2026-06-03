package com.ftp.manager;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FtpActivity extends AppCompatActivity {

    private EditText etHost, etPort, etUser, etPass;
    private Button btnConnect;
    private TextView tvStatus;
    private RecyclerView recycler;
    private FTPClient ftp = new FTPClient();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String currentPath = "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.ftp_connect_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etHost = findViewById(R.id.et_host);
        etPort = findViewById(R.id.et_port);
        etUser = findViewById(R.id.et_user);
        etPass = findViewById(R.id.et_pass);
        btnConnect = findViewById(R.id.btn_connect);
        tvStatus = findViewById(R.id.tv_status);
        recycler = findViewById(R.id.recycler_ftp);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        String ip = getWifiIp();
        if (!ip.isEmpty()) {
            etHost.setText(ip);
            etHost.setHint(getString(R.string.ip_label) + ip);
        }

        btnConnect.setOnClickListener(v -> {
            if (ftp.isConnected()) disconnect();
            else connect();
        });
    }

    private String getWifiIp() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                if (ip != 0) return Formatter.formatIpAddress(ip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void connect() {
        String host = etHost.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (host.isEmpty()) { etHost.setError(getString(R.string.error)); return; }
        int port = 21;
        try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}
        if (user.isEmpty()) user = "anonymous";

        final String fUser = user, fPass = pass;
        final int fPort = port;
        tvStatus.setText(getString(R.string.searching));

        executor.execute(() -> {
            try {
                ftp.connect(host, fPort);
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                boolean ok = ftp.login(fUser, fPass);
                final String msg = ok ? "✅ " + host : "❌ " + getString(R.string.error);
                runOnUiThread(() -> {
                    tvStatus.setText(msg);
                    if (ok) {
                        btnConnect.setText(R.string.disconnect);
                        listFiles("/");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("❌ " + e.getMessage()));
            }
        });
    }

    private void listFiles(String path) {
        executor.execute(() -> {
            try {
                FTPFile[] files = ftp.listFiles(path);
                List<File> list = new ArrayList<>();
                if (files != null) {
                    for (FTPFile f : files) {
                        if (!f.getName().equals(".") && !f.getName().equals("..")) {
                            list.add(new File(path + "/" + f.getName()));
                        }
                    }
                }
                currentPath = path;
                runOnUiThread(() -> {
                    tvStatus.setText("📂 " + path);
                    FileAdapter adapter = new FileAdapter(f -> {
                        if (f.isDirectory()) listFiles(f.getAbsolutePath());
                        else Toast.makeText(this, f.getName(), Toast.LENGTH_SHORT).show();
                    });
                    adapter.setFiles(list);
                    recycler.setAdapter(adapter);
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("❌ " + e.getMessage()));
            }
        });
    }

    private void disconnect() {
        executor.execute(() -> {
            try { ftp.logout(); ftp.disconnect(); } catch (Exception ignored) {}
            runOnUiThread(() -> {
                tvStatus.setText(getString(R.string.not_connected));
                btnConnect.setText(R.string.connect);
                recycler.setAdapter(null);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        executor.execute(() -> {
            try { if (ftp.isConnected()) ftp.disconnect(); } catch (Exception ignored) {}
        });
        executor.shutdown();
        super.onDestroy();
    }
}
