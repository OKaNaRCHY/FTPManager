package com.ftp.manager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvPath;
    private RecyclerView recycler;
    private FileAdapter adapter;
    private File currentDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPath = findViewById(R.id.tv_path);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FileAdapter(item -> {
            if (item.isDirectory()) {
                loadDir(item);
            } else {
                openFile(item);
            }
        }, item -> showContextMenu(item));
        recycler.setAdapter(adapter);

        Button btnFtp = findViewById(R.id.btn_ftp);
        btnFtp.setOnClickListener(v -> startActivity(new Intent(this, FtpActivity.class)));

        Button btnFolder = findViewById(R.id.btn_new_folder);
        btnFolder.setOnClickListener(v -> createFolder());

        requestPermissions();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                } catch (Exception e) {
                    startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                }
            } else {
                loadDir(Environment.getExternalStorageDirectory());
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                loadDir(Environment.getExternalStorageDirectory());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            if (currentDir == null) loadDir(Environment.getExternalStorageDirectory());
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            loadDir(Environment.getExternalStorageDirectory());
        }
    }

    void loadDir(File dir) {
        currentDir = dir;
        tvPath.setText(dir.getAbsolutePath());
        File[] files = dir.listFiles();
        List<File> list = new ArrayList<>();
        if (files != null) for (File f : files) if (!f.isHidden()) list.add(f);
        Collections.sort(list, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        adapter.setFiles(list);
    }

    private void openFile(File f) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(f), getMime(f.getName()));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Açılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMime(String name) {
        String e = name.contains(".") ? name.substring(name.lastIndexOf('.')+1).toLowerCase() : "";
        switch (e) {
            case "jpg": case "jpeg": case "png": return "image/*";
            case "mp4": case "mkv": return "video/*";
            case "mp3": return "audio/*";
            case "pdf": return "application/pdf";
            case "txt": return "text/plain";
            case "apk": return "application/vnd.android.package-archive";
            default: return "*/*";
        }
    }

    private void showContextMenu(File f) {
        new AlertDialog.Builder(this)
                .setTitle(f.getName())
                .setItems(new String[]{"Sil", "Yeniden Adlandır"}, (d, w) -> {
                    if (w == 0) deleteFile(f);
                    else renameFile(f);
                }).show();
    }

    private void deleteFile(File f) {
        new AlertDialog.Builder(this)
                .setMessage("\"" + f.getName() + "\" silinsin mi?")
                .setPositiveButton("Sil", (d, w) -> {
                    if (f.delete()) loadDir(currentDir);
                    else Toast.makeText(this, "Silinemedi", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("İptal", null).show();
    }

    private void renameFile(File f) {
        EditText et = new EditText(this);
        et.setText(f.getName());
        new AlertDialog.Builder(this).setTitle("Yeniden Adlandır").setView(et)
                .setPositiveButton("Tamam", (d, w) -> {
                    File nf = new File(f.getParent(), et.getText().toString().trim());
                    if (f.renameTo(nf)) loadDir(currentDir);
                    else Toast.makeText(this, "Hata", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("İptal", null).show();
    }

    private void createFolder() {
        EditText et = new EditText(this);
        et.setHint("Klasör adı");
        new AlertDialog.Builder(this).setTitle("Yeni Klasör").setView(et)
                .setPositiveButton("Oluştur", (d, w) -> {
                    File nf = new File(currentDir, et.getText().toString().trim());
                    if (nf.mkdirs()) loadDir(currentDir);
                    else Toast.makeText(this, "Hata", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("İptal", null).show();
    }

    @Override
    public void onBackPressed() {
        File parent = currentDir != null ? currentDir.getParentFile() : null;
        if (parent != null && !currentDir.equals(Environment.getExternalStorageDirectory())) {
            loadDir(parent);
        } else {
            super.onBackPressed();
        }
    }
}
