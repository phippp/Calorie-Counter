package com.example.caloriecounter.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service {

    NotificationCreator notification = new NotificationCreator();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        notification.createStickyNotification(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notification.createStickyNotification(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}