package com.example.ftpmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var files: List<File> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.fileRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 📌 Android 11+ için tam dosya erişim kontrolü
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                Toast.makeText(this, "Lütfen dosya erişim izni verin", Toast.LENGTH_LONG).show()
            } else {
                listFiles()
            }
        } else {
            checkPermissionAndList()
        }
    }

    private fun checkPermissionAndList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            listFiles()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            listFiles()
        } else {
            Toast.makeText(this, "Dosya erişim izni reddedildi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listFiles() {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileArray = directory.listFiles()?.filter { it.exists() } ?: emptyList()

        if (fileArray.isEmpty()) {
            Toast.makeText(this, "Dosya bulunamadı veya erişim izni yok", Toast.LENGTH_LONG).show()
            return
        }

        files = fileArray
        val adapter = FileAdapter(files,
            onClick = { file -> openFile(file) },
            onLongClick = { file ->
                if (file.delete()) {
                    Toast.makeText(this, "Silindi: ${file.name}", Toast.LENGTH_SHORT).show()
                    listFiles()
                } else {
                    Toast.makeText(this, "Silinemedi: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            })
        recyclerView.adapter = adapter
    }

    private fun openFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.fromFile(file), "*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Açılamadı: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }
}
