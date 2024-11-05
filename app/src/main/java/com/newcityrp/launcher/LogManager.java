package com.newcityrp.launcher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

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
        createLogFile();
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

    // Write log message to file
    private void writeLog(String message) {
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

    // Log error message with a single String
    public void logError(String error) {
        writeLog("ERROR: " + error);
    }

    // Log error message with multiple Strings
    public void logError(String... errors) {
        StringBuilder message = new StringBuilder("ERROR: ");
        for (String error : errors) {
            message.append(error).append(" ");
        }
        writeLog(message.toString().trim());
    }

    // Log error message with multiple parameters (Objects)
    public void logError(Object... errors) {
        StringBuilder message = new StringBuilder("ERROR: ");
        for (Object error : errors) {
            message.append(error).append(" ");
        }
        writeLog(message.toString().trim());
    }

    // Log general message with a single String
    public void logInfo(String info) {
        writeLog("INFO: " + info);
    }

    // Log general message with multiple Strings
    public void logInfo(String... infos) {
        StringBuilder message = new StringBuilder("INFO: ");
        for (String info : infos) {
            message.append(info).append(" ");
        }
        writeLog(message.toString().trim());
    }

    // Log general message with multiple parameters (Objects)
    public void logInfo(Object... infos) {
        StringBuilder message = new StringBuilder("INFO: ");
        for (Object info : infos) {
            message.append(info).append(" ");
        }
        writeLog(message.toString().trim());
    }

    // Log assert message
    public void logAssert(String message) {
        writeLog("ASSERT: " + message);
    }

    public void logAssert(String... messages) {
        StringBuilder message = new StringBuilder("ASSERT: ");
        for (String msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }

    public void logAssert(Object... messages) {
        StringBuilder message = new StringBuilder("ASSERT: ");
        for (Object msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }

    // Log warn message
    public void logWarn(String message) {
        writeLog("WARN: "+ message);
    }

    public void logWarn(String... messages) {
        StringBuilder message = new StringBuilder("WARN: ");
        for (String msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }

    public void logWarn(Object... messages) {
        StringBuilder message = new StringBuilder("WARN: ");
        for (Object msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }

    // Log verbose message
    public void logVerbose(String message) {
        writeLog("VERBOSE: "+ message);
    }

    public void logVerbose(String... messages) {
        StringBuilder message = new StringBuilder("VERBOSE: ");
        for (String msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }

    public void logVerbose(Object... messages) {
        StringBuilder message = new StringBuilder("VERBOSE: ");
        for (Object msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }

    // Log debug message
    public void logDebug(String message) {
        writeLog("DEBUG: "+ message);
    }

    public void logDebug(String... messages) {
        StringBuilder message = new StringBuilder("DEBUG: ");
        for (String msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }

    public void logDebug(Object... messages) {
        StringBuilder message = new StringBuilder("DEBUG: ");
        for (Object msg : messages) {
            message.append(msg).append(" ");
        }
        writeLog(message.toString().trim());
    }
}