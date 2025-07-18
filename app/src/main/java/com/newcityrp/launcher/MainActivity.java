package com.newcityrp.launcher;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.Manifest;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import android.content.Context;
import androidx.annotation.NonNull;
import android.content.SharedPreferences;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
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
    private UtilManager utilManager;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_NOTIFICATION_SHOWN = "notification_shown";
    private static final String GAME_FILES_DIR = "files";
    private static final String KEY_GPU_INFO = "gpu_info";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::onPermissionsResult
        );

        logManager = new LogManager(this);
        utilManager = new UtilManager(this);
        permissionHelper = new PermissionHelper(this, permissionLauncher, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                sendGreetingNotification();
                utilManager.launchMainActivityFreshly(MainActivity.this);
            }

            @Override
            public void onPermissionsDenied() {
                finish();
            }
        });

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

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2); // Optional: for OpenGL ES 2.0
        glSurfaceView.setRenderer(new AppRenderer()); // Set the custom renderer
        //setContentView(glSurfaceView);
        

        if(permissionHelper.arePermissionsGranted()) {
            if (!isNetworkAvailable()) {
                showNetworkDialog();
            } 
            else if(!utilManager.isGameFilesDownloaded(this)) {
                Intent intent = new Intent(MainActivity.this, GameDataSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            permissionHelper.checkAndRequestPermissions();
        }

        logManager.logInfo("========Application started========");
        Toast.makeText(this, getString(R.string.app_name_long) + " v" + getAppVersion(), Toast.LENGTH_LONG).show();
    }

    public void finishActivity() {
        finish();
    }

    private void onPermissionsResult(Map<String, Boolean> permissions) {
        permissionHelper.handlePermissionsResult(permissions);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if(permissionHelper.allFilesPermsReqTriggered) {
            if(permissionHelper.arePermissionsGranted()) {
            	permissionHelper.restartApp(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationShown = prefs.getBoolean(KEY_NOTIFICATION_SHOWN, false);

        if (!notificationShown) {
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.notify("Welcome to NewCityRP!", "Proceed to check for app updates and game file verification.");

            // Update shared preferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_NOTIFICATION_SHOWN, true);
            editor.apply();
        }
    }

    // Show a dialog when there is no network connection
    private void showNetworkDialog() {
        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setMessage("Network is unavailable. Do you want to restart the app?")
            .setCancelable(false)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Restart the activity if Yes is clicked
                    restartActivity();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Close the app if No is clicked
                    finish(); // This will close the app
                }
            })
            .show();
    }

    // Method to check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Method to restart the activity
    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private class AppRenderer implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Called when the surface is created for the first time
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String gpuInfo = gl.glGetString(GL10.GL_RENDERER);  // Get GPU info
            
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_GPU_INFO, gpuInfo);
            editor.apply();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            // Called every frame to render the content
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // Called when the surface size changes (e.g., on screen rotation)
        }
    }
}