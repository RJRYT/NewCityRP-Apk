package com.newcityrp.launcher;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

public class NotificationHelper {

    private static final String CHANNEL_ID = "greeting_channel";
    private static final String TAG = "NotificationHelper";
    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Greetings",
                        NotificationManager.IMPORTANCE_HIGH
                );
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to create notification channel", e);
            }
        }
    }

    public void notify(String title, String description) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logoround)  // Ensure you have a drawable resource for the icon
                    .setContentTitle(title)
                    .setContentText(description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (notificationManager != null) {
                notificationManager.notify(1, builder.build());
            } else {
                Log.e(TAG, "Notification Manager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to send notification", e);
        }
    }
}
