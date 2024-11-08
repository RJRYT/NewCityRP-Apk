package com.newcityrp.launcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView serverTitleTextView, descriptionTextView, createdAtTextView, ownersTextView, serverVersionTextView, appBuildVersionTextView;
    private LinearLayout ownersLayout, serverLinksLayout;

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
        ownersTextView = view.findViewById(R.id.infoOwnersTitle);
        serverVersionTextView = view.findViewById(R.id.infoServerVersion);
        appBuildVersionTextView = view.findViewById(R.id.infoAppBuildVersion);

        // Initialize LinearLayout
        ownersLayout = view.findViewById(R.id.infoOwnersContainer);
        serverLinksLayout = view.findViewById(R.id.infoLinksContainer);

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
                getActivity().runOnUiThread(() -> descriptionTextView.setText("Failed to load server info."));
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
            ownersTextView.setText("Owners: ");
            ownersLayout.removeAllViews(); // Clear any previous data
            for (int i = 0; i < ownersArray.length(); i++) {
                JSONObject owner = ownersArray.getJSONObject(i);
                
                 // Create a horizontal LinearLayout for each owner
                LinearLayout ownerLayout = new LinearLayout(getContext());
                ownerLayout.setOrientation(LinearLayout.HORIZONTAL);
                ownerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                ownerLayout.setPadding(0, 16, 0, 16); // Add spacing between each owner entry
                
                // Create a new TextView for the owner's name and username
                TextView ownerNameTextView = new TextView(getContext());
                ownerNameTextView.setText("Name: " + owner.getString("name") + 
                                        " (" + owner.getString("username") + ") :-");
                ownerNameTextView.setTextSize(16);
                ownerLayout.addView(ownerNameTextView);

                // Create clickable icons for each social media link
                createSocialIcon("GitHub", owner.getString("github"), R.drawable.ic_github_white, ownerLayout);
                createSocialIcon("Website", owner.getString("website"), R.drawable.ic_website_white, ownerLayout);
                createSocialIcon("Instagram", owner.getString("instagram"), R.drawable.ic_instagram_white, ownerLayout);

                // Add the owner layout to the parent ownersLayout
                ownersLayout.addView(ownerLayout);
            }

            // Set server version
            serverVersionTextView.setText("Server Version: " + jsonObject.getString("serverVersion"));

            // Parse and set links
            JSONObject linksObject = jsonObject.getJSONObject("links");
            TextView infoLinksTitle = new TextView(getContext());
            infoLinksTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            infoLinksTitle.setTextSize(18);
            infoLinksTitle.setPadding(0, 16, 0, 0);  // Top padding to match marginTop
            infoLinksTitle.setText("Contact Us:");  // Set initial text
            infoLinksTitle.setTypeface(null, Typeface.BOLD);  // Set text style to bold
            serverLinksLayout.removeAllViews(); // Clear previous links
            serverLinksLayout.addView(infoLinksTitle);
            createSocialIcon("Discord", linksObject.getString("discord"), R.drawable.ic_discord_white, serverLinksLayout);
            createSocialIcon("Instagram", linksObject.getString("instagram"), R.drawable.ic_instagram_white, serverLinksLayout);
            createSocialIcon("WhatsApp", linksObject.getString("whatsapp"), R.drawable.ic_whatsapp_white, serverLinksLayout);
            createSocialIcon("YouTube", linksObject.getString("youtube"), R.drawable.ic_youtube_white, serverLinksLayout);

        } catch (JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> descriptionTextView.setText("Error parsing server info"));
        }
    }

    private void createSocialIcon(String platform, String url, int iconRes, ViewGroup parentLayout) {
        ImageView iconView = new ImageView(getContext());
        iconView.setImageResource(iconRes);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(70, 70)); // Adjust size as needed
        iconView.setPadding(8, 8, 8, 8);
        iconView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
        parentLayout.addView(iconView);
    }

    private void setAppBuildVersion() {
        try {
            PackageManager pm = requireContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(requireContext().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            appBuildVersionTextView.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            appBuildVersionTextView.setText("");
        }
    }
}
