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
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ServerAdapter adapter = new ServerAdapter(serverList, getContext());
        recyclerView.setAdapter(adapter);
        
        // Refresh server data asynchronously
        for (Server server : serverList) {
            new FetchServerDetailsTask(server, adapter).execute();
        }
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
            holder.tvServerName.setText(server.getName().trim());
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

        public void updateServer(Server updatedServer) {
            int index = serverList.indexOf(updatedServer);
            if (index >= 0) {
                serverList.set(index, updatedServer);
                notifyItemChanged(index);
            }
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
        
        if(!server.hasPassword()) {
            passwordField.setVisibility(View.GONE);
        }

        tvServerNameDetail.setText("Server Name: " + server.getName().trim());
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
                String NickName = nicknameField.getText().toString().trim();
                String ServerPass = passwordField.getText().toString().trim();
                if(NickName.length() < 3) {
                    alertManager.showAlert("You must enter a nickname!", AlertManager.AlertType.ERROR);
                } else if(server.hasPassword() && ServerPass.length() < 1) {
                    alertManager.showAlert("You must enter the server password!", AlertManager.AlertType.ERROR);
                } else {
                    alertManager.showAlert("Joining server "+server.getName(), AlertManager.AlertType.INFO);
                    ServerJoinHelper joinHelper = new ServerJoinHelper(requireContext());
                    joinHelper.joinServer(server, NickName, ServerPass);
                    dialog.dismiss();
                }
            }
        });
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

    private class FetchServerDetailsTask extends AsyncTask<Void, Void, String[]> {
        private final Server server;
        private final ServerAdapter adapter;

        public FetchServerDetailsTask(Server server, ServerAdapter adapter) {
            this.server = server;
            this.adapter = adapter;
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            
            ServerQuery serverQuery = new ServerQuery(server.getIp(), server.getPort(), requireContext());
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
                // Check if name, online players, or max players are different before updating
                String newName = serverInfo[3];
                boolean hasPassword = Boolean.parseBoolean(serverInfo[0]);
                int newOnlinePlayers = 0;
                int newMaxPlayers = 0;

                try {
                    newOnlinePlayers = Integer.parseInt(serverInfo[1]);
                    newMaxPlayers = Integer.parseInt(serverInfo[2]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                // Only update if any of the details have changed
                boolean shouldUpdate = false;

                if (!server.getName().equals(newName)) {
                    server.setName(newName);
                    shouldUpdate = true;
                }

                if (server.getOnlinePlayers() != newOnlinePlayers) {
                    server.setOnlinePlayers(newOnlinePlayers);
                    shouldUpdate = true;
                }

                if (server.getMaxPlayers() != newMaxPlayers) {
                    server.setMaxPlayers(newMaxPlayers);
                    shouldUpdate = true;
                }

                if (server.hasPassword() != hasPassword) {
                    server.setHasPassword(hasPassword);
                    shouldUpdate = true;
                }

                // If any data has changed, update the server details
                if (shouldUpdate) {
                    favoriteManager.updateFavoriteServerDetails(server);  // Update in favorites list
                    adapter.updateServer(server);  // Update the UI
                }
            }
        }
    }
}