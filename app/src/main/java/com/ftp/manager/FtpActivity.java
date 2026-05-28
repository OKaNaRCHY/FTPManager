package com.ftp.manager;

import android.os.Bundle;
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

        etHost = findViewById(R.id.et_host);
        etPort = findViewById(R.id.et_port);
        etUser = findViewById(R.id.et_user);
        etPass = findViewById(R.id.et_pass);
        btnConnect = findViewById(R.id.btn_connect);
        tvStatus = findViewById(R.id.tv_status);
        recycler = findViewById(R.id.recycler_ftp);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        btnConnect.setOnClickListener(v -> {
            if (ftp.isConnected()) disconnect();
            else connect();
        });
    }

    private void connect() {
        String host = etHost.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (host.isEmpty()) { etHost.setError("Gerekli"); return; }
        int port = 21;
        try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}
        if (user.isEmpty()) user = "anonymous";

        final String fUser = user, fPass = pass;
        final int fPort = port;
        tvStatus.setText("Bağlanıyor...");

        executor.execute(() -> {
            try {
                ftp.connect(host, fPort);
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                boolean ok = ftp.login(fUser, fPass);
                final String msg = ok ? "Bağlı: " + host : "Giriş hatası";
                runOnUiThread(() -> {
                    tvStatus.setText(msg);
                    if (ok) { btnConnect.setText("Bağlantıyı Kes"); listFiles("/"); }
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Hata: " + e.getMessage()));
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
                    FileAdapter adapter = new FileAdapter(
                        f -> {
                            if (f.isDirectory()) listFiles(f.getAbsolutePath());
                            else Toast.makeText(this, f.getName(), Toast.LENGTH_SHORT).show();
                        },
                        f -> Toast.makeText(this, f.getName(), Toast.LENGTH_SHORT).show()
                    );
                    adapter.setFiles(list);
                    recycler.setAdapter(adapter);
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Liste hatası: " + e.getMessage()));
            }
        });
    }

    private void disconnect() {
        executor.execute(() -> {
            try { ftp.logout(); ftp.disconnect(); } catch (Exception ignored) {}
            runOnUiThread(() -> {
                tvStatus.setText("Bağlı değil");
                btnConnect.setText("Bağlan");
                recycler.setAdapter(null);
            });
        });
    }

    @Override
    protected void onDestroy() {
        executor.execute(() -> { try { if (ftp.isConnected()) ftp.disconnect(); } catch (Exception ignored) {} });
        executor.shutdown();
        super.onDestroy();
    }
}
