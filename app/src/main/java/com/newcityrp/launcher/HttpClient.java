package com.newcityrp.launcher;

import android.util.Log;
import java.util.Scanner;
import java.net.HttpURLConnection;
import android.content.Context;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
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
                    String inline = "";
                    Scanner scanner = new Scanner(url.openStream());

                    //Write all the JSON data into a string using a scanner
                    while (scanner.hasNext()) {
                        inline += scanner.nextLine();
                    }

                    //Close the scanner
                    scanner.close();

                    //Using the JSON simple library parse the string into a json object
                    JSONParser parse = new JSONParser();
                    JSONObject resultObject = (JSONObject) parse.parse(inline);

                    callback.onSuccess(resultObject);
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
