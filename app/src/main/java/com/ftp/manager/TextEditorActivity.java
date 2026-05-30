package com.ftp.manager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TextEditorActivity extends AppCompatActivity {

    private EditText editText;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        editText = findViewById(R.id.edit_text);
        TextView tvFilename = findViewById(R.id.tv_filename);
        Button btnSave = findViewById(R.id.btn_save);

        String path = getIntent().getStringExtra("file_path");
        if (path == null) { finish(); return; }

        file = new File(path);
        tvFilename.setText(file.getName());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            editText.setText(sb.toString());
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.cannot_read),
                    Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(v -> saveFile());
    }

    private void saveFile() {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(editText.getText().toString());
            fw.close();
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.cannot_save), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        saveFile();
        super.onBackPressed();
    }
}
