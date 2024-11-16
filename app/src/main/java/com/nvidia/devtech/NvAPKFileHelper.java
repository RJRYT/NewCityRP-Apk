package com.nvidia.devtech;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class NvAPKFileHelper {
    private static NvAPKFileHelper instance = new NvAPKFileHelper();
    private static final boolean logAssetFiles = false;
    private int READ_MODE_ONLY = 268435456;
    int apkCount = 0;
    String[] apkFiles;
    private Context context = null;
    boolean hasAPKFiles = false;
    int myApkCount = 0;

    private int findInAPKFiles(String str) {
        if (this.myApkCount == 0) {
            return -1;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(".mp3");
        String stringBuilder2 = stringBuilder.toString();
        int i = 0;
        while (true) {
            String[] strArr = this.apkFiles;
            if (i >= strArr.length) {
                return -1;
            }
            if (str.compareToIgnoreCase(strArr[i]) == 0) {
                break;
            } else if (stringBuilder2.compareToIgnoreCase(this.apkFiles[i]) == 0) {
                break;
            } else {
                i++;
            }
        }
        str.compareTo(this.apkFiles[i]);
        return i;
    }

    public static NvAPKFileHelper getInstance() {
        return instance;
    }

    public void AddAssetFile(String str) {
        String[] strArr = this.apkFiles;
        int i = this.myApkCount;
        this.myApkCount = i + 1;
        strArr[i] = str;
    }

    public void GetAssetList() {
        String readLine;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.context.getAssets().open("assetfile.txt")));
            int parseInt = Integer.parseInt(bufferedReader.readLine());
            this.myApkCount = 0;
            if (parseInt > 0) {
                this.apkFiles = new String[parseInt];
                while (true) {
                    readLine = bufferedReader.readLine();
                    if (readLine != null) {
                        String[] strArr = this.apkFiles;
                        int i = this.myApkCount;
                        this.myApkCount = i + 1;
                        strArr[i] = readLine;
                    } else {
                        return;
                    }
                }
            }
        } catch (Exception unused) {
            AssetManager assets = this.context.getAssets();
            readLine = "";
            getDirectoryListing(assets, readLine, 0);
            getDirectoryListing(assets, readLine, this.apkCount);
        }
    }

    public void closeFileAndroid(NvAPKFile nvAPKFile) {
        try {
            nvAPKFile.is.close();
        } catch (IOException unused) {
        }
        nvAPKFile.data = new byte[0];
        nvAPKFile.is = null;
    }

    public int getDirectoryListing(AssetManager assetManager, String str, int i) {
        try {
            if (this.apkFiles == null && i > 0) {
                this.apkFiles = new String[i];
            }
            String[] list = assetManager.list(str);
            if (list.length == 0) {
                if (i > 0) {
                    AddAssetFile(str);
                } else {
                    this.apkCount++;
                }
            }
            for (int i2 = 0; i2 < list.length; i2++) {
                String str2 = "/";
                StringBuilder stringBuilder;
                String stringBuilder2;
                if (list[i2].indexOf(46) == -1) {
                    if (str.length() > 0) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(str);
                        stringBuilder.append(str2);
                        stringBuilder.append(list[i2]);
                        stringBuilder2 = stringBuilder.toString();
                    } else {
                        stringBuilder2 = list[i2];
                    }
                    getDirectoryListing(assetManager, stringBuilder2, i);
                } else if (i > 0) {
                    if (str.length() > 0) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(str);
                        stringBuilder.append(str2);
                        stringBuilder.append(list[i2]);
                        stringBuilder2 = stringBuilder.toString();
                    } else {
                        stringBuilder2 = list[i2];
                    }
                    AddAssetFile(stringBuilder2);
                } else {
                    this.apkCount++;
                }
            }
        } catch (Exception e) {
            PrintStream printStream = System.out;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("ERROR: getDirectoryListing ");
            stringBuilder3.append(e.getMessage());
            printStream.println(stringBuilder3.toString());
        }
        return 0;
    }

    public NvAPKFile openFileAndroid(String str) {
        if (!this.hasAPKFiles) {
            this.apkCount = 0;
            this.apkFiles = null;
            GetAssetList();
            this.hasAPKFiles = true;
        }
        int findInAPKFiles = findInAPKFiles(str);
        if (findInAPKFiles == -1) {
            return null;
        }
        NvAPKFile nvAPKFile = new NvAPKFile();
        nvAPKFile.is = null;
        nvAPKFile.length = 0;
        nvAPKFile.position = 0;
        nvAPKFile.bufferSize = 0;
        try {
            InputStream open = this.context.getAssets().open(this.apkFiles[findInAPKFiles]);
            nvAPKFile.is = open;
            nvAPKFile.length = open.available();
            nvAPKFile.is.mark(this.READ_MODE_ONLY);
            nvAPKFile.bufferSize = 1024;
            nvAPKFile.data = new byte[1024];
            return nvAPKFile;
        } catch (Exception unused) {
            return null;
        }
    }

    public void readFileAndroid(NvAPKFile nvAPKFile, int i) {
        if (i > nvAPKFile.bufferSize) {
            nvAPKFile.data = new byte[i];
            nvAPKFile.bufferSize = i;
        }
        try {
            nvAPKFile.is.read(nvAPKFile.data, 0, i);
            nvAPKFile.position += i;
        } catch (IOException unused) {
        }
    }

    public long seekFileAndroid(NvAPKFile nvAPKFile, int i) {
        long j = 0;
        try {
            nvAPKFile.is.reset();
            long j2 = 0;
            int i2 = 128;
            while (i > 0 && i2 > 0) {
                try {
                    j2 = nvAPKFile.is.skip((long) i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                j += j2;
                i = (int) (((long) i) - j2);
                i2--;
            }
        } catch (IOException unused) {
        }
        nvAPKFile.position = (int) j;
        return j;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}