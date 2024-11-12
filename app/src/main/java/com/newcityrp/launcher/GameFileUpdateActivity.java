package com.newcityrp.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class GameFileUpdateActivity extends AppCompatActivity {

    private DownloadHelper downloadHelper;
    private LogManager logManager;
    private UtilManager utilManager;
    private TextView downloadStatusText;
    private TextView downloadErrorText;
    private TextView currentFileText;
    private TextView downloadSizeText;
    private TextView downloadSpeedText;
    private TextView estimatedTimeText;
    private ProgressBar downloadProgressBar;
    private DownloadFilesTask downloadTask;
    private long totalSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_file_update); // Use the XML layout you provided

        // Initialize views
        downloadStatusText = findViewById(R.id.downloadStatusText);
        downloadErrorText = findViewById(R.id.downloadErrorText);
        currentFileText = findViewById(R.id.currentFileText);
        downloadSizeText = findViewById(R.id.downloadSizeText);
        downloadSpeedText = findViewById(R.id.downloadSpeedText);
        estimatedTimeText = findViewById(R.id.estimatedTimeText);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);

        downloadHelper = new DownloadHelper(this);
        logManager = new LogManager(this);
        utilManager = new UtilManager(this);

        // Directly start the download process as soon as the activity is created
        downloadTask = new DownloadFilesTask();
        downloadTask.execute();
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

    public class DownloadProgressData {
        private int progressPercent;
        private DownloadHelper.FileData currentFile;
        private String speed;
        private String estimatedTimeLeft;
        private String downloadedSize;

        public DownloadProgressData(int progressPercent, DownloadHelper.FileData currentFile, String speed, String estimatedTimeLeft, String downloadedSize) {
            this.progressPercent = progressPercent;
            this.currentFile = currentFile;
            this.speed = speed;
            this.estimatedTimeLeft = estimatedTimeLeft;
            this.downloadedSize = downloadedSize;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public DownloadHelper.FileData getCurrentFile() {
            return currentFile;
        }

        public String getSpeed() {
            return speed;
        }

        public String getEstimatedTimeLeft() {
            return estimatedTimeLeft;
        }

        public String getDownloadedSize() {
            return downloadedSize;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setMessage("Are you sure you want to cancel the download?")
            .setCancelable(false)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    // Start the download process
    private class DownloadFilesTask extends AsyncTask<Void, DownloadProgressData, String> {

        @Override
        protected void onPreExecute() {
            // Initial setup before download
            downloadStatusText.setText("Preparing to download...");
        }

        @Override
        protected String doInBackground(Void... voids) {
            downloadHelper.getMissingFilesAndSizes(missingFiles -> {
                if (missingFiles == null || missingFiles.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(GameFileUpdateActivity.this, "No files to update.", Toast.LENGTH_SHORT).show();
                        utilManager.launchMainActivityFreshly(GameFileUpdateActivity.this);
                    });
                    return;
                }

                totalSize = downloadHelper.getTotalSize(missingFiles);
                runOnUiThread(() -> downloadSizeText.setText(formatSize(totalSize)));

                downloadHelper.downloadFiles(missingFiles, new DownloadHelper.DownloadCallback() {
                    @Override
                    public void onProgressUpdate(int progressPercent, DownloadHelper.FileData currentFile, String speed, String estimatedTimeLeft, String downloadedSize) {
                        // Create a DownloadProgressData object and publish the progress
                        DownloadProgressData progressData = new DownloadProgressData(progressPercent, currentFile, speed, estimatedTimeLeft, downloadedSize);
                        publishProgress(progressData);

                        // Optionally release references after progress update
                        currentFile = null;  // Free memory by nullifying large object
                    }

                    @Override
                    public void onComplete() {
                        runOnUiThread(() -> {
                            Toast.makeText(GameFileUpdateActivity.this, "Game files updated successfully!", Toast.LENGTH_SHORT).show();
                            utilManager.launchMainActivityFreshly(GameFileUpdateActivity.this);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            downloadErrorText.setText(error+ " We recommend to restart the app");
                            downloadProgressBar.setVisibility(View.GONE);
                            downloadStatusText.setVisibility(View.GONE);
                            currentFileText.setVisibility(View.GONE);
                            downloadSpeedText.setVisibility(View.GONE);
                            estimatedTimeText.setVisibility(View.GONE);
                            downloadSizeText.setVisibility(View.GONE);
                            downloadHelper.shutdown();
                        });
                    }
                });
            });
            return "Completed";
        }

        @Override
        protected void onProgressUpdate(DownloadProgressData... values) {
            if (values.length > 0) {
                DownloadProgressData progressData = values[0];

                // Update UI with the new progress
                downloadProgressBar.setProgress(progressData.getProgressPercent());
                downloadStatusText.setText("Downloading... " + progressData.getProgressPercent() + "%");
                currentFileText.setText(progressData.getCurrentFile().getName());
                downloadSpeedText.setText(progressData.getSpeed());
                estimatedTimeText.setText(progressData.getEstimatedTimeLeft() + " left");
                downloadSizeText.setText(progressData.getDownloadedSize() + "/" + formatSize(totalSize));

                // Nullify the reference so the object can be garbage collected when no longer used
                progressData = null;  // Let the garbage collector handle cleanup
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle task completion, update UI
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadTask != null && downloadTask.getStatus() == AsyncTask.Status.RUNNING) {
            downloadTask.cancel(true); // This will stop the download
        }
        downloadHelper.shutdown();  // Shutdown the services when the activity is destroyed
    }
}
