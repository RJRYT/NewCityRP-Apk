package com.newcityrp.launcher;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.Manifest;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;
import android.content.Context;
import androidx.annotation.NonNull;
import android.content.SharedPreferences;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;
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

        permissionHelper = new PermissionHelper(this, permissionLauncher, new PermissionHelper.PermissionCallback() {
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

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2); // Optional: for OpenGL ES 2.0
        glSurfaceView.setRenderer(new AppRenderer()); // Set the custom renderer
        setContentView(glSurfaceView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, GameFileUpdateActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }).start();
    }
    
    private void onPermissionsResult(Map<String, Boolean> permissions) {
        permissionHelper.handlePermissionsResult(permissions);
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

    private class AppRenderer implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Called when the surface is created for the first time
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String gpu = gl.glGetString(GL10.GL_RENDERER);  // Get GPU info
            
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

        @Override
        public void onSurfaceDestroyed(GL10 gl) {
            // Called when the surface is destroyed (e.g., when activity is paused)
        }
    }
}