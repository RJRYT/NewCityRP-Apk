package com.newcityrp.launcher;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.Manifest;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Map;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String[]> permissionLauncher;
    private boolean isMicrophonePermissionGranted = false;
    private boolean isNotificationPermissionGranted = false;
    private boolean permissionDialogShown = false; // Prevent loop
    
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    private AlertManager alertManager;
    private LogManager logManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::onPermissionsResult
        );

        checkAndRequestPermissions();
        Toast.makeText(this, getString(R.string.app_name_long) + " v" + getAppVersion(), Toast.LENGTH_LONG).show();
        
        logManager = new LogManager(this);
        
        logManager.logInfo("Application started");
        writeLogFile();
        
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up ViewPager2 adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        //default page
        viewPager.setCurrentItem(2);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        
        
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            
            if (item.getItemId() == R.id.nav_info) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.nav_favorite) {
                viewPager.setCurrentItem(1);
                return true;
            }  else if (item.getItemId() == R.id.nav_home) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (item.getItemId() == R.id.nav_servers) {
                viewPager.setCurrentItem(3);
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                viewPager.setCurrentItem(4);
                return true;
            } 
            return false;
        });

        // Handle page changes to sync with bottom navigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_info);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_favorite);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_servers);
                        break;
                    case 4:
                        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                        break;
                }
            }
        });
        
    }

    private void writeLogFile() {
    try {
        File logFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "my_log_file.txt");
        FileWriter writer = new FileWriter(logFile);
        writer.append("This is a log entry");
        writer.flush();
        writer.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check permissions only if the dialog was not previously shown
        if (!permissionDialogShown) {
            checkAndRequestPermissions();
        }
    }

    private void checkAndRequestPermissions() {
        isMicrophonePermissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        isNotificationPermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        // Only show dialog if permissions are not already granted
        if (!isMicrophonePermissionGranted || !isNotificationPermissionGranted) {
            showPermissionsDialog();
        }else {
            //test
        }
    }

    private void showPermissionsDialog() {
        alertManager = new AlertManager(this);
        permissionDialogShown = true; // Set flag to avoid repeat dialog

        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("This app needs access to your microphone and notifications to work properly.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    requestPermissions();
                })
                .setNegativeButton("Deny", (dialog, which) -> {
                    alertManager.showAlert("Request cancelled. Exiting App", AlertManager.AlertType.ERROR);
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    private void requestPermissions() {
        permissionLauncher.launch(new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
        });
    }

    private void onPermissionsResult(Map<String, Boolean> permissions) {
        alertManager = new AlertManager(this);
        isMicrophonePermissionGranted = permissions.getOrDefault(Manifest.permission.RECORD_AUDIO, isMicrophonePermissionGranted);
        isNotificationPermissionGranted = permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, isNotificationPermissionGranted);

        if (isMicrophonePermissionGranted && isNotificationPermissionGranted) {
            alertManager.showAlert("All permissions granted!", AlertManager.AlertType.INFO);
            startNotificationService();
        } else {
            handlePermissionDenial();
        }
    }

    private void handlePermissionDenial() {
        alertManager = new AlertManager(this);
        boolean showRationaleMicrophone = shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
        boolean showRationaleNotification = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);

        if (!showRationaleMicrophone || !showRationaleNotification) {
            // Permissions are permanently denied
            showSettingsDialog();
        } else {
            // Permission denied but can ask again
            alertManager.showAlert("Permissions Denied. App may not function correctly.", AlertManager.AlertType.ERROR);
        }
    }

    private void showSettingsDialog() {
        alertManager = new AlertManager(this);
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("You have denied some permissions permanently. Please go to settings to enable them.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    alertManager.showAlert("Permissions Denied. Exiting app.", AlertManager.AlertType.ERROR);
                    finish();
                })
                .create()
                .show();
    }
    
    private String getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName; // Fetch the version name
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown"; // Return a default value in case of an error
        }
    }

    private void startNotificationService() {
        Intent intent = new Intent(this, NotificationService.class);
        startService(intent);
    }
}





