package com.newcityrp.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Config;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


public class SplashScreenActivity extends AppCompatActivity {
    
    private TextView versionText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        versionText = findViewById(R.id.version_text);
        versionText.setText("v"+getAppVersion());
        
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3000); // 3-second delay
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
}




