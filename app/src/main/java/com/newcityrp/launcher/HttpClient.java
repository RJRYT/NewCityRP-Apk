package com.newcityrp.launcher;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpClient {
    private static final String BASE_URL = "http://rjryt.github.io/samp/";

    private static HttpClient instance;
    private OkHttpClient client;

    private HttpClient() {
        client = new OkHttpClient();
    }

    public static HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    public void fetchData(String endpoint, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
