package com.newcityrp.launcher;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;

public class InfoRepository {
    private static final long CACHE_EXPIRY_TIME = 3600 * 1000; // 1 hour in milliseconds

    private CacheManager cacheManager;
    private HttpClient httpClient;

    public InfoRepository(Context context) {
        this.cacheManager = new CacheManager(context);
        this.httpClient = new HttpClient(context);
    }

    public interface DataCallback {
        void onSuccess(JSONObject data);
        void onFailure(String error);
    }

    public void fetchServerInfo(DataCallback callback) {
        String cachedData = cacheManager.getCache(cacheManager.SERVER_INFO_KEY, CACHE_EXPIRY_TIME);
        if (cachedData != null) {
            try {
                JSONObject cacheObject = new JSONObject(cachedData);
                callback.onSuccess(cacheObject);
            } catch (JSONException e) {
                Log.d("Error: ",e.toString());
                callback.onFailure(e.getMessage());
            }
        } else {
            httpClient.fetchData("serverinfo.json", new HttpClient.DataCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    cacheManager.saveCache(cacheManager.SERVER_INFO_KEY, data.toString());
                    callback.onSuccess(data);
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        }
    }
}
