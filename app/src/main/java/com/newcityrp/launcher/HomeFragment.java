package com.newcityrp.launcher;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class HomeFragment extends Fragment {

    private HttpClient httpClient;
    private LogManager logManager;
    private AlertManager alertManager;
    private DownloadHelper downloadHelper;
    private SharedPreferences preferences;
    private TextView statusTextView;
    private Button updateGameButton;
    private GradientDrawable statustextbackground;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        statusTextView = view.findViewById(R.id.statusTextView);
        updateGameButton = view.findViewById(R.id.updateGameButton);
        statustextbackground = (GradientDrawable) statusTextView.getBackground();
        
        httpClient = new HttpClient(requireContext());
        logManager = new LogManager(requireContext());
        alertManager = new AlertManager(requireActivity());
        downloadHelper = new DownloadHelper(requireContext()); 
        preferences = requireActivity().getSharedPreferences("GameUpdatePrefs", Context.MODE_PRIVATE);

        fetchGameFileURLs();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("update_status", "checking");
        editor.apply();

    downloadHelper.checkUpdates(
        new DownloadHelper.FileCheckCallback<Boolean>() {
          @Override
          public void onResult(Boolean updateNeeded) {
            requireActivity()
                .runOnUiThread(
                    new Runnable() {
                      @Override
                      public void run() {
                        if (updateNeeded) {
                          saveGameStatus("need_to_update");
                        } else {
                          saveGameStatus("ready_to_play");
                        }
                      }
                    });
          }
        });

        updateGameStatusText();

        updateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                    .setTitle("Confirm Update")
                    .setMessage("Are you sure you want to update the game files?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), GameFileUpdateActivity.class);
                            startActivity(intent);
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).finishActivity();
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Close the dialog
                            dialog.dismiss();
                        }
                    })
                    .show();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGameStatusText();
    }
    
    private void saveGameStatus(String status) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("update_status", status);
        editor.apply();
    	updateGameStatusText();
    }

    private void updateGameStatusText() {
        String status = preferences.getString("update_status", "checking");
        switch (status) {
            
            case "need_to_update":
                statusTextView.setText("Need to Update");
                statusTextView.setTextColor(Color.RED);
                statustextbackground.setStroke(2, Color.RED);
                updateGameButton.setVisibility(View.VISIBLE);
                break;
            case "checking":
                statusTextView.setText("Checking");
                statusTextView.setTextColor(Color.YELLOW);
                statustextbackground.setStroke(2, Color.YELLOW);
                updateGameButton.setVisibility(View.GONE);
                break;
            case "ready_to_play":
                statusTextView.setText("Ready to Play");
                statusTextView.setTextColor(Color.GREEN);
                statustextbackground.setStroke(2, Color.GREEN);
                updateGameButton.setVisibility(View.GONE);
                break;
        }
    }

    private void fetchGameFileURLs() {
        httpClient.fetchData(
            "mobile/update.json",
            new HttpClient.DataCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        String fullUrl = data.getString("data_full_url");
                        String liteUrl = data.getString("data_lite_url");
                        String sampUrl = data.getString("data_samp_url");

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("data_full_url", fullUrl);
                        editor.putString("data_lite_url", liteUrl);
                        editor.putString("data_samp_url", sampUrl);
                        editor.apply();
                    } catch (JSONException err) {
                        logManager.logError(err);
                    }
                }

                @Override
                public void onFailure(String error) {
                    getActivity().runOnUiThread(() -> {
                        logManager.logError("HomeFragment: error on fetchGameFileURLs", error);
                        alertManager.showAlert("Network error. check your internet connection.", AlertManager.AlertType.ERROR);
                    });
                }
        });
    }
}