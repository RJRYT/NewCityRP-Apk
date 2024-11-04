package com.newcityrp.launcher;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import android.content.Context;
import java.net.URL;

public class HttpClient {
    private static final String TAG = "HttpClient";
    private static final String BASE_URL = "https://rjryt.github.io/samp/";
    private Context context;

    public interface DataCallback {
        void onSuccess(String data);
        void onFailure(String error);
    }

    public HttpClient(Context context) {
        this.context = context;
    }

    public void fetchData(String endpoint, DataCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    callback.onSuccess(result.toString());
                } else {
                    callback.onFailure("Server returned: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data: " + e.getMessage());
                callback.onFailure(e.getMessage());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing reader: " + e.getMessage());
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
