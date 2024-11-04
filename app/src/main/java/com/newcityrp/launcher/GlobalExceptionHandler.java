package com.newcityrp.launcher;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "GlobalExceptionHandler";
    private static final String ERROR_COUNT_KEY = "error_count";
    private static final int MAX_ERROR_COUNT = 3;

    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final SharedPreferences sharedPreferences;
    private LogManager logManager;

    public GlobalExceptionHandler(Context context) {
        this.context = context;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // Main logic of your custom error handler
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.recordException(throwable);

            logManager = new LogManager(this.context);
            logManager.logError("========uncaughtException========");
            logManager.logError(throwable.toString());

            Log.e(TAG, "Unhandled exception in thread " + thread.getName(), throwable);

            int errorCount = sharedPreferences.getInt(ERROR_COUNT_KEY, 0) + 1;
            sharedPreferences.edit().putInt(ERROR_COUNT_KEY, errorCount).apply();

            Toast.makeText(this.context, "An unexpected error occurred. Restarting the app...", Toast.LENGTH_SHORT)
                    .show();

            if (errorCount > MAX_ERROR_COUNT) {
                sharedPreferences.edit().putInt(ERROR_COUNT_KEY, 0).apply();
                Process.killProcess(Process.myPid());
                System.exit(1);
                return;
            }
        } catch (Exception e) {
            // Log or handle any error occurring within the handler itself
            Log.e(TAG, "Error in GlobalExceptionHandler: " + e.getMessage(), e);
        }
    }

}
