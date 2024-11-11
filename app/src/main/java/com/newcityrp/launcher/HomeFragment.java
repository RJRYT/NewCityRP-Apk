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
    private SharedPreferences preferences, apppref;
    private TextView statusTextView;
    private Button updateGameButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        httpClient = new HttpClient(requireContext());
        logManager = new LogManager(requireContext());
        downloadHelper = new DownloadHelper(requireContext()); 
        preferences = requireActivity().getSharedPreferences("GameUpdatePrefs", Context.MODE_PRIVATE);
        apppref = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);

        fetchGameFileURLs();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("update_status", "checking");
        editor.apply();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        statusTextView = view.findViewById(R.id.statusTextView);
        updateGameButton = view.findViewById(R.id.updateGameButton);
        SharedPreferences.Editor editor = preferences.edit();
        if(isUpdateAvaliable()) {
            editor.putString("update_status", "need_to_update");
            editor.apply();
        } else {
            editor.putString("update_status", "ready_to_play");
            editor.apply();
        }
        updateGameStatusText();
        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGameStatusText();
    }

    updateGameButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Show confirmation dialog
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to update the game files?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, GameFileUpdateActivity.class);
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

    private void updateGameStatusText() {
        String status = preferences.getString("update_status", "checking");
        switch (status) {
            case "need_to_update":
                statusTextView.setText("Need to Update");
                statusTextView.setTextColor(Color.RED);
                updateGameButton.setVisibility(View.VISIBLE);
                break;
            case "checking":
                statusTextView.setText("Checking");
                statusTextView.setTextColor(Color.YELLOW);
                updateGameButton.setVisibility(View.GONE);
                break;
            case "ready_to_play":
                statusTextView.setText("Ready to Play");
                statusTextView.setTextColor(Color.GREEN);
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
                    finish();
                }
        });
    }

    private boolean isUpdateAvaliable() {
        String chosenGameType = apppref.getString("gameType", "full");
        String dataUrl = chosenGameType.equals("lite") ? preferences.getString("data_lite_url", "") : preferences.getString("data_full_url", "");
        String sampUrl = preferences.getString("data_samp_url", "");

        Boolean dataStatus = downloadHelper.checkFilesFromServerWithLocalFiles(dataUrl);
        Boolean sampStatus = downloadHelper.checkFilesFromServerWithLocalFiles(sampUrl);

        if (dataUrl.isEmpty()) return true;
        if (sampUrl.isEmpty()) return true;
        return !dataStatus && !sampStatus;
    }
}