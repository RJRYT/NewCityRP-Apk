package com.newcityrp.launcher;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.widget.EditText;
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
import android.content.SharedPreferences;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ServersFragment extends Fragment {

    private ServerListRepository serverListRepository;
    private AlertManager alertManager;
    private FavoriteManager favoriteManager;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverListRepository = new ServerListRepository(requireContext());
        alertManager = new AlertManager(requireActivity());
        favoriteManager = new FavoriteManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servers, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewServers);
        loadServerList();

        return view;
    }

    private void loadServerList() {
        serverListRepository.fetchServerList(new ServerListRepository.DataCallback() {

            @Override
            public void onSuccess(JSONObject data) {
                parseAndDisplayData(data);
            }

            @Override
            public void onFailure(String error) {
                getActivity().runOnUiThread(() -> {
                    alertManager.showAlert("Failed to load servers list.", AlertManager.AlertType.ERROR);
                });
            }
        });
    }

    private void parseAndDisplayData(JSONObject jsonResponse) {
    try {
        JSONArray filesArray = jsonResponse.getJSONArray("query");
        List<Server> serverList = new ArrayList<>();

        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject fileObject = filesArray.getJSONObject(i);

            String serverName = fileObject.getString("name");
            String serverIp = fileObject.getString("ip");
            int serverPort = fileObject.getInt("port");
            boolean serverPassword = fileObject.getBoolean("password");
            int onlinePlayers = fileObject.getInt("online");
            int maxPlayers = fileObject.getInt("maxplayers");

            Server server = new Server(serverName, serverIp, serverPort, serverPassword, onlinePlayers, maxPlayers);
            serverList.add(server);
        }

        // Ensure UI update happens on the main thread
        getActivity().runOnUiThread(() -> displayServerList(serverList));

    } catch (JSONException e) {
        e.printStackTrace();
        getActivity().runOnUiThread(() -> 
            alertManager.showAlert("Failed to parse servers list.", AlertManager.AlertType.ERROR));
        }
    }

    private void displayServerList(List<Server> serverList) {
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

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String playerNickname = sharedPreferences.getString("nickname", "");
        nicknameField.setText(playerNickname);
        
        if(!server.hasPassword()) {
            passwordField.setVisibility(View.GONE);
        }

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
            }
        });

        imgJoinServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences updatepref = requireActivity().getSharedPreferences("GameUpdatePrefs", Context.MODE_PRIVATE);
                String status = updatepref.getString("update_status", "checking");
                String NickName = nicknameField.getText().toString().trim();
                String ServerPass = passwordField.getText().toString().trim();
                if(!status.equals("ready_to_play")) {
                    alertManager.showAlert("You must update game data to play!", AlertManager.AlertType.ERROR);
                } else if(NickName.length() < 3) {
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
                server.setName(serverInfo[3]);
                server.setHasPassword(Boolean.parseBoolean(serverInfo[0]));
                
                try {
                    server.setOnlinePlayers(Integer.parseInt(serverInfo[1]));
                    server.setMaxPlayers(Integer.parseInt(serverInfo[2]));
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                adapter.updateServer(server);
            } 
        }
    }
}