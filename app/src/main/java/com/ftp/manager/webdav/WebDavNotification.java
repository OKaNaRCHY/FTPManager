package com.ftp.manager.webdav;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.ftp.manager.R;

public class WebDavNotification {

    public static Notification build(Context ctx, String text) {
        // STOP_WEBDAV action ile servisi durdur
        Intent stopIntent = new Intent(ctx, WebDavService.class);
        stopIntent.setAction("STOP_WEBDAV");
        PendingIntent stopPending = PendingIntent.getService(
                ctx, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(ctx, "webdav_channel")
                .setContentTitle("WebDAV Server")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .addAction(R.drawable.ic_stop, "Durdur", stopPending)
                .setOngoing(true)
                .build();
    }
}
