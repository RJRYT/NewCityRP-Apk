package com.newcityrp.launcher;

import android.content.Context;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;

public class ServerListRepository {
    private static final long CACHE_EXPIRY_TIME = 3600 * 1000; // 1 hour in milliseconds

    private CacheManager cacheManager;
    private HttpClient httpClient;
    private LogManager logManager;
    private Context context;

    public ServerListRepository(Context context) {
        this.cacheManager = new CacheManager(context);
        this.httpClient = new HttpClient(context);
    }

    public interface DataCallback {
        void onSuccess(JSONObject data);
        void onFailure(String error);
    }

    public void fetchServerList(DataCallback callback) {
        logManager = new LogManager(context);
        String cachedData = cacheManager.getCache(cacheManager.SERVER_LIST_KEY, CACHE_EXPIRY_TIME);
        if (cachedData != null) {
            try {
                JSONObject cacheObject = new JSONObject(cachedData);
                callback.onSuccess(cacheObject);
            } catch (JSONException e) {
                logManager.logError("[fetchServerList]Error: ",e.toString());
                callback.onFailure(e.getMessage());
            }
        } else {
            httpClient.fetchData("mobile/servers.json", new HttpClient.DataCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    cacheManager.saveCache(cacheManager.SERVER_LIST_KEY, data.toString());
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
