package com.newcityrp.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.widget.Toast;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public GlobalExceptionHandler(Context context) {
        this.context = context;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Log the exception with Firebase Crashlytics
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.recordException(throwable);  // This logs the exception to Firebase

        // Optional: Add custom keys or user information
        crashlytics.setCustomKey("UnhandledExceptionThread", thread.getName());

        // Optional: Show a Toast message
        Toast.makeText(context, "An unexpected error occurred. Restarting the app...", Toast.LENGTH_SHORT).show();

        // Restart the app (or navigate to a specific activity)
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Kill the process to end the current instance of the app
        Process.killProcess(Process.myPid());
        System.exit(1);

        // Call the default handler as well if needed (optional)
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        }
    }
}