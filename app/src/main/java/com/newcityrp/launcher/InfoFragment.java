package com.newcityrp.launcher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InfoFragment extends Fragment {

    private InfoRepository infoRepository;
    private TextView serverTitleTextView, descriptionTextView, createdAtTextView, ownersTextView, serverVersionTextView, linksTextView, appBuildVersionTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoRepository = new InfoRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        // Initialize TextViews
        serverTitleTextView = view.findViewById(R.id.infoServerTitle);
        descriptionTextView = view.findViewById(R.id.infoDescription);
        createdAtTextView = view.findViewById(R.id.infoCreatedAt);
        ownersTextView = view.findViewById(R.id.infoOwners);
        serverVersionTextView = view.findViewById(R.id.infoServerVersion);
        linksTextView = view.findViewById(R.id.infoLinks);
        appBuildVersionTextView = view.findViewById(R.id.infoAppBuildVersion);

        // Load server info
        loadServerInfo();

        // Set app build version
        setAppBuildVersion();

        return view;
    }

    private void loadServerInfo() {
        infoRepository.fetchServerInfo(new InfoRepository.DataCallback() {

            @Override
            public void onSuccess(JSONObject data) {
                parseAndDisplayData(data);
            }

            @Override
            public void onFailure(String error) {
                getActivity().runOnUiThread(() -> descriptionTextView.setText("Failed to load server info: " + error));
            }
        });
    }

    private void parseAndDisplayData(JSONObject jsonObject) {
        try {
             // Set server title as "Server Name [Server Version]"
            String serverName = jsonObject.getString("name");
            String serverVersion = jsonObject.getString("serverVersion");
            serverTitleTextView.setText(serverName + " [" + serverVersion + "]");

            // Set description
            descriptionTextView.setText(jsonObject.getString("description"));

            // Set created at
            createdAtTextView.setText("Created At: " + jsonObject.getString("createdAt"));

            // Parse and set owners information
            JSONArray ownersArray = jsonObject.getJSONArray("owners");
            StringBuilder ownersBuilder = new StringBuilder();
            for (int i = 0; i < ownersArray.length(); i++) {
                JSONObject owner = ownersArray.getJSONObject(i);
                ownersBuilder.append("Name: ").append(owner.getString("name"))
                        .append(" (Username: ").append(owner.getString("username")).append(")\n");
                ownersBuilder.append("GitHub: ").append(owner.getString("github")).append("\n");
                ownersBuilder.append("Website: ").append(owner.getString("website")).append("\n");
                ownersBuilder.append("Instagram: ").append(owner.getString("instagram")).append("\n\n");
            }
            ownersTextView.setText(ownersBuilder.toString());

            // Set server version
            serverVersionTextView.setText("Server Version: " + jsonObject.getString("serverVersion"));

            // Parse and set links
            JSONObject linksObject = jsonObject.getJSONObject("links");
            String linksText = "Discord: " + linksObject.getString("discord") + "\n" +
                    "Instagram: " + linksObject.getString("instagram") + "\n" +
                    "WhatsApp: " + linksObject.getString("whatsapp") + "\n" +
                    "YouTube: " + linksObject.getString("youtube") + "\n";
            linksTextView.setText(linksText);

        } catch (JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> descriptionTextView.setText("Error parsing server info"));
        }
    }

    private void setAppBuildVersion() {
        try {
            PackageManager pm = requireContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(requireContext().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            appBuildVersionTextView.setText("App Version: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            appBuildVersionTextView.setText("App Version: Unknown");
        }
    }
}
