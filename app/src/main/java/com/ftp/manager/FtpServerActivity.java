package com.ftp.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FtpServerActivity extends AppCompatActivity {

    private FtpServerService ftpService;
    private boolean bound = false;
    private TextView tvStatus, tvIp;
    private Button btnStart, btnStop;
    private EditText etPort, etUser, etPass;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FtpServerService.LocalBinder binder = (FtpServerService.LocalBinder) service;
            ftpService = binder.getService();
            bound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_server);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.ftp_server_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvStatus = findViewById(R.id.tv_status);
        tvIp = findViewById(R.id.tv_ip);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        etPort = findViewById(R.id.et_port);
        etUser = findViewById(R.id.et_user);
        etPass = findViewById(R.id.et_pass);

        String ip = getWifiIp();
        tvIp.setText(getString(R.string.ip_label) +
                (ip.isEmpty() ? getString(R.string.wifi_not_connected) : ip));
        tvStatus.setText(R.string.server_stopped);

        btnStart.setOnClickListener(v -> startServer());
        btnStop.setOnClickListener(v -> stopServer());

        Intent intent = new Intent(this, FtpServerService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void startServer() {
        int port = 2121;
        try { port = Integer.parseInt(etPort.getText().toString()); }
        catch (Exception ignored) {}

        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if (user.isEmpty()) user = "admin";
        if (pass.isEmpty()) pass = "1234";

        Intent intent = new Intent(this, FtpServerService.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        if (bound && ftpService != null) {
            String rootDir = android.os.Environment
                    .getExternalStorageDirectory().getAbsolutePath();
            ftpService.startFtp(port, user, pass, rootDir);

            String ip = getWifiIp();
            tvStatus.setText(getString(R.string.server_running) +
                    "\nftp://" + ip + ":" + port);
            tvIp.setText(getString(R.string.ip_label) + ip + "  Port: " + port);
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            Toast.makeText(this, getString(R.string.ftp_started),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopServer() {
        if (bound && ftpService != null) {
            ftpService.stopFtp();
            tvStatus.setText(R.string.server_stopped);
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            Toast.makeText(this, getString(R.string.ftp_stopped),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (bound && ftpService != null && ftpService.isRunning()) {
            tvStatus.setText(R.string.server_running);
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        }
    }

    private String getWifiIp() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wm != null && wm.isWifiEnabled()) {
                WifiInfo info = wm.getConnectionInfo();
                int ip = info.getIpAddress();
                if (ip != 0) return Formatter.formatIpAddress(ip);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (bound) { unbindService(conn); bound = false; }
        super.onDestroy();
    }
}
