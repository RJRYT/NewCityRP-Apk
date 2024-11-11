package com.newcityrp.launcher;

import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.io.File;

class UtilManager {

    private final Context context;
    private HttpClient httpClient;
    private LogManager loger;
    private DownloadHelper downloadHelper;
    private AlertManager alertManager;

    private static final String KEY_NOTIFICATION_SHOWN = "notification_shown";
    private static final String PREFS_NAME = "AppPrefs";

    public UtilManager(Context context) {
        this.context = context;
        this.httpClient = new HttpClient(context);
        this.loger = new LogManager(context);
        this.downloadHelper = new DownloadHelper(context);
        this.alertManager = new AlertManager((Activity) context);
    }

    public void launchMainActivityFreshly(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public String getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName; // Fetch the version name
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown"; // Return a default value in case of an error
        }
    }

    public void sendGreetingNotification() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationShown = prefs.getBoolean(KEY_NOTIFICATION_SHOWN, false);

        if (!notificationShown) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.notify("Welcome to NewCityRP!", "Proceed to check for app updates and game file verification.");

            // Update shared preferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_NOTIFICATION_SHOWN, true);
            editor.apply();
        }
    }

    private boolean isGameFilesDownloaded(Context context) {
        File gameFilesDir = new File(context.getExternalFilesDir(null), "");
        return gameFilesDir.exists() && gameFilesDir.isDirectory() && gameFilesDir.listFiles().length > 0;
    }
}