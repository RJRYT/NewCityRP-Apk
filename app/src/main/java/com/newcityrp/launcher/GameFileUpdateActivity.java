package com.newcityrp.launcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;

public class GameFileUpdateActivity extends AppCompatActivity {

    private TextView downloadStatusText, currentFileText, downloadSizeText, downloadSpeedText, estimatedTimeText;
    private ProgressBar downloadProgressBar;
    private String chosenGameType; // "lite" or "full"
    private Handler handler;
    private DownloadHelper downloadHelper;
    private HttpClient httpClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadHelper = new DownloadHelper(this);
        httpClient = new HttpClient(this);
        fetchGameFileURLs();

        if (!areGameFilesAvailable()) {
            setupGameTypeSelection();
        } else if(!checkFilesIsNeedUpdate()) {
            SharedPreferences apppref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            chosenGameType = apppref.getString("gameType", "full");
            startDownloadProcess();
        } else {
            finish();
        }
    }

    private void setupGameTypeSelection() {
        setContentView(R.layout.activity_game_type_selection);
        Button liteButton = findViewById(R.id.liteButton);
        Button fullButton = findViewById(R.id.fullButton);

        liteButton.setOnClickListener(v -> {
            chosenGameType = "lite";
            startDownloadProcess();
        });

        fullButton.setOnClickListener(v -> {
            chosenGameType = "full";
            startDownloadProcess();
        });
    }

    private void setupDownloadUI() {
        downloadStatusText = findViewById(R.id.downloadStatusText);
        currentFileText = findViewById(R.id.currentFileText);
        downloadSizeText = findViewById(R.id.downloadSizeText);
        downloadSpeedText = findViewById(R.id.downloadSpeedText);
        estimatedTimeText = findViewById(R.id.estimatedTimeText);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);
        handler = new Handler();
    }

    private boolean areGameFilesAvailable() {
        File gameFilesDir = new File(getExternalFilesDir(null), "");
        return gameFilesDir.exists() && gameFilesDir.isDirectory() && gameFilesDir.listFiles().length > 0;
    }

    private void startDownloadProcess() {
        setContentView(R.layout.activity_game_file_update);
        setupDownloadUI();
        fetchGameData(chosenGameType)
    }

    private void fetchGameFileURLs() {
        httpClient.fetchData("mobile/update.json", new HttpClient.DataCallback() {
            @Override
            public void onSuccess(JSONObject data) {
                String fullUrl = data.getString("data_full_url");
                String liteUrl = data.getString("data_lite_url");
                String sampUrl = data.getString("data_samp_url");

                SharedPreferences prefs = getSharedPreferences("GameUpdatePrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("data_full_url", fullUrl);
                editor.putString("data_lite_url", liteUrl);
                editor.putString("data_samp_url", sampUrl);
                editor.apply();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(this, "error on update", 1).show();
                finish();
            }
        });
    }

    private void fetchGameData(String gameType) {
        SharedPreferences prefs = getSharedPreferences("GameUpdatePrefs", MODE_PRIVATE);
        String dataUrl = gameType.equals("lite") ? prefs.getString("data_lite_url", "") : prefs.getString("data_full_url", "");
        String sampUrl = prefs.getString("data_samp_url", "");

        if (!dataUrl.isEmpty() && !sampUrl.isEmpty()) {
            downloadGameFiles(dataUrl);
            downloadGameFiles(sampUrl);
        } else {
            // Show error if URLs are missing
        }
    }

    private void downloadGameFiles(String url) {
        new Thread(() -> {
            try {
                JSONObject response = makeHttpRequest(url); // Replace with actual HTTP request function
                JSONArray filesArray = response.getJSONArray("files");

                for (int i = 0; i < filesArray.length(); i++) {
                    JSONObject fileObject = filesArray.getJSONObject(i);
                    // Extract file details and initiate download
                    updateDownloadProgress(i, filesArray.length(), fileObject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateDownloadProgress(int fileIndex, int totalFiles, JSONObject fileObject) {
        handler.post(() -> {
            // Update download UI with progress for each file
            downloadProgressBar.setProgress((fileIndex + 1) * 100 / totalFiles);
            currentFileText.setText("Current File: " + fileObject.optString("name", "N/A"));
            // Further UI updates like speed and estimated time based on your calculation
        });
    }

    private void checkFilesIsNeedUpdate() {
        SharedPreferences prefs = getSharedPreferences("GameUpdatePrefs", MODE_PRIVATE);
        String dataUrl = gameType.equals("lite") ? prefs.getString("data_lite_url", "") : prefs.getString("data_full_url", "");
        String sampUrl = prefs.getString("data_samp_url", "");

        if (!dataUrl.isEmpty() && !sampUrl.isEmpty()) {
            return !downloadHelper.checkFilesFromServerWithLocalFiles(dataUrl) && !downloadHelper.checkFilesFromServerWithLocalFiles(sampUrl)
        } else {
            return false;
        }
    }
}
