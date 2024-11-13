package com.newcityrp.launcher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import java.util.Map;
import android.util.Log;

public class PermissionHelper {

    private final Context context;
    private final ActivityResultLauncher<String[]> permissionLauncher;
    private final AlertManager alertManager;
    private final PermissionCallback callback;

    private boolean isMicrophonePermissionGranted = false;
    private boolean isNotificationPermissionGranted = false;
    public boolean permissionDialogShown = false;
    public boolean allFilesPermsReqTriggered = false;

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    public PermissionHelper(Context context, ActivityResultLauncher<String[]> permissionLauncher, PermissionCallback callback) {
        this.context = context;
        this.permissionLauncher = permissionLauncher;
        this.alertManager = new AlertManager((Activity) context);
        this.callback = callback;
    }

    public void checkPermissionsForCreateFiles() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestAllFilesAccessPermission((Activity) context);
            }
        } else {
            requestLegacyPermission((Activity) context);
        }
    }

    // Request all files access permission
    private void requestAllFilesAccessPermission(Activity activity) {
        try {
            if (activity != null) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                allFilesPermsReqTriggered = true;
                activity.startActivity(intent);
            } else {
                Log.e("PermissionError", "Activity reference is null");
                Toast.makeText(activity, "Unable to access permission settings. Restarting the app...", Toast.LENGTH_SHORT).show();
                restartApp(activity);
            }
        } catch (Exception e) {
            Log.e("PermissionError", "Failed to request all files access permission", e);
            Toast.makeText(activity, "An error occurred. Restarting the app...", Toast.LENGTH_SHORT).show();
            restartApp(activity);
        }
    }

    public void restartApp(Activity activity) {
        Toast.makeText(activity, "Restarting",1).show();
    if (activity != null) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            activity.finishAffinity(); // Closes all activities
        }
    }
}

    // Request legacy permission for Android 10 and below
    private void requestLegacyPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    public void checkAndRequestPermissions() {
        isMicrophonePermissionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        isNotificationPermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        if (!isMicrophonePermissionGranted || !isNotificationPermissionGranted) {
            if (!permissionDialogShown) {
                showPermissionsDialog();
            }
        } else if(!areFileAccessGranted()){
            checkPermissionsForCreateFiles();
            
        }else{
            callback.onPermissionsGranted();
        }
    }

    private void showPermissionsDialog() {
        permissionDialogShown = true;

        new AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setTitle("Permissions Required")
            .setMessage("This app needs access to your microphone, notifications, and files to work properly.")
            .setCancelable(false)
            .setOnDismissListener(dialog -> {
                permissionDialogShown = false;
            })
            .setPositiveButton("Grant", (dialog, which) -> requestPermissions())
            .setNegativeButton("Deny", (dialog, which) -> {
                alertManager.showAlert("Request cancelled. Exiting App", AlertManager.AlertType.ERROR);
                callback.onPermissionsDenied();
            })
            .show();
    }

    private void requestPermissions() {
        permissionLauncher.launch(new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        });
    }

    public void handlePermissionsResult(Map<String, Boolean> permissions) {
        isMicrophonePermissionGranted = permissions.getOrDefault(Manifest.permission.RECORD_AUDIO, false);
        isNotificationPermissionGranted = permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false);

        if (isMicrophonePermissionGranted && isNotificationPermissionGranted) {
            alertManager.showAlert("All permissions granted!", AlertManager.AlertType.INFO);
            checkPermissionsForCreateFiles();
            callback.onPermissionsGranted();
        } else {
            handlePermissionDenial();
        }
    }

    private void handlePermissionDenial() {
        boolean showRationaleMicrophone = ((Activity) context).shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
        boolean showRationaleNotification = ((Activity) context).shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);

        if (!showRationaleMicrophone || !showRationaleNotification) {
            showSettingsDialog();
        } else {
            alertManager.showAlert("Permissions Denied. App may not function correctly.", AlertManager.AlertType.ERROR);
            callback.onPermissionsDenied();
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setTitle("Permissions Required")
            .setMessage("You have denied some permissions permanently. Please go to settings to enable them.")
            .setCancelable(false)
            .setPositiveButton("Go to Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                alertManager.showAlert("Permissions Denied. Exiting app.", AlertManager.AlertType.ERROR);
                callback.onPermissionsDenied();
            })
            .create()
            .show();
    }
    
    public boolean arePermissionsGranted() {
        // Check for voice recording permission
        boolean voiceGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        // Check for notification permission (required on Android 13+)
        boolean notificationGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled();
        }

        // Check for all files access (special permission on Android 11+)
        boolean fileAccessGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fileAccessGranted = Environment.isExternalStorageManager();
        } else {
            fileAccessGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        return voiceGranted && notificationGranted && fileAccessGranted;
    }
    
    public boolean areVoiceAndNotifyGranted() {
        // Check for voice recording permission
        boolean voiceGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        // Check for notification permission (required on Android 13+)
        boolean notificationGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled();
        }

        return voiceGranted && notificationGranted;
    }
    
    public boolean areFileAccessGranted() {
       
        // Check for all files access (special permission on Android 11+)
        boolean fileAccessGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fileAccessGranted = Environment.isExternalStorageManager();
        } else {
            fileAccessGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        return fileAccessGranted;
    }
}






