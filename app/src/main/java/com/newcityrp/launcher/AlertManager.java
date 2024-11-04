package com.newcityrp.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.Queue;

public class AlertManager {
    private final Activity activity;
    private final Queue<Alert> alertQueue = new LinkedList<>();
    private boolean isShowingAlert = false;

    public AlertManager(Activity activity) {
        this.activity = activity;
    }

    public enum AlertType {
        ERROR,
        INFO,
        SUCCESS
    }

    public void showAlert(String message, AlertType type) {
        // Create an alert object and add it to the queue
        Alert alert = new Alert(message, type);
        alertQueue.offer(alert);
        // Attempt to display the alert if none is currently showing
        if (!isShowingAlert) {
            displayNextAlert();
        }
    }

    private void displayNextAlert() {
        if (alertQueue.isEmpty()) {
            return; // No alerts to display
        }

        isShowingAlert = true;
        Alert alert = alertQueue.poll();
        // Inflate the custom alert layout
        View alertView = LayoutInflater.from(activity).inflate(R.layout.custom_alert_view, null);

        // Get the alert text view
        TextView alertText = alertView.findViewById(R.id.alert_text);
        ImageView alertIcon = alertView.findViewById(R.id.alert_icon);
        alertText.setText(alert.message);

        // Set the background color based on the alert type
        
        switch (alert.type) {
            case ERROR:
                alertView.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light Red for error background
                alertText.setTextColor(Color.parseColor("#B71C1C")); // Dark Red for error text
                alertIcon.setImageResource(R.drawable.ic_error);
                alertIcon.setColorFilter(Color.parseColor("#B71C1C"));
                break;
            case INFO:
                alertView.setBackgroundColor(Color.parseColor("#BBDEFB")); // Light Blue for info background
                alertText.setTextColor(Color.parseColor("#0D47A1")); // Dark Blue for info text
                alertIcon.setImageResource(R.drawable.ic_info);
                alertIcon.setColorFilter(Color.parseColor("#0D47A1"));
                break;
            case SUCCESS:
                alertView.setBackgroundColor(Color.parseColor("#C8E6C9")); // Light Green for success background
                alertText.setTextColor(Color.parseColor("#1B5E20")); // Dark Green for success text
                alertIcon.setImageResource(R.drawable.ic_success);
                alertIcon.setColorFilter(Color.parseColor("#1B5E20"));
                break;
        }

        // Set layout parameters and add it to your parent view
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        // Add alert view to the current activity's content view
        ((ViewGroup) activity.findViewById(android.R.id.content)).addView(alertView, params);

        // Set initial position off the screen for dropdown animation
        alertView.setTranslationY(-alertView.getHeight());

        // Create a dropdown animation
        alertView.animate()
                .translationY(0)
                .setDuration(300)
                .withEndAction(() -> {
                    // Automatically hide the alert after a delay
                    alertView.postDelayed(() -> hideAlert(alertView), 3000);
                });
    }

    private void hideAlert(View alertView) {
        // Create an upward animation for hiding
        alertView.animate()
                .translationY(-alertView.getHeight())
                .setDuration(300)
                .withEndAction(() -> {
                    ((ViewGroup) activity.findViewById(android.R.id.content)).removeView(alertView);
                    isShowingAlert = false; // Allow showing the next alert
                    displayNextAlert(); // Show next alert in the queue
                });
    }

    // Inner class to represent an alert
    private static class Alert {
        String message;
        AlertType type;

        Alert(String message, AlertType type) {
            this.message = message;
            this.type = type;
        }
    }
}
