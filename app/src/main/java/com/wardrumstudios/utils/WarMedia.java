package com.wardrumstudios.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import com.nvidia.devtech.NvUtil;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class WarMedia extends WarGamepad {
    protected String apkFileName;
    private String baseDirectory;
    private String baseDirectoryRoot;
    protected String expansionFileName;
    protected String patchFileName;

    class a implements FileFilter {
        a(WarMedia warMedia) {
        }

        public boolean accept(File file) {
            return Pattern.matches("cpu[0-9]", file.getName());
        }
    }

    public WarMedia() {
        String str = "";
        this.patchFileName = str;
        this.expansionFileName = str;
        this.apkFileName = str;
    }

    private int getNumberOfProcessors() {
        try {
            return new File("/sys/devices/system/cpu/").listFiles(new a(this)).length;
        } catch (Exception unused) {
            return 1;
        }
    }

    public boolean CheckIfNeedsReadPermission(Activity activity) {
        String str = "android.permission.READ_EXTERNAL_STORAGE";
        int a = ContextCompat.checkSelfPermission(activity, str);
        String str2 = "android.permission.RECORD_AUDIO";
        int a2 = ContextCompat.checkSelfPermission(activity, str2);
        if (a == 0 && a2 == 0) {
            return false;
        }
        this.waitForPermissions = true;
        ArrayList arrayList = new ArrayList();
        if (a == -1) {
            arrayList.add(str);
        }
        if (a2 == -1) {
            arrayList.add(str2);
        }
        ActivityCompat.requestPermissions(activity, (String[]) arrayList.toArray(new String[0]), 8001);
        return true;
    }

    public boolean ConvertToBitmap(byte[] bArr, int i) {
        System.out.println("**** ConvertToBitmap");
        return false;
    }

    public void CreateTextBox(int i, int i2, int i3, int i4, int i5) {
        System.out.println("**** CreateTextBox");
    }

    public boolean CustomLoadFunction() {
        return false;
    }

    public boolean DeleteFile(String str) {
        System.out.println("**** DeleteFile");
        return true;
    }

    public String FileGetArchiveName(int i) {
        System.out.println("**** FileGetArchiveName");
        return "";
    }

    public boolean FileRename(String str, String str2, int i) {
        System.out.println("**** FileRename");
        return true;
    }

    public String GetAndroidBuildinfo(int i) {
        System.out.println("**** GetAndroidBuildinfo");
        return i != 0 ? i != 1 ? i != 2 ? i != 3 ? "UNKNOWN" : Build.HARDWARE : Build.MODEL : Build.PRODUCT : Build.MANUFACTURER;
    }

    public String GetAppId() {
        System.out.println("**** GetAppId");
        return "";
    }

    public int GetAvailableMemory() {
        System.out.println("**** GetAvailableMemory");
        return 0;
    }

    public int GetDeviceInfo(int i) {
        System.out.println("**** GetDeviceInfo");
        if (i == 0) {
            return getNumberOfProcessors();
        }
        if (i != 1) {
            return -1;
        }
        System.out.println("Return for touchsreen 1");
        return 1;
    }

    public int GetDeviceLocale() {
        System.out.println("**** GetDeviceLocale");
        return 0;
    }

    public int GetDeviceType() // TODO: implement this
    {

        int i = 0;
        System.out.println("Build info version device  " + Build.DEVICE);
        System.out.println("Build MANUFACTURER  " + Build.MANUFACTURER);
        System.out.println("Build BOARD  " + Build.BOARD);
        System.out.println("Build DISPLAY  " + Build.DISPLAY);
        System.out.println("Build CPU_ABI  " + Build.CPU_ABI);
        System.out.println("Build CPU_ABI2  " + Build.CPU_ABI2);
        System.out.println("Build HARDWARE  " + Build.HARDWARE);
        System.out.println("Build MODEL  " + Build.MODEL);
        System.out.println("Build PRODUCT  " + Build.PRODUCT);
        int i2 = 0;
        int numberOfProcessors = 1 * 4;
        int i3 = 8 * 64;
        if (IsPhone())
        {
            i = 1;
        }
        return i + i2 + numberOfProcessors + i3;
    }

    public String GetGameBaseDirectory() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            try {
                File externalFilesDir = getExternalFilesDir(null);
                String absolutePath = externalFilesDir.getAbsolutePath();
                this.baseDirectoryRoot = absolutePath.substring(0, absolutePath.indexOf("/Android"));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(externalFilesDir.getAbsolutePath());
                stringBuilder.append("/");
                return stringBuilder.toString();
            } catch (Exception unused) {
            }
        }
        return "";
    }

    public int GetLowThreshhold() {
        System.out.println("**** GetLowThreshhold");
        return 0;
    }

    public String GetPackageName(String str) {
        List installedApplications = getPackageManager().getInstalledApplications(8192);
        for (int i = 0; i < installedApplications.size(); i++) {
            if (str.compareToIgnoreCase(((ApplicationInfo) installedApplications.get(i)).packageName.toString()) == 0) {
                return ((ApplicationInfo) installedApplications.get(i)).sourceDir;
            }
        }
        return "";
    }

    public float GetScreenWidthInches() {
        System.out.println("**** GetScreenWidthInches");
        return 0.0f;
    }

    public int GetSpecialBuildType() {
        System.out.println("**** GetSpecialBuildType");
        return 0;
    }

    public int GetTotalMemory() {
        System.out.println("**** GetTotalMemory");
        return 0;
    }

    public boolean IsAppInstalled(String str) {
        System.out.println("**** IsAppInstalled");
        return false;
    }

    public boolean IsCloudAvailable() {
        System.out.println("**** IsCloudAvailable");
        return false;
    }

    public boolean IsKeyboardShown() {
        System.out.println("**** IsKeyboardShown");
        return false;
    }

    public int IsMoviePlaying() {
        System.out.println("**** IsMoviePlaying");
        return 0;
    }

    public boolean IsPhone() {
        System.out.println("**** IsPhone");
        return true;
    }

    public void LoadAllGamesFromCloud() {
        System.out.println("**** LoadAllGamesFromCloud");
    }

    public String LoadGameFromCloud(int i, byte[] bArr) {
        System.out.println("**** LoadGameFromCloud");
        return "";
    }

    public void MovieClearText(boolean z) {
        System.out.println("**** MovieClearText");
    }

    public void MovieDisplayText(boolean z) {
        System.out.println("**** MovieDisplayText");
    }

    public void MovieKeepAspectRatio(boolean z) {
        System.out.println("**** MovieKeepAspectRatio");
    }

    public void MovieSetSkippable(boolean z) {
        System.out.println("**** MovieSetSkippable");
    }

    public void MovieSetText(String str, boolean z, boolean z2) {
        System.out.println("**** MovieSetText");
    }

    public void MovieSetTextScale(int i) {
        System.out.println("**** MovieSetTextScale");
    }

    public boolean NewCloudSaveAvailable(int i) {
        System.out.println("**** NewCloudSaveAvailable");
        return false;
    }

    public String OBFU_GetDeviceID() {
        System.out.println("**** OBFU_GetDeviceID");
        return "no id";
    }

    public void OpenLink(String str) {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
        System.out.println("**** OpenLink");
    }

    public void PlayMovie(String str, float f) {
        System.out.println("**** PlayMovie");
    }

    public void PlayMovieInFile(String str, float f, int i, int i2) {
        System.out.println("**** PlayMovieInFile");
    }

    public void PlayMovieInWindow(String str, int i, int i2, int i3, int i4, float f, int i5, int i6, int i7) {
        System.out.println("**** PlayMovieInWindow");
    }

    public void PlayMovieInWindow(String str, int i, int i2, int i3, int i4, float f, int i5, int i6, int i7, boolean z) {
        System.out.println("**** PlayMovieInWindow");
    }

    public void SaveGameToCloud(int i, byte[] bArr, int i2) {
        System.out.println("**** SaveGameToCloud");
    }

    public void ScreenSetWakeLock(boolean z) {
        System.out.println("**** ScreenSetWakeLock");
    }

    public void SendStatEvent(String str, String str2, String str3, boolean z) {
        System.out.println("**** SendStatEvent1");
    }

    public void SendStatEvent(String str, boolean z) {
        System.out.println("**** SendStatEvent");
    }

    public void SendTimedStatEventEnd(String str) {
        System.out.println("**** SendTimedStatEventEnd");
    }

    public boolean ServiceAppCommand(String str, String str2) {
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**** ServiceAppCommand ");
        stringBuilder.append(str);
        stringBuilder.append(" ");
        stringBuilder.append(str2);
        printStream.println(stringBuilder.toString());
        return false;
    }

    public boolean ServiceAppCommandInt(String str, int i) {
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**** ServiceAppCommandInt ");
        stringBuilder.append(str);
        stringBuilder.append(" ");
        stringBuilder.append(i);
        printStream.println(stringBuilder.toString());
        return false;
    }

    public int ServiceAppCommandValue(String str, String str2) {
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**** ServiceAppCommandValue ");
        stringBuilder.append(str);
        stringBuilder.append(" ");
        stringBuilder.append(str2);
        printStream.println(stringBuilder.toString());
        return 0;
    }

    public void ShowKeyboard(int i) {
        System.out.println("**** ShowKeyboard");
    }

    public void StopMovie() {
        System.out.println("**** StopMovie");
    }

    public boolean isNetworkAvailable() {
        System.out.println("**** isNetworkAvailable");
        return false;
    }

    public boolean isTV() {
        System.out.println("**** isTV");
        return false;
    }

    public boolean isWiFiAvailable() {
        System.out.println("**** isWiFiAvailable");
        return false;
    }

    public void onCreate(Bundle bundle) {
        CustomLoadFunction();
        this.expansionFileName = "main.8.com.newcityrp.launcher.obb";
        this.patchFileName = "patch.8.com.newcityrp.launcher.obb";
        this.apkFileName = GetPackageName("com.newcityrp.launcher");
        this.baseDirectory = GetGameBaseDirectory();
        NvUtil.getInstance().setActivity(this);
        NvUtil.getInstance().setAppLocalValue("STORAGE_ROOT", this.baseDirectory);
        NvUtil.getInstance().setAppLocalValue("STORAGE_ROOT_BASE", this.baseDirectoryRoot);
        super.onCreate(bundle);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        System.out.println("onRequestPermissionsResult");
        if (i == 8001) {
            String str = "Exiting App";
            if (iArr.length <= 0) {
                System.out.println(str);
                finish();
                return;
            }
            for (int i2 = 0; i2 < iArr.length; i2++) {
                if (iArr[0] != 0) {
                    System.out.println(str);
                    finish();
                    return;
                }
            }
            this.waitForPermissions = false;
            initGame();
        }
    }
}