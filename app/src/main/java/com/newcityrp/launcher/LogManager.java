package com.newcityrp.launcher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private static final String TAG = "LogManager";
    private static final String LOG_FOLDER_NAME = "NewCityRP/logs";
    private static final String LOG_FILE_NAME = "app_log.txt";
    private File logFile;
    private Context context;

    public LogManager(Context context) {
        this.context = context;
    }

    public void checkPermissionsAndCreateLogFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                createLogFile(); // Permission granted
            } else {
                requestAllFilesAccessPermission((Activity) context);
            }
        } else {
            requestLegacyPermission((Activity) context);
        }
    }

    // Create log file
    private void createLogFile() {
        if (isExternalStorageWritable()) {
            File logDirectory = new File(Environment.getExternalStorageDirectory(), LOG_FOLDER_NAME);
            if (!logDirectory.exists()) {
                logDirectory.mkdirs(); // Create directory if it does not exist
            }
            logFile = new File(logDirectory, LOG_FILE_NAME);
            try {
                if (!logFile.exists()) {
                    logFile.createNewFile(); // Create log file if it does not exist
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating log file: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "External storage is not writable. Check permissions.");
        }
    }

    // Check if external storage is writable
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Request all files access permission
    private void requestAllFilesAccessPermission(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    // Request legacy permission for Android 10 and below
    private void requestLegacyPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    // Write log message to file
    public void writeLog(String message) {
        if (logFile != null) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logMessage = timeStamp + " - " + message + "\n";
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.append(logMessage);
                writer.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error writing log: " + e.getMessage());
            }
        }
    }

    // Log error message
    public void logError(String error) {
        writeLog("ERROR: " + error);
    }

    // Log general message
    public void logInfo(String info) {
        writeLog("INFO: " + info);
    }
}