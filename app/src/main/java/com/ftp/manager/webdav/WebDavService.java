
package com.ftp.manager.webdav;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class WebDavService extends Service {

    private static final String CHANNEL_ID = "webdav_channel";
    private static final int NOTIFICATION_ID = 1001;
    private WebDavServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification("WebDAV başlatılıyor..."));
        startServer();
    }

    private void startServer() {
        try {
            File root = Environment.getExternalStorageDirectory();
            server = new WebDavServer(8080, root, "admin", "1234");
            server.start();
            Log.i("WebDavService", "WebDAV Server started on port 8080");
            updateNotification("WebDAV çalışıyor: http://localhost:8080");
        } catch (Exception e) {
            Log.e("WebDavService", "Server start error: " + e.getMessage());
            updateNotification("WebDAV başlatılamadı!");
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            Log.i("WebDavService", "WebDAV Server stopped");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "WebDAV Server",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WebDAV Server")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(text));
        }
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
