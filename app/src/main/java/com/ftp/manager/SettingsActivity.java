package com.ftp.manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ayarlar");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences("FTPManagerPrefs", MODE_PRIVATE);

        // Dark Mode switch
        Switch switchDark = findViewById(R.id.switch_dark_mode);
        switchDark.setChecked(prefs.getBoolean("dark_mode", false));
        switchDark.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Gizli dosyalar switch
        Switch switchHidden = findViewById(R.id.switch_hidden_files);
        switchHidden.setChecked(prefs.getBoolean("show_hidden", false));
        switchHidden.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("show_hidden", isChecked).apply();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
