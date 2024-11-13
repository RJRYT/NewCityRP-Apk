package com.newcityrp.launcher;

import android.os.Bundle;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewServers);
        displayServerList();

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
    }

    public void joinServer(Server server) {
        alertManager.showAlert("Server Join: "+server.getIp(), AlertManager.AlertType.SUCCESS);
    }
}