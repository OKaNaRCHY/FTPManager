package com.ftp.manager.webdav;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ftp.manager.R;

public class WebDavSettingsActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webdav_settings);

        usernameInput = findViewById(R.id.webdav_username);
        passwordInput = findViewById(R.id.webdav_password);
        saveButton = findViewById(R.id.webdav_save);

        SharedPreferences prefs = getSharedPreferences("webdav_prefs", MODE_PRIVATE);

        // Önceden kaydedilmiş değerleri yükle
        usernameInput.setText(prefs.getString("username", "admin"));
        passwordInput.setText(prefs.getString("password", "1234"));

        saveButton.setOnClickListener(v -> {
            String user = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();

            prefs.edit()
                    .putString("username", user)
                    .putString("password", pass)
                    .apply();

            finish(); // Ayarlar kaydedildikten sonra activity kapanır
        });
    }
}
