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
    private LogManager logManager;
    private UtilManager utilManager;
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
        logManager = new LogManager(this);
        utilManager = new UtilManager(this);

        // Directly start the download process as soon as the activity is created
        startDownloadProcess();
        logManager.logDebug("========GameFileUpdateActivity========");
    }
    
    private String formatSize(long sizeInBytes) {
    final String[] units = {"B", "KB", "MB", "GB", "TB"};
    double size = sizeInBytes;
    int unitIndex = 0;

    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
    }

    return String.format("%.2f %s", size, units[unitIndex]);
}

    // Start the download process
    private void startDownloadProcess() {
        downloadHelper.getMissingFilesAndSizes(missingFiles -> {
            if (missingFiles == null || missingFiles.isEmpty()) {
                Toast.makeText(GameFileUpdateActivity.this, "No files to update.", Toast.LENGTH_SHORT).show();
                utilManager.launchMainActivityFreshly(GameFileUpdateActivity.this);
                return;
            }

            long totalSize = downloadHelper.getTotalSize(missingFiles);
            downloadSizeText.setText(formatSize(totalSize));

            // Start downloading the missing files and update progress
            downloadHelper.downloadFiles(missingFiles, new DownloadHelper.DownloadCallback() {
                @Override
                public void onProgressUpdate(int progressPercent, DownloadHelper.FileData currentFile, String speed, String estimatedTimeLeft, String downloadedSize) {
                    // Update the progress bar and current file information
                    downloadProgressBar.setProgress(progressPercent);
                    downloadStatusText.setText("Downloading... " + progressPercent + "%");
                    currentFileText.setText(currentFile.getName());
                    downloadSpeedText.setText(speed);
                    estimatedTimeText.setText(estimatedTimeLeft + " left");
                    downloadSizeText.setText(downloadedSize+"/"+formatSize(totalSize));
                }

                @Override
                public void onComplete() {
                    // Download is complete, notify user and return to MainActivity
                    Toast.makeText(GameFileUpdateActivity.this, "Game files updated successfully!", Toast.LENGTH_SHORT).show();
                    utilManager.launchMainActivityFreshly(GameFileUpdateActivity.this);
                }
            });
        });
    }
}
