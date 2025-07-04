package com.newcityrp.launcher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private SharedPreferences preferences, apppref;
    private ExecutorService executorService;
    private ExecutorService downloadService;

    private static final String KEY_GPU_INFO = "gpu_info";
    private static final String PREFS_NAME = "AppPrefs";

    public class FileData {
        private String name;
        private long size;
        private String path;
        private String url;
        private String gpu;

        public FileData(String name, long size, String path, String url, String gpu) {
            this.name = name;
            this.size = size;
            this.path = path;
            this.url = url;
            this.gpu = gpu;
        }

        // Getter methods
        public long getSize() {
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

    private class DownloadProgress {
        long downloadedSize;
        long totalSize;
        long startTime;

        public DownloadProgress(long downloadedSize, long totalSize, long startTime) {
            this.downloadedSize = downloadedSize;
            this.totalSize = totalSize;
            this.startTime = startTime;
        }

        public void addDownloadProgress(int progress) {
            downloadedSize += progress;
        }

        public int getCompletedPercentage() {
            return (int) ((downloadedSize * 100) / totalSize);
        }

        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }

        public double getDownloadSpeed(long elapsedTime) {
            return (downloadedSize / 1024.0) / (elapsedTime / 1000.0);
        }

        public long getRemainingSize() {
            return totalSize - downloadedSize;
        }

        public long getEstimatedTimeLeft(long remainingSize, double speed) {
            return (long) (remainingSize / (speed * 1024));
        }
    }

    public DownloadHelper(Context context) {
        this.context = context;
        this.httpClient = new HttpClient(context);
        this.loger = new LogManager(context);
        preferences = context.getSharedPreferences("GameUpdatePrefs", Context.MODE_PRIVATE);
        apppref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();
        downloadService = Executors.newFixedThreadPool(4);
    }

    public void checkFilesFromServerWithLocalFiles(final String dataUrl, final FileCheckCallback<Boolean> callback) {
        // Run network tasks in a background thread
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Fetch the list of files from the server (full or lite list)
                    URL url = new URL(dataUrl); // Use the appropriate URL based on user selection (full or lite)
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.setReadTimeout(15000);
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
                            File localFile = new File(context.getExternalFilesDir(null), filePath); // Use appropriate folder for your app
                            String gpu = fileObject.getString("gpu");
                            if (!localFile.exists()) {
                                if (gpu.equals("all") || isGpuSupported(gpu)) {
                                    loger.logDebug("checkFilesFromServerWithLocalFiles: file didnt exist: ",fileName);
                                    callback.onResult(false); // File is missing, return false
                                    return;
                                }
                            }

                            // Check if the local file's size matches the server file size
                            long localFileSize = localFile.length();
                            long serverFileSize = Long.parseLong(fileObject.getString("size"));
                            if (localFileSize != serverFileSize) {
                                if (gpu.equals("all") || isGpuSupported(gpu)){
                                    loger.logDebug("checkFilesFromServerWithLocalFiles: file size didnt match: ",fileName);
                                    callback.onResult(false); // File size mismatch, return false
                                    return;
                                }
                            }
                        }

                        // All files are valid
                        callback.onResult(true);
                    } else {
                        loger.logDebug("checkFilesFromServerWithLocalFiles: Network error: ", urlConnection.getResponseCode());
                        callback.onResult(false);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    callback.onResult(false); // Return false if there was an error during the check
                }
            }
        });
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
    public interface FilesCallback<T> {
        void onComplete(T result);
    }

    public interface FileCheckCallback<T> {
        void onResult(T result);
    }

    public interface DownloadCallback {
        void onProgressUpdate(int percentComplete, FileData file, String speed, String estimatedTimeLeft, String downloadedSize);
        void onComplete();
        void onError(String error);
    }

    // Method to get missing files and their sizes
    public void getMissingFilesAndSizes(FilesCallback<List<FileData>> callback) {
        String chosenGameType = apppref.getString("gameType", "full");
        String dataUrl = chosenGameType.equals("lite") ? preferences.getString("data_lite_url", "") : preferences.getString("data_full_url", "");
        String sampUrl = preferences.getString("data_samp_url", "");

        List<FileData> allFiles = new ArrayList<>();

        // Fetch files from data URL and samp URL, then combine them
        fetchFilesFromUrl(dataUrl, dataFiles -> {
            if (dataFiles != null) allFiles.addAll(dataFiles);

            fetchFilesFromUrl(sampUrl, sampFiles -> {
                if (sampFiles != null) allFiles.addAll(sampFiles);

                List<FileData> missingFiles = new ArrayList<>();
                for (FileData file : allFiles) {
                    File localFile = new File(context.getExternalFilesDir(null), file.getPath());
                    String fileGpu = file.getGpu();

                    if (!localFile.exists()) {
                        // Check if the file is compatible with the device's GPU
                        if (fileGpu.equals("all") || isGpuSupported(fileGpu)) {
                            loger.logDebug("getMissingFilesAndSizes: missing file: ",file.getName());
                            missingFiles.add(file); // Add only if the file is compatible
                        }
                    } else {
                        long localFileSize = localFile.length();
                        long serverFileSize = file.getSize();
                        if (localFileSize != serverFileSize) {
                            loger.logDebug("getMissingFilesAndSizes: file size mismatch: ",file.getName());
                            missingFiles.add(file); // Add only if the file is compatible
                        }
                    }
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(missingFiles));
            });
        });
    }

    // Helper method to fetch files from a URL and parse JSON using HttpURLConnection
    private void fetchFilesFromUrl(String urlString, FilesCallback<List<FileData>> callback) {
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
        downloadService.submit(() -> {
            DownloadProgress downloadProgress = new DownloadProgress(0, getTotalSize(files), System.currentTimeMillis());

            for (FileData file : files) {
                File localFile = new File(context.getExternalFilesDir(null), file.getPath());
                localFile.getParentFile().mkdirs(); // Ensure the directories exist
                loger.logDebug("downloadFiles: ",file.getName());
                HttpURLConnection urlConnection = null;
                try {
                    if (!isNetworkAvailable()) {
                        throw new IOException("Network is unavailable. Please check your connection.");
                    }

                    URL fileUrl = new URL(file.getUrl());
                    urlConnection = (HttpURLConnection) fileUrl.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = urlConnection.getInputStream();
                        FileOutputStream outputStream = new FileOutputStream(localFile);

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        int lastReportedPercentage = -1;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            downloadProgress.addDownloadProgress(bytesRead);

                            // Calculate percent complete
                            int percentComplete = downloadProgress.getCompletedPercentage();

                            // Calculate elapsed time and speed
                            long elapsedTime = downloadProgress.getElapsedTime();
                            double speed = downloadProgress.getDownloadSpeed(elapsedTime); // KB/s

                            // Estimate time remaining
                            long remainingSize = downloadProgress.getRemainingSize();
                            long estimatedTimeLeft = downloadProgress.getEstimatedTimeLeft(remainingSize, speed); // seconds

                            // Update UI with progress, speed, and time left
                            if (percentComplete != lastReportedPercentage) { 
                                lastReportedPercentage = percentComplete; // Update last reported percentage

                                String finalSpeed = formatSpeed(speed);
                                String downloadedSizeFormatted = formatSize(downloadProgress.downloadedSize);
                                String finalEstimatedTimeLeft = formatTime(estimatedTimeLeft);
                                new Handler(Looper.getMainLooper()).post(() -> callback.onProgressUpdate(percentComplete, file, finalSpeed, finalEstimatedTimeLeft, downloadedSizeFormatted));
                            }
                        }

                        outputStream.close();
                    } else {
                        throw new IOException("Server error. HTTP response code: " + urlConnection.getResponseCode());
                    }
                } catch (IOException e) {
                    // Handle network issues, server issues, or file issues
                    e.printStackTrace();
                    String errorMessage = "Download failed: " + e.getMessage();
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(errorMessage));

                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "Unexpected error occurred: " + e.getMessage();
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(errorMessage));
                    shutdown();
                } finally {
                    try {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Notify download completion or error
            new Handler(Looper.getMainLooper()).post(() -> {
                if (downloadProgress.downloadedSize == downloadProgress.totalSize) {
                    callback.onComplete();
                } else {
                    callback.onError("Download incomplete. Some files may not have been downloaded.");
                }
            });
        });
    }

    // Helper method to format speed
    private String formatSpeed(double speed) {
        if (speed >= 1024) {
            return String.format("%.2f MB/s", speed / 1024);
        } else {
            return String.format("%.2f KB/s", speed);
        }
    }

    // Helper method to format time
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long remainingSeconds = seconds % 3600;
        long minutes = remainingSeconds / 60;
        remainingSeconds = remainingSeconds % 60;

        if (hours > 0) {
            return String.format("%d hr %d min %d sec", hours, minutes, remainingSeconds);
        } else if (minutes > 0) {
            return String.format("%d min %d sec", minutes, remainingSeconds);
        } else {
            return String.format("%d sec", remainingSeconds);
        }
    }
    
    // Helper function to check network availability
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    
    // Format size in a human-readable form (KB, MB, GB)
    private String formatSize(long sizeInBytes) {
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        double size = sizeInBytes;
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    // Method to calculate the total size of files
    public long getTotalSize(List<FileData> files) {
        long totalSize = 0;
        for (FileData file : files) {
            long fileSize = file.getSize();
            totalSize += fileSize;
        }
        return totalSize;
    }

    public void checkUpdates(final FileCheckCallback<Boolean> callback) {
        String chosenGameType = apppref.getString("gameType", "full");
        String dataUrl = chosenGameType.equals("lite") ? preferences.getString("data_lite_url", "") : preferences.getString("data_full_url", "");
        String sampUrl = preferences.getString("data_samp_url", "");

        if (dataUrl.isEmpty() || sampUrl.isEmpty()) {
            callback.onResult(true); // If either URL is empty, we assume an update is needed
            return;
        }

        // Run both checks asynchronously
        checkFilesFromServerWithLocalFiles(dataUrl, new FileCheckCallback<Boolean>() {
            @Override
            public void onResult(Boolean dataStatus) {
                // Check sampUrl after dataUrl completes
                loger.logDebug("checkUpdates: dataStatus: ",dataStatus);
                checkFilesFromServerWithLocalFiles(sampUrl, new FileCheckCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean sampStatus) {
                        // If either dataStatus or sampStatus is false, we need an update
                        loger.logDebug("checkUpdates: sampStatus: ",sampStatus);
                        boolean updateNeeded = !dataStatus || !sampStatus;
                        loger.logDebug("checkUpdates: updateNeeded: ",updateNeeded);
                        callback.onResult(updateNeeded);
                    }
                });
            }
        });
    }

    // Helper function to check if GPU is supported
    public boolean isGpuSupported(String gpu) {
        // Assuming that we have a method to get the device's GPU type.
        String deviceGpu = getDeviceGpu();  // You should implement this to check the GPU type on the device
        return gpu.equals(deviceGpu);
    }

    public String getDeviceGpu() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String gpu = preferences.getString(KEY_GPU_INFO, null);
        
        return (gpu != null) 
        ? (gpu.toLowerCase().contains("adreno") ? "dxt"  // For Adreno GPUs
            : (gpu.toLowerCase().contains("powervr") ? "pvr"  // For PowerVR GPUs
            : "etc"))  // For other GPUs
        : "unknown";  // If GPU info is null or unavailable
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (downloadService != null) {
            downloadService.shutdown();
        }
    }
}




