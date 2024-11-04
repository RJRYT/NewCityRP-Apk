package com.newcityrp.launcher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import main.java.com.newcityrp.launcher.HttpClient;
import main.java.com.newcityrp.launcher.InfoRepository;

import org.json.JSONObject;

public class InfoFragment extends Fragment {

    private InfoRepository infoRepository;
    private TextView infoTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoRepository = new InfoRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        infoTextView = view.findViewById(R.id.infoTextView);
        loadServerInfo();
        return view;
    }

    private void loadServerInfo() {
        infoRepository.fetchServerInfo(new HttpClient.DataCallback() {
            @Override
            public void onSuccess(JSONObject data) {
                getActivity().runOnUiThread(() -> infoTextView.setText(data.toString()));
            }
            @Override
            public void onFailure(Exception e) {
                getActivity().runOnUiThread(() -> infoTextView.setText("Failed to load server info"));
            }
        });
    }
}
