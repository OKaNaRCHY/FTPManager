package com.example.ftpmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var files: List<Pair<String, Uri>> = emptyList() // 📌 Dosya adı + URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.fileRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                Toast.makeText(this, "Lütfen dosya erişim izni verin", Toast.LENGTH_LONG).show()
            } else {
                listAllMedia()
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
            listAllMedia()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            listAllMedia()
        } else {
            Toast.makeText(this, "Dosya erişim izni reddedildi", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Tüm klasörleri MediaStore ile listeleme
    private fun listAllMedia() {
        val allFiles = mutableListOf<Pair<String, Uri>>()

        queryMediaStore(MediaStore.Downloads.EXTERNAL_CONTENT_URI, allFiles)
        queryMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, allFiles)
        queryMediaStore(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, allFiles)
        queryMediaStore(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, allFiles)

        if (allFiles.isEmpty()) {
            Toast.makeText(this, "Hiç dosya bulunamadı", Toast.LENGTH_LONG).show()
            return
        }

        files = allFiles
        val adapter = UriAdapter(files, // 📌 Yeni adapter: isim + URI
            onClick = { name, uri -> openFile(uri) },
            onLongClick = { name, uri ->
                Toast.makeText(this, "Silme MediaStore URI ile desteklenmiyor", Toast.LENGTH_SHORT).show()
            })
        recyclerView.adapter = adapter
    }

    // 📌 Artık URI kullanıyoruz
    private fun queryMediaStore(uri: Uri, list: MutableList<Pair<String, Uri>>) {
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns._ID
        )
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (it.moveToNext()) {
                val name = it.getString(nameColumn)
                val id = it.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(uri, id.toString())
                list.add(Pair(name, contentUri))
            }
        }
    }

    private fun openFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Açılamadı: $uri", Toast.LENGTH_SHORT).show()
        }
    }
}
