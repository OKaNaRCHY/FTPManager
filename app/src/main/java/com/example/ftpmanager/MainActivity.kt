package com.example.ftpmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.fileListView)

        // 📌 İzin kontrolü
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            listFiles()
        }
    }

    private fun listFiles() {
        val path = Environment.getExternalStorageDirectory().path
        val directory = File(path)
        val files = directory.listFiles()

        val fileNames = files?.map { it.name } ?: listOf("Dosya bulunamadı")

        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, fileNames)
        listView.adapter = adapter
    }
}
