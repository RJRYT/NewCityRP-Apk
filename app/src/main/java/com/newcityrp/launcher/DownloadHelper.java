package com.newcityrp.launcher;

import android.content.Context;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import android.opengl.GLES20;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class DownloadHelper {

    private final Context context;
    private HttpClient httpClient;

    public DownloadHelper(Context context) {
        this.context = context;
        this.httpClient = new HttpClient(context);
    }

    public boolean checkFilesFromServerWithLocalFiles(String dataUrl) {
        try {
            // Fetch the list of files from the server (full or lite list)
            URL url = new URL(dataUrl);  // Use the appropriate URL based on user selection (full or lite)
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

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
                File localFile = new File(context.getExternalFilesDir(null), filePath);  // Use appropriate folder for your app
                if (!localFile.exists()) {
                    // File is missing, return false
                    return false;
                }

                // Check if the local file's size matches the server file size
                long localFileSize = localFile.length();
                long serverFileSize = Long.parseLong(fileObject.getString("size"));
                if (localFileSize != serverFileSize) {
                    // File size mismatch, return false
                    return false;
                }

                // Check if the file's GPU compatibility matches the device's GPU (optional)
                String gpu = fileObject.getString("gpu");
                if (!gpu.equals("all") && !isGpuSupported(gpu)) {
                    // If the file is not compatible with the device's GPU, return false
                    return false;
                }
            }

            // All files are valid
            return true;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;  // Return false if there was an error during the check
        }
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

    // Helper function to check if GPU is supported
    public boolean isGpuSupported(String gpu) {
        // Assuming that we have a method to get the device's GPU type.
        String deviceGpu = getDeviceGpu();  // You should implement this to check the GPU type on the device
        return gpu.equals(deviceGpu);
    }

    // Placeholder function to get the device's GPU type
    public String getDeviceGpu() {
        String gpuVendor = GLES20.glGetString(GLES20.GL_VENDOR);
        String gpuRenderer = GLES20.glGetString(GLES20.GL_RENDERER);

        return gpuVendor + " " + gpuRenderer;
    }

}