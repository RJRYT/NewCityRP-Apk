package com.newcityrp.launcher;

import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ServerJoinHelper {

    private Context context;

    public ServerJoinHelper(Context context) {
        this.context = context;
    }

    // Method to handle joining the server
    public void joinServer(Server server, String nickName, String serverPass) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("nickname", nickName);
            editor.apply();

            boolean autoaim = sharedPreferences.getBoolean("autoaim", true);
            boolean timestamp = sharedPreferences.getBoolean("timestamp", false);
            boolean displayFps = sharedPreferences.getBoolean("displayfps", true);
            int fpsLimit = sharedPreferences.getInt("fpsLimit", 60); // Default to 60
            int numStrings = sharedPreferences.getInt("chatStrings", 10); // Default to 10
            boolean voice = sharedPreferences.getBoolean("voicechat", true);
            boolean fastConnect = sharedPreferences.getBoolean("fastconnect", false);
            boolean modifyMode = sharedPreferences.getBoolean("modifymode", false); // Default to false

            // Create the JSON object
            JSONObject settingsJson = new JSONObject();
            JSONObject clientJson = new JSONObject();
            JSONObject serverJson = new JSONObject();
            JSONObject launcherJson = new JSONObject();

            // Populate the server details
            serverJson.put("ip", server.getIp());
            serverJson.put("port", server.getPort());
            serverJson.put("password", serverPass);

            // Populate the launcher details
            launcherJson.put("nickname", nickName);
            launcherJson.put("autoaim", autoaim);
            launcherJson.put("timestamp", timestamp);
            launcherJson.put("displayfps", displayFps);
            launcherJson.put("fpslimit", fpsLimit);
            launcherJson.put("numstrings", numStrings);
            launcherJson.put("voice", voice);
            launcherJson.put("fastconnect", fastConnect);
            launcherJson.put("modifymode", modifyMode);

            // Combine into the main JSON structure
            clientJson.put("server", serverJson);
            settingsJson.put("client", clientJson);
            settingsJson.put("launcher", launcherJson);

            // Save JSON to SAMP/settings.json
            saveSettingsToFile(settingsJson);

            // Proceed to start the game
            //Intent intent = new Intent(context, GTASA.class);
            //context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save settings to a JSON file
    private void saveSettingsToFile(JSONObject settingsJson) throws IOException {
    // Define the SAMP directory path
    File sampDirectory = new File(context.getExternalFilesDir(null), "SAMP");

    // Ensure the SAMP directory exists
    if (!sampDirectory.exists()) {
        sampDirectory.mkdirs(); // Create the directory if it doesn't exist
    }

    // Define the settings file within the SAMP directory
    File settingsFile = new File(sampDirectory, "settings.json");

    // Write the JSON object to the file
    try (FileWriter writer = new FileWriter(settingsFile)) {
        try {
            writer.write(settingsJson.toString(4)); // Pretty print with an indent of 4 spaces
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
}
