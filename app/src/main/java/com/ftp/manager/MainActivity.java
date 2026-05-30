package com.ftp.manager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvPath;
    private RecyclerView recycler;
    private FileAdapter adapter;
    private File currentDir;
    private SharedPreferences prefs;
    private List<File> allFiles = new ArrayList<>();
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("FTPManagerPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("FTP Manager");
        }

        tvPath = findViewById(R.id.tv_path);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FileAdapter(item -> {
            if (item.isDirectory()) loadDir(item);
            else openFile(item);
        }, item -> showContextMenu(item));
        recycler.setAdapter(adapter);

        requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentDir != null) loadDir(currentDir);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                Environment.isExternalStorageManager()) {
            loadDir(Environment.getExternalStorageDirectory());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setQueryHint("Dosya ara...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchFiles(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchQuery = newText;
                    if (newText.isEmpty()) {
                        adapter.setFiles(allFiles);
                        tvPath.setText(currentDir != null ?
                                currentDir.getAbsolutePath() : "");
                    } else {
                        searchFiles(newText);
                    }
                    return true;
                }
            });

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    searchQuery = "";
                    adapter.setFiles(allFiles);
                    tvPath.setText(currentDir != null ?
                            currentDir.getAbsolutePath() : "");
                    return true;
                }
            });
        }
        return true;
    }

    // Tüm alt klasörlerde recursive arama
    private void searchFiles(String query) {
        if (query.isEmpty() || currentDir == null) return;

        List<File> results = new ArrayList<>();
        searchRecursive(currentDir, query.toLowerCase(), results);

        Collections.sort(results, (a, b) ->
                a.getName().compareToIgnoreCase(b.getName()));

        adapter.setFiles(results);
        tvPath.setText("🔍 \"" + query + "\" — " + results.size() + " sonuç");
    }

    private void searchRecursive(File dir, String query, List<File> results) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.getName().toLowerCase().contains(query)) {
                results.add(f);
            }
            if (f.isDirectory()) {
                searchRecursive(f, query, results);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new_file) {
            createNewFile();
            return true;
        } else if (id == R.id.action_new_folder) {
            createFolder();
            return true;
        } else if (id == R.id.action_ftp_server) {
            startActivity(new Intent(this, FtpServerActivity.class));
            return true;
        } else if (id == R.id.action_ftp) {
            startActivity(new Intent(this, FtpActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            loadDir(Environment.getExternalStorageDirectory());
        }
    }

    void loadDir(File dir) {
        currentDir = dir;
        if (searchQuery.isEmpty()) {
            tvPath.setText(dir.getAbsolutePath());
        }
        boolean showHidden = prefs.getBoolean("show_hidden", false);
        File[] files = dir.listFiles();
        allFiles = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (showHidden || !f.isHidden()) allFiles.add(f);
            }
        }
        Collections.sort(allFiles, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        if (!searchQuery.isEmpty()) {
            searchFiles(searchQuery);
        } else {
            adapter.setFiles(allFiles);
        }
    }

    private void openFile(File f) {
        String ext = f.getName().contains(".") ?
                f.getName().substring(f.getName().lastIndexOf('.') + 1).toLowerCase() : "";

        if (ext.matches("txt|log|md|java|xml|json|html|css|js|py|gradle|kt")) {
            Intent i = new Intent(this, TextEditorActivity.class);
            i.putExtra("file_path", f.getAbsolutePath());
            startActivity(i);
            return;
        }

        if (ext.equals("pdf")) {
            Intent i = new Intent(this, PdfViewerActivity.class);
            i.putExtra("file_path", f.getAbsolutePath());
            startActivity(i);
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", f);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, getMime(ext));
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Bu dosyayı açacak uygulama bulunamadı",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getMime(String ext) {
        switch (ext) {
            case "jpg": case "jpeg": case "png": case "gif": case "webp": return "image/*";
            case "mp4": case "mkv": case "avi": case "mov": return "video/*";
            case "mp3": case "wav": case "flac": case "aac": return "audio/*";
            case "apk": return "application/vnd.android.package-archive";
            case "zip": return "application/zip";
            case "doc": case "docx": return "application/msword";
            case "xls": case "xlsx": return "application/vnd.ms-excel";
            default: return "*/*";
        }
    }

    private void showContextMenu(File f) {
        new AlertDialog.Builder(this)
                .setTitle(f.getName())
                .setItems(new String[]{"Aç", "Sil", "Yeniden Adlandır"}, (d, w) -> {
                    if (w == 0) openFile(f);
                    else if (w == 1) deleteFile(f);
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

    private void createNewFile() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etName = new EditText(this);
        etName.setHint("Dosya adı (örn: notlar.txt)");

        EditText etContent = new EditText(this);
        etContent.setHint("İçerik (isteğe bağlı)");
        etContent.setMinLines(3);

        layout.addView(etName);
        layout.addView(etContent);

        new AlertDialog.Builder(this)
                .setTitle("Yeni Dosya")
                .setView(layout)
                .setPositiveButton("Oluştur", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String content = etContent.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Dosya adı boş olamaz",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File newFile = new File(currentDir, name);
                    try {
                        FileWriter fw = new FileWriter(newFile);
                        fw.write(content);
                        fw.close();
                        loadDir(currentDir);
                        Toast.makeText(this, "Dosya oluşturuldu!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Hata: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
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
