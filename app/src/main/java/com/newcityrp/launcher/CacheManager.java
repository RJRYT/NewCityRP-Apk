package com.newcityrp.launcher;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class CacheManager {
    
    private static final String CACHE_DIR = "cache"; // Directory to store cache files
    private static final String TIMESTAMP_SUFFIX = "_timestamp";
    public final String SERVER_INFO_KEY = "server_info.json";
    public final String SERVER_LIST_KEY = "server_list.json";

    private Context context;
    private LogManager logManager;

    public CacheManager(Context context) {
        this.context = context;
        this.logManager = new LogManager(context);  // Initialize LogManager
    }

    // Save data with timestamp in a file-based cache
    public void saveCache(String filename, String data) {
        File cacheFile = new File(context.getCacheDir(), CACHE_DIR + File.separator + filename);
        File timestampFile = new File(context.getCacheDir(), CACHE_DIR + File.separator + filename + TIMESTAMP_SUFFIX);

        try {
            // Ensure the directory exists
            File cacheDir = cacheFile.getParentFile();
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                logManager.logError("Failed to create cache directory");
                throw new IOException("Failed to create cache directory");
            }

            // Write data to the cache file
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(data.getBytes(StandardCharsets.UTF_8));
            }

            // Save the current timestamp to timestamp file
            try (FileOutputStream fos = new FileOutputStream(timestampFile)) {
                fos.write(String.valueOf(new Date().getTime()).getBytes(StandardCharsets.UTF_8));
            }

            logManager.logInfo("Cache saved successfully: " + filename);

        } catch (IOException e) {
            logManager.logError("Error saving cache to file: " + e.getMessage());
        }
    }

    // Retrieve cached data from a file-based cache if valid
    public String getCache(String filename, long cacheDurationMs) {
        File cacheFile = new File(context.getCacheDir(), CACHE_DIR + File.separator + filename);
        File timestampFile = new File(context.getCacheDir(), CACHE_DIR + File.separator + filename + TIMESTAMP_SUFFIX);

        // Check if the cache and timestamp files exist
        if (!cacheFile.exists() || !timestampFile.exists()) {
            logManager.logDebug("Cache file or timestamp file does not exist for: " + filename);
            return null; // Cache file doesn't exist
        }

        try {
            // Check cache expiration
            long timestamp = getTimestamp(timestampFile);
            if (new Date().getTime() - timestamp < cacheDurationMs) {
                // Read cached data from the file
                return readFile(cacheFile);
            } else {
                logManager.logDebug("Cache expired for: " + filename);
                return null; // Cache expired
            }
        } catch (IOException e) {
            logManager.logError("Error retrieving cache: " + e.getMessage());
        }

        return null; // Default return if an error occurred
    }

    // Helper method to read data from a file
    private String readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    // Helper method to read the timestamp from the file
    private long getTimestamp(File timestampFile) throws IOException {
        String timestampStr = readFile(timestampFile);
        try {
            return Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            logManager.logError("Error parsing timestamp: " + e.getMessage());
            throw new IOException("Invalid timestamp format");
        }
    }

    // Clear cache data by filename
    public void clearCache(String filename) {
        File cacheFile = new File(context.getCacheDir(), CACHE_DIR + File.separator + filename);
        File timestampFile = new File(context.getCacheDir(), CACHE_DIR + File.separator + filename + TIMESTAMP_SUFFIX);

        if (cacheFile.exists()) {
            if (!cacheFile.delete()) {
                logManager.logError("Failed to delete cache file: " + filename);
            } else {
                logManager.logInfo("Deleted cache file: " + filename);
            }
        }

        if (timestampFile.exists()) {
            if (!timestampFile.delete()) {
                logManager.logError("Failed to delete timestamp file: " + filename);
            } else {
                logManager.logInfo("Deleted timestamp file: " + filename);
            }
        }
    }

    // Clear all cache data in the cache directory
    public void clearAll() {
        File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            for (File file : cacheDir.listFiles()) {
                if (!file.delete()) {
                    logManager.logError("Failed to delete cache file: " + file.getName());
                } else {
                    logManager.logInfo("Deleted cache file: " + file.getName());
                }
            }
        }
    }
}