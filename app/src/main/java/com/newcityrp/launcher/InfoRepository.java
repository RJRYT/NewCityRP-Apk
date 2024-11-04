package com.newcityrp.launcher;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InfoRepository {
    private static final long CACHE_EXPIRY_TIME = 3600 * 1000; // 1 hour in milliseconds

    private CacheManager cacheManager;
    private HttpClient httpClient;

    public InfoRepository(Context context) {
        this.context = context;
        this.cacheManager = new CacheManager(context);
        this.httpClient = HttpClient.getInstance();
    }

    public void fetchServerInfo(DataCallback callback) {
        String cachedData = cacheManager.getCache(cacheManager.SERVER_INFO_KEY, CACHE_EXPIRY_TIME);

        if (cachedData != null) {
            // Return cached data if not expired
            try {
                callback.onSuccess(new JSONObject(cachedData));
            } catch (Exception e) {
                callback.onFailure(e);
            }
            return;
        }

        // Fetch from server if cache is missing or expired
        httpClient.fetchData("serverinfo.json", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    cacheManager.saveCache(SERVER_INFO_KEY, responseData);
                    try {
                        callback.onSuccess(new JSONObject(responseData));
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new IOException("Unexpected code " + response));
                }
            }
        });
    }
}
