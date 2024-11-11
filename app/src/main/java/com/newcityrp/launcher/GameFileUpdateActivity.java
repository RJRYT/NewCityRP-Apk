package com.newcityrp.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class GameFileUpdateActivity extends AppCompatActivity {

    private DownloadHelper downloadHelper;
    private TextView downloadStatusText;
    private TextView currentFileText;
    private TextView downloadSizeText;
    private TextView downloadSpeedText;
    private TextView estimatedTimeText;
    private ProgressBar downloadProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_file_update); // Use the XML layout you provided

        // Initialize views
        downloadStatusText = findViewById(R.id.downloadStatusText);
        currentFileText = findViewById(R.id.currentFileText);
        downloadSizeText = findViewById(R.id.downloadSizeText);
        downloadSpeedText = findViewById(R.id.downloadSpeedText);
        estimatedTimeText = findViewById(R.id.estimatedTimeText);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);

        downloadHelper = new DownloadHelper(this);

        // Directly start the download process as soon as the activity is created
        startDownloadProcess();
    }

    // Start the download process
    private void startDownloadProcess() {
        downloadHelper.getMissingFilesAndSizes(missingFiles -> {
            if (missingFiles == null || missingFiles.isEmpty()) {
                Toast.makeText(GameFileUpdateActivity.this, "No files to update.", Toast.LENGTH_SHORT).show();
                finish();  // Close the activity if no files need updating
                return;
            }

            long totalSize = downloadHelper.getTotalSize(missingFiles);
            downloadSizeText.setText("Total Size: " + (totalSize / 1024 / 1024) + " MB");

            // Start downloading the missing files and update progress
            downloadHelper.downloadFiles(missingFiles, new DownloadHelper.DownloadCallback() {
                @Override
                public void onProgressUpdate(int progressPercent, DownloadHelper.FileData currentFile) {
                    // Update the progress bar and current file information
                    downloadProgressBar.setProgress(progressPercent);
                    downloadStatusText.setText("Downloading... " + progressPercent + "%");
                    currentFileText.setText("Current File: " + currentFile.getName());
                }

                @Override
                public void onComplete() {
                    // Download is complete, notify user and return to MainActivity
                    Toast.makeText(GameFileUpdateActivity.this, "Game files updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(GameFileUpdateActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                    finish();
                }
            });
        });
    }
}
