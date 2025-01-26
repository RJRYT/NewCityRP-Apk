package com.newcityrp.launcher;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import com.wardrumstudios.utils.WarMedia;
import java.io.PrintStream;

public class GTASA extends WarMedia {
    public static GTASA gtasaSelf;
    static String vmVersion;
    private boolean once = false;

    /* JADX WARNING: Removed duplicated region for block: B:4:? A:{SYNTHETIC, ExcHandler: java.lang.ExceptionInInitializerError (unused java.lang.ExceptionInInitializerError), Splitter: B:1:0x0007} */
    
    static {
        System.out.println("**** Loading SO's");
        try {
    System.loadLibrary("ImmEmulatorJ");
    System.out.println("Loaded ImmEmulatorJ");
    System.loadLibrary("GTASA");
    System.out.println("Loaded GTASA");
    System.loadLibrary("AML");
    System.out.println("Loaded AML");
} catch (ExceptionInInitializerError | UnsatisfiedLinkError e) {
    e.printStackTrace();
    System.err.println("Error loading native libraries: " + e.getMessage());
}
    }

    private void initialiseClientInfo() {
    }

    public static void staticEnterSocialClub() {
        gtasaSelf.EnterSocialClub();
    }

    public static void staticExitSocialClub() {
        gtasaSelf.ExitSocialClub();
    }

    public void AfterDownloadFunction() {
        System.out.println("**** AfterDownloadFunction");
    }

    public boolean CustomLoadFunction() {
        return CheckIfNeedsReadPermission(gtasaSelf);
    }

    public void EnterSocialClub() {
        System.out.println("**** EnterSocialClub");
    }

    public void ExitSocialClub() {
        System.out.println("**** ExitSocialClub");
    }

    public boolean ServiceAppCommand(String str, String str2) {
        return false;
    }

    public int ServiceAppCommandValue(String str, String str2) {
        return 0;
    }

    public native void main();

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void onCreate(Bundle bundle) {
        if (getIntent().getStringExtra("begi_otsyda").equals("fdfef8itfh94t6ywefgiewfwrdi")) {
            if (!this.once) {
                initialiseClientInfo();
                this.once = true;
            }
            System.out.println("GTASA onCreate");
            gtasaSelf = this;
            this.wantsMultitouch = true;
            this.wantsAccelerometer = true;
            super.onCreate(bundle);
            return;
         }
        finish();
    }

    public void onDestroy() {
        System.out.println("GTASA onDestroy");
        super.onDestroy();
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return super.onKeyDown(i, keyEvent);
    }

    public void onPause() {
        System.out.println("GTASA onPause");
        super.onPause();
    }

    public void onRestart() {
        System.out.println("GTASA onRestart");
        super.onRestart();
    }

    public void onResume() {
        System.out.println("GTASA onResume");
        super.onResume();
    }

    public void onStart() {
        System.out.println("GTASA onStart");
        super.onStart();
    }

    public void onStop() {
        System.out.println("GTASA onStop");
        super.onStop();
    }

    public native void setCurrentScreenSize(int i, int i2);
}