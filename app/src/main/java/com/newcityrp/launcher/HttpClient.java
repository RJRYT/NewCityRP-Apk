package com.newcityrp.launcher;

import android.util.Log;
import java.util.Scanner;
import java.net.HttpURLConnection;
import android.content.Context;
import org.json.JSONObject;
import org.json.JSONException;
import java.net.URL;

public class HttpClient {
    private static final String TAG = "HttpClient";
    private static final String BASE_URL = "https://rjryt.github.io/samp/";
    private Context context;

    public interface DataCallback {
        void onSuccess(JSONObject data);
        void onFailure(String error);
    }

    public HttpClient(Context context) {
        this.context = context;
    }

    public void fetchData(String endpoint, DataCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder inline = new StringBuilder();
                    Scanner scanner = new Scanner(url.openStream());

                    // Read JSON data into a StringBuilder using Scanner
                    while (scanner.hasNext()) {
                        inline.append(scanner.nextLine());
                    }

                    scanner.close();

                    try {
                        // Parse the string into a JSONObject
                        JSONObject resultObject = new JSONObject(inline.toString());
                        callback.onSuccess(resultObject);
                    } catch (JSONException e) {
                        Log.d("Error: ",e.toString());
                        callback.onFailure(e.getMessage());
                    }
                } else {
                    callback.onFailure("Server returned: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data: " + e.getMessage());
                callback.onFailure(e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
