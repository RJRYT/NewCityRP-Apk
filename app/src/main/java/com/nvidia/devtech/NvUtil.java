package com.nvidia.devtech;

import android.app.Activity;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class NvUtil {
    private static NvUtil instance = new NvUtil();
    private Activity activity = null;
    private HashMap<String, String> appLocalValues;

    private NvUtil() {
        HashMap hashMap = new HashMap();
        this.appLocalValues = hashMap;
        hashMap.put("STORAGE_ROOT", Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static NvUtil getInstance() {
        return instance;
    }

    public void appendLog(String str) {
        String appLocalValue = getAppLocalValue("STORAGE_ROOT");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appLocalValue);
        stringBuilder.append("SAMP/javalog.txt");
        File file = new File(stringBuilder.toString());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.append(str);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public String getAppLocalValue(String str) {
        return (String) this.appLocalValues.get(str);
    }

    public String getParameter(String str) {
        return this.activity.getIntent().getStringExtra(str);
    }

    public boolean hasAppLocalValue(String str) {
        return this.appLocalValues.containsKey(str);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setAppLocalValue(String str, String str2) {
        this.appLocalValues.put(str, str2);
    }
}