package com.ftp.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.List;

public class FtpServerService extends Service {

    private static final String TAG = "FtpServerService";
    private static final String CHANNEL_ID = "ftp_channel";
    private static final int NOTIF_ID = 1;

    private FtpServer server;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public FtpServerService getService() { return FtpServerService.this; }
    }

    @Override
    public IBinder onBind(Intent intent) { return binder; }

    public void startFtp(int port, String username, String password, String rootDir) {
        try {
            FtpServerFactory factory = new FtpServerFactory();
            ListenerFactory lf = new ListenerFactory();
            lf.setPort(port);
            factory.addListener("default", lf.createListener());

            PropertiesUserManagerFactory umf = new PropertiesUserManagerFactory();
            org.apache.ftpserver.usermanager.UserManager um = umf.createUserManager();

            BaseUser user = new BaseUser();
            user.setName(username);
            user.setPassword(password);
            user.setHomeDirectory(rootDir);
            user.setEnabled(true);

            List<Authority> auths = new ArrayList<>();
            auths.add(new WritePermission());
            user.setAuthorities(auths);
            um.save(user);
            factory.setUserManager(um);

            server = factory.createServer();
            server.start();
            Log.d(TAG, "FTP started on port " + port);

            startForeground(NOTIF_ID, buildNotification(port));
        } catch (FtpException e) {
            Log.e(TAG, "FTP start error", e);
        }
    }

    public void stopFtp() {
        if (server != null && !server.isStopped()) {
            server.stop();
            server = null;
        }
        stopForeground(true);
        stopSelf();
    }

    public boolean isRunning() {
        return server != null && !server.isStopped();
    }

    private Notification buildNotification(int port) {
        createChannel();
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FTP Sunucusu Çalışıyor")
                .setContentText("Port: " + port)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setOngoing(true)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "FTP Sunucusu", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @Override
    public void onDestroy() {
        stopFtp();
        super.onDestroy();
    }
}
