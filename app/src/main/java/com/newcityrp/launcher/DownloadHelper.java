package com.newcityrp.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

class DownloadHelper {

    private final Context context;
    private HttpClient httpClient;
    private LogManager loger;

    private static final String KEY_GPU_INFO = "gpu_info";
    private static final String PREFS_NAME = "AppPrefs";

    public class FileData {
        private String name;
        private String size;
        private String path;
        private String url;
        private String gpu;

        // Getter methods
        public String getSize() {
            return size;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public String getUrl() {
            return url;
        }

        public String getGpu() {
            return gpu;
        }
    }

    public DownloadHelper(Context context) {
        this.context = context;
        this.httpClient = new HttpClient(context);
        this.loger = new LogManager(context);
    }

    public boolean checkFilesFromServerWithLocalFiles(String dataUrl) {
        try {
            // Fetch the list of files from the server (full or lite list)
            URL url = new URL(dataUrl);  // Use the appropriate URL based on user selection (full or lite)
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();
            
            // Read the response from the server
            InputStream inputStream = urlConnection.getInputStream();
            String response = convertStreamToString(inputStream);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray filesArray = jsonResponse.getJSONArray("files");

            // Check each file
            for (int i = 0; i < filesArray.length(); i++) {
                JSONObject fileObject = filesArray.getJSONObject(i);
                String fileName = fileObject.getString("name");
                String filePath = fileObject.getString("path");
                String fileUrl = fileObject.getString("url");

                // Check if the file exists locally
                File localFile = new File(context.getExternalFilesDir(null), filePath);  // Use appropriate folder for your app
                if (!localFile.exists()) {
                    // File is missing, return false
                    return false;
                }

                // Check if the local file's size matches the server file size
                long localFileSize = localFile.length();
                long serverFileSize = Long.parseLong(fileObject.getString("size"));
                if (localFileSize != serverFileSize) {
                    // File size mismatch, return false
                    return false;
                }

                // Check if the file's GPU compatibility matches the device's GPU (optional)
                String gpu = fileObject.getString("gpu");
                if (!gpu.equals("all") && !isGpuSupported(gpu)) {
                    // If the file is not compatible with the device's GPU, return false
                    return false;
                }
            }

            // All files are valid
            return true;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;  // Return false if there was an error during the check
        }
    }

    // Helper function to convert InputStream to String (for reading the server's JSON response)
    private String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    // Callback interfaces for download progress and completion
    public interface Callback<T> {
        void onComplete(T result);
    }

    public interface DownloadCallback {
        void onProgressUpdate(int progressPercent, FileData currentFile);
        void onComplete();
    }

    // Method to get missing files and their sizes
    public void getMissingFilesAndSizes(Callback<List<FileData>> callback) {
        SharedPreferences preferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String dataUrl = preferences.getString("dataUrl", "");
        String sampUrl = preferences.getString("sampUrl", "");

        List<FileData> allFiles = new ArrayList<>();

        // Fetch files from data URL and samp URL, then combine them
        fetchFilesFromUrl(dataUrl, dataFiles -> {
            if (dataFiles != null) allFiles.addAll(dataFiles);

            fetchFilesFromUrl(sampUrl, sampFiles -> {
                if (sampFiles != null) allFiles.addAll(sampFiles);

                List<FileData> missingFiles = new ArrayList<>();
                for (FileData file : allFiles) {
                    File localFile = new File(context.getExternalFilesDir(null), file.getPath());
                    if (!localFile.exists()) missingFiles.add(file);
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(missingFiles));
            });
        });
    }

    // Helper method to fetch files from a URL and parse JSON using HttpURLConnection
    private void fetchFilesFromUrl(String urlString, Callback<List<FileData>> callback) {
        new Thread(() -> {
            List<FileData> fileDataList = new ArrayList<>();
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    String jsonResponse = convertStreamToString(inputStream);

                    // Parse the JSON response
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray filesArray = jsonObject.getJSONArray("files");

                    for (int i = 0; i < filesArray.length(); i++) {
                        JSONObject fileObject = filesArray.getJSONObject(i);
                        String name = fileObject.getString("name");
                        long size = fileObject.getLong("size");
                        String path = fileObject.getString("path");
                        String fileUrl = fileObject.getString("url");
                        String gpu = fileObject.getString("gpu");

                        FileData fileData = new FileData(name, size, path, fileUrl, gpu);
                        fileDataList.add(fileData);
                    }
                }

                callback.onComplete(fileDataList);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onComplete(null);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    // Method to download files and update progress using HttpURLConnection
    public void downloadFiles(List<FileData> files, DownloadCallback callback) {
        new Thread(() -> {
            long totalSize = getTotalSize(files);
            long downloadedSize = 0;

            for (FileData file : files) {
                File localFile = new File(context.getExternalFilesDir(null), file.getPath());
                localFile.getParentFile().mkdirs(); // Ensure the directories exist

                HttpURLConnection urlConnection = null;
                try {
                    URL fileUrl = new URL(file.getUrl());
                    urlConnection = (HttpURLConnection) fileUrl.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = urlConnection.getInputStream();
                        FileOutputStream outputStream = new FileOutputStream(localFile);

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            downloadedSize += bytesRead;

                            int percentComplete = (int) ((downloadedSize * 100) / totalSize);
                            new Handler(Looper.getMainLooper()).post(() -> callback.onProgressUpdate(percentComplete, file));
                        }

                        outputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }

            // Notify download completion
            new Handler(Looper.getMainLooper()).post(callback::onComplete);
        }).start();
    }

    // Method to calculate the total size of files
    public int getTotalSize(List<FileData> files) {
        int totalSize = 0;
        for (FileData file : files) {
            try {
                // Parse the size as an integer
                int fileSize = Integer.parseInt(file.getSize());
                totalSize += fileSize;
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Log or handle the error if parsing fails
            }
        }
        return totalSize;
    }

    // Helper function to check if GPU is supported
    public boolean isGpuSupported(String gpu) {
        // Assuming that we have a method to get the device's GPU type.
        String deviceGpu = getDeviceGpu();  // You should implement this to check the GPU type on the device
        return gpu.equals(deviceGpu);
    }
    public String getDeviceGpu() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String gpu = preferences.getString(KEY_GPU_INFO, "unknown");
        
        return (gpu != null) 
        ? (gpu.toLowerCase().contains("adreno") ? "dex"  // For Adreno GPUs
            : (gpu.toLowerCase().contains("powervr") ? "pvr"  // For PowerVR GPUs
            : "etc"))  // For other GPUs
        : "unknown";  // If GPU info is null or unavailable
    }
}