package com.newcityrp.launcher;

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
    private DownloadHelper downloadHelper;
    private SharedPreferences preferences;
    private TextView statusTextView;
    private Button updateGameButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        statusTextView = view.findViewById(R.id.statusTextView);
        updateGameButton = view.findViewById(R.id.updateGameButton);

        httpClient = new HttpClient(requireContext());
        logManager = new LogManager(requireContext());
        downloadHelper = new DownloadHelper(requireContext()); 
        preferences = requireActivity().getSharedPreferences("GameUpdatePrefs", Context.MODE_PRIVATE);

        fetchGameFileURLs();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("update_status", "checking");
        editor.apply();

        downloadHelper.checkUpdates(new Callback<Boolean>() {
            @Override
            public void onResult(Boolean updateNeeded) {
                if (updateNeeded) {
                    editor.putString("update_status", "need_to_update");
                    editor.apply();
                    updateGameStatusText();
                } else {
                    editor.putString("update_status", "ready_to_play");
                    editor.apply();
                    updateGameStatusText();
                }
            }
        });

        updateGameStatusText();

        updateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                new AlertDialog.Builder(getActivity())
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

    private void updateGameStatusText() {
        String status = preferences.getString("update_status", "checking");
        switch (status) {
            case "need_to_update":
                statusTextView.setText("Need to Update");
                statusTextView.setTextColor(R.color.colorRed);
                updateGameButton.setVisibility(View.VISIBLE);
                break;
            case "checking":
                statusTextView.setText("Checking");
                statusTextView.setTextColor(R.color.colorYellow);
                updateGameButton.setVisibility(View.GONE);
                break;
            case "ready_to_play":
                statusTextView.setText("Ready to Play");
                statusTextView.setTextColor(R.color.colorGreen);
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
                    logManager.logError("HomeFragment: error on fetchGameFileURLs", error);
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).finishActivity();
                    }
                }
        });
    }
}