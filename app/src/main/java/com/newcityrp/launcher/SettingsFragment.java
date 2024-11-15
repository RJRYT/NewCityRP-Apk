package com.newcityrp.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.app.AlertDialog;
import java.io.File;
import com.google.common.primitives.Ints;

public class SettingsFragment extends Fragment {

    private SharedPreferences preferences;
    private UtilManager utilManager;

    private final int[] fpsValues = {30, 60, 90};
    private final int[] chatStringValues = {5, 10, 15};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        preferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        utilManager = new UtilManager(requireActivity());

        // Set up toggles
        setupToggle(view, R.id.toggleAutoaim, "autoaim", true);
        setupToggle(view, R.id.toggleTimestamp, "timestamp", false);
        setupToggle(view, R.id.toggleDisplayFPS, "displayfps", true);
        setupToggle(view, R.id.toggleVoiceChat, "voicechat", true);
        setupToggle(view, R.id.toggleFastConnect, "fastconnect", false);
        
        // FPS Limit SeekBar
        setupSeekBar(view, R.id.seekBarFpsLimit, R.id.seekBarFpsLimitValue, "fpsLimit", fpsValues);
        // Chat Strings SeekBar
        setupSeekBar(view, R.id.seekBarChatStrings, R.id.seekBarChatStringsValue, "chatStrings", chatStringValues);

        // Game Type display and delete button
        setupGameTypeDisplay(view);

        // Initialize the delete button
        Button deleteButton = view.findViewById(R.id.buttonDeleteFiles);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        return view;
    }

    private void setupToggle(View view, int toggleId, String key, Boolean defValue) {
        Switch toggle = view.findViewById(toggleId);
        toggle.setChecked(preferences.getBoolean(key, defValue));
        toggle.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(key, isChecked).apply());
    }

    private void setupSeekBar(View view, int seekBarId, int seekBarTextId, String key, int[] values) {
        SeekBar seekBar = view.findViewById(seekBarId);
        TextView seekBarText = view.findViewById(seekBarTextId);
        int savedValue = preferences.getInt(key, values[0]);
        seekBar.setProgress(Ints.indexOf(values, savedValue));
        seekBarText.setText(String.valueOf(savedValue));
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preferences.edit().putInt(key, values[progress]).apply();
                seekBarText.setText(String.valueOf(values[progress]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupGameTypeDisplay(View view) {
        TextView gameTypeTextView = view.findViewById(R.id.textGameType);
        Button deleteButton = view.findViewById(R.id.buttonDeleteFiles);

        String gameType = preferences.getString("gameType", "---");
        gameTypeTextView.setText("Game Type: " + gameType);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog)
            .setTitle("Delete Game Files")
            .setMessage("Are you sure you want to delete the game files? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteGameFiles())
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show();
    }

    private void deleteGameFiles() {
        File gameFilesDir = new File(requireContext().getExternalFilesDir(null), "");
        
        if (deleteDirectory(gameFilesDir)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("gameType"); 
            editor.apply();
            showToast("Game files deleted successfully.");
            utilManager.launchMainActivityFreshly(requireActivity());
        } else {
            // Show an error message
            showToast("Failed to delete game files.");
        }
    }

    private boolean deleteDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDirectory(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}