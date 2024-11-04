package com.newcityrp.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Date;

public class CacheManager {
    private static final String CACHE_PREFS = "CachePrefs";
    public final String SERVER_INFO_KEY = "server_info";
    public final String SERVER_LIST_KEY = "server_list";
    private static final String TIMESTAMP_SUFFIX = "_timestamp";

    private SharedPreferences sharedPreferences;

    public CacheManager(Context context) {
        sharedPreferences = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
    }

    // Save data with timestamp
    public void saveCache(String key, String data) {
        sharedPreferences.edit()
            .putString(key, data)
            .putLong(key + TIMESTAMP_SUFFIX, new Date().getTime())
            .apply();
    }

    // Retrieve cached data if valid
    public String getCache(String key, long cacheDurationMs) {
        long timestamp = sharedPreferences.getLong(key + TIMESTAMP_SUFFIX, 0);
        if (new Date().getTime() - timestamp < cacheDurationMs) {
            return sharedPreferences.getString(key, null);
        }
        return null; // Cache expired
    }

    // Clear cache data by key
    public void clearCache(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    // Clear all cache data
    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}
