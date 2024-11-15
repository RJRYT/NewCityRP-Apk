package com.newcityrp.launcher;

import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.android.material.button.MaterialButton;
import java.util.regex.Pattern;

public class FavoriteFragment extends Fragment {

    private ServerListRepository serverListRepository;
    private AlertManager alertManager;
    private LogManager logManager;
    private FavoriteManager favoriteManager;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverListRepository = new ServerListRepository(requireContext());
        alertManager = new AlertManager(requireActivity());
        favoriteManager = new FavoriteManager(requireContext());
        logManager = new LogManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewServers);
        displayServerList();
        
        MaterialButton addNewServerButton = view.findViewById(R.id.addNewServerButton);
        addNewServerButton.setOnClickListener(v -> showAddServerDialog());

        return view;
    }
    
    @Override
public void onResume() {
    super.onResume();
    // Reload favorite servers here
    displayServerList();
    }

    private void displayServerList() {
        
        List<Server> serverList = favoriteManager.getFavoriteServersAsObjects();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ServerAdapter adapter = new ServerAdapter(serverList, getContext());
        recyclerView.setAdapter(adapter);
    }
    
    public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
        private final List<Server> serverList;
        private final Context context;

        public ServerAdapter(List<Server> serverList, Context context) {
            this.serverList = serverList;
            this.context = context;
        }

        @NonNull
        @Override
        public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_server, parent, false);
            return new ServerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
            Server server = serverList.get(position);
            holder.tvServerName.setText(server.getName());
            holder.tvServerInfo.setText(server.getIp() + ":" + server.getPort() + " | " +
                    server.getOnlinePlayers() + "/" + server.getMaxPlayers());

            holder.imgLockStatus.setImageResource(server.hasPassword() ? 
                R.drawable.ic_lock_red : R.drawable.ic_lock_open_green);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showServerDetailsDialog(server);
                }
            });
        }

        @Override
        public int getItemCount() {
            return serverList.size();
        }

        class ServerViewHolder extends RecyclerView.ViewHolder {
            TextView tvServerName, tvServerInfo;
            ImageView imgLockStatus;

            public ServerViewHolder(View itemView) {
                super(itemView);
                tvServerName = itemView.findViewById(R.id.tvServerName);
                tvServerInfo = itemView.findViewById(R.id.tvServerInfo);
                imgLockStatus = itemView.findViewById(R.id.imgLockStatus);
            }
        }
    }

    public void showServerDetailsDialog(Server server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_server_details, null);
        builder.setView(dialogView);

        TextView tvServerNameDetail = dialogView.findViewById(R.id.tvServerNameDetail);
        TextView tvServerIPPortDetail = dialogView.findViewById(R.id.tvServerIPPortDetail);
        TextView tvPlayerCountDetail = dialogView.findViewById(R.id.tvPlayerCountDetail);
        ImageView imgJoinServer = dialogView.findViewById(R.id.imgJoinServer);
        ImageView imgFavoriteServer = dialogView.findViewById(R.id.imgFavoriteServer);
        EditText nicknameField = dialogView.findViewById(R.id.nicknameField);
        EditText passwordField = dialogView.findViewById(R.id.passwordField);

        tvServerNameDetail.setText("Server Name: " + server.getName());
        tvServerIPPortDetail.setText("Server Ip: " + server.getIp() + ":" + server.getPort());
        tvPlayerCountDetail.setText("Players: " + server.getOnlinePlayers() + "/" + server.getMaxPlayers());

        // Set favorite icon
        if (favoriteManager.isServerFavorite(server)) {
            imgFavoriteServer.setImageResource(R.drawable.ic_heart_red);
        } else {
            imgFavoriteServer.setImageResource(R.drawable.ic_heart_white);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
        
        
        imgFavoriteServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(favoriteManager.isServerFavorite(server)) {
                    imgFavoriteServer.setImageResource(R.drawable.ic_heart_white);
                    favoriteManager.removeServerFromFavorites(server);
                } else {
                    imgFavoriteServer.setImageResource(R.drawable.ic_heart_red);
                    favoriteManager.addServerToFavorites(server);
                }
                    displayServerList();
            }
        });

        imgJoinServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinServer(server);
                dialog.dismiss();
            }
        });
        
        new FetchServerDetailsTask().execute(server.getIp(), server.getPort());

    }

    public void joinServer(Server server) {
        alertManager.showAlert("Server Join: "+server.getIp(), AlertManager.AlertType.SUCCESS);
        //Intent intent = new Intent(requireContext(), GTASA.class);
        //startActivity(intent);
        //requireActivity().finish();
    }
    
        // Show a dialog for user to enter server IP and port
    private void showAddServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        builder.setTitle("Add Server");

        // Linear layout for input fields
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Input for IP and port
        final EditText ipInput = new EditText(getContext());
        ipInput.setHint("Server IP:Port (e.g., 127.0.0.1:7777 or localhost)");
        ipInput.setInputType(InputType.TYPE_CLASS_TEXT);
        ipInput.setHintTextColor(Color.WHITE);
        ipInput.setHighlightColor(Color.GREEN);
        ipInput.setTextColor(Color.WHITE);
        layout.addView(ipInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String ipAddress = ipInput.getText().toString().trim();
            
            // Validate IP and port input
            if (isValidIpPort(ipAddress)) {
                    String[] parts = ipAddress.split(":");
                    String ip = parts[0];
                    int port = (parts.length > 1) ? Integer.parseInt(parts[1]) : 7777; // Default port 7777 if missing

                addServerToFavorites(ip, port);
            } else {
                Toast.makeText(getContext(), "Invalid IP or port format!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Validate IP and port based on regex patterns
    private boolean isValidIpPort(String valueOf) {
    // Regex to match IP or domain with optional port
    Pattern compile = Pattern.compile("^(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}|localhost|(([0-9]{1,3}\\.){3})[0-9]{1,3}):[0-9]{1,5}$");
    // Regex to match IP or domain without port
    Pattern compile2 = Pattern.compile("^(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}|localhost|(([0-9]{1,3}\\.){3})[0-9]{1,3})$");

    // Check if the input matches either regex
    if (compile.matcher(valueOf).matches() || compile2.matcher(valueOf).matches()) {
        int i = 7777;
        String str = ":";
        if (valueOf.contains(str)) {
            String[] split = valueOf.split(str);
            String str2 = split[0];
            i = Integer.parseInt(split[1]);  // Parse port if provided
            valueOf = str2;  // Update the valueOf with the IP
        }
        return true;  // Valid input
    }
    return false;  // Invalid input
}

    // Add the server to favorites
    private void addServerToFavorites(String ipAddress, int port) {
        Server server = new Server("loading", ipAddress, port, false, 0, 100);
        // Assuming FavoriteManager handles adding servers to a list or database
        if (!favoriteManager.isServerFavorite(server)) {
            favoriteManager.addServerToFavorites(server);
            Toast.makeText(getContext(), "Server added to favorites!", Toast.LENGTH_SHORT).show();
            displayServerList();
        } else {
            Toast.makeText(getContext(), "Server already in favorites!", Toast.LENGTH_SHORT).show();
        }
    }

    
    
    
    private class FetchServerDetailsTask extends AsyncTask<Object, Void, String[]> {
    @Override
    protected String[] doInBackground(Object... params) {
        String serverIp = (String) params[0];
        int serverPort = (int) params[1];

        ServerQuery serverQuery = new ServerQuery(serverIp, serverPort, requireContext());
        String[] serverInfo = null;

        try {
            if (serverQuery.pingServer()) {
                serverInfo = serverQuery.getServerInfo();
            }
        } finally {
            serverQuery.closeSocket();
        }
        
        return serverInfo;
    }

    @Override
    protected void onPostExecute(String[] serverInfo) {
            
        if (serverInfo != null) {
            // Display the server info in your dialog or UI
            // For example:
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),  R.style.CustomAlertDialog);
            builder.setTitle("Server Details")
                    .setMessage("Server Name: " + serverInfo[3] + "\n"
                            + "Players: " + serverInfo[1] + "/" + serverInfo[2] + "\n"
                            + "Game Mode: " + serverInfo[4] + "\n"
                            + "Language: " + serverInfo[5])
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            Toast.makeText(getActivity(), "Failed to retrieve server info.", Toast.LENGTH_SHORT).show();
        }
    }
}
}


