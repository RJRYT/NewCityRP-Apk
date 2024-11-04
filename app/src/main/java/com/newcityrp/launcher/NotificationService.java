package com.newcityrp.launcher;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.widget.RemoteViews;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "greeting_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        sendGreetingNotification();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Greetings",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void sendGreetingNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        notificationLayout.setTextViewText(R.id.notification_title, "Welcome to NewCityRP Launcher");
        notificationLayout.setTextViewText(R.id.notification_text, "Proceed to check for app updates and game file verification.");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logoround)
                .setContentTitle("Welcome to NewCityRP Launcher")
                .setContentText("Proceed to check for app updates and game file verification.")
                .setCustomContentView(notificationLayout)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding necessary for this service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendGreetingNotification(); // Send notification when the service starts
        return START_NOT_STICKY;
    }
}