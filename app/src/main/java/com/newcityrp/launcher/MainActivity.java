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

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    private AlertManager alertManager;
    private LogManager logManager;
    private PermissionHelper permissionHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionHelper = new PermissionHelper(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                sendGreetingNotification();
            }

            @Override
            public void onPermissionsDenied() {
                finish();
            }
        });
        permissionHelper.checkAndRequestPermissions();

        Toast.makeText(this, getString(R.string.app_name_long) + " v" + getAppVersion(), Toast.LENGTH_LONG).show();
        logManager = new LogManager(this);        
        logManager.logInfo("========Application started========");

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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check permissions only if the dialog was not previously shown
        if (!permissionHelper.permissionDialogShown) {
            permissionHelper.checkAndRequestPermissions();
        }
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

    private void sendGreetingNotification() {
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.notify("Welcome to NewCityRP!", "Proceed to check for app updates and game file verification.");
    }
}