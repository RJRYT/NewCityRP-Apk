package com.newcityrp.launcher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InfoFragment extends Fragment {

    private InfoRepository infoRepository;
    private TextView serverNameTextView, descriptionTextView, createdAtTextView, ownersTextView, serverVersionTextView, linksTextView;

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
        serverNameTextView = view.findViewById(R.id.serverName);
        descriptionTextView = view.findViewById(R.id.description);
        createdAtTextView = view.findViewById(R.id.createdAt);
        ownersTextView = view.findViewById(R.id.owners);
        serverVersionTextView = view.findViewById(R.id.serverVersion);
        linksTextView = view.findViewById(R.id.links);

        // Load server info
        loadServerInfo();

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

    private void parseAndDisplayData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            // Set server name
            serverNameTextView.setText(jsonObject.getString("name"));

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
}
