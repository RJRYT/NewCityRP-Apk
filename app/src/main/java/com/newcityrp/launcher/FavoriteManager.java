package com.newcityrp.launcher;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class FavoriteManager {
    private static final String FAVORITES_PREF = "favorites_pref";
    private static final String FAVORITES_KEY = "favorite_servers";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public FavoriteManager(Context context) {
        sharedPreferences = context.getSharedPreferences(FAVORITES_PREF, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Add a server to favorites, but only if it's not already in the list (based on ip:port)
    public void addServerToFavorites(Server server) {
        Set<String> favorites = getFavoriteServers();
        
        // Check if the server is already in favorites using ip:port comparison
        for (String serverString : favorites) {
            Server existingServer = deserializeServer(serverString);
            if (existingServer.getIp().equals(server.getIp()) && existingServer.getPort() == server.getPort()) {
                // Server is already in favorites, don't add it again
                return;
            }
        }

        // If not found, add it to the favorites list
        favorites.add(serializeServer(server));
        saveFavorites(favorites);
    }

    // Remove a server from favorites
    public void removeServerFromFavorites(Server server) {
        Set<String> favorites = getFavoriteServers();
        favorites.remove(serializeServer(server));
        saveFavorites(favorites);
    }

    // Check if a server is in favorites based on ip:port comparison
    public boolean isServerFavorite(Server server) {
        for (String serverString : getFavoriteServers()) {
            Server existingServer = deserializeServer(serverString);
            if (existingServer.getIp().equals(server.getIp()) && existingServer.getPort() == server.getPort()) {
                return true; // Server is in favorites
            }
        }
        return false; // Server is not in favorites
    }

    // Get all favorite servers as Server objects
    public List<Server> getFavoriteServersAsObjects() {
        Set<String> favorites = getFavoriteServers();
        List<Server> serverList = new ArrayList<>();
        for (String serverString : favorites) {
            serverList.add(deserializeServer(serverString));
        }
        return serverList;
    }

    // Save favorite servers to SharedPreferences
    private void saveFavorites(Set<String> favorites) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(FAVORITES_KEY, favorites);
        editor.apply();
    }

    // Get favorite servers as Strings
    private Set<String> getFavoriteServers() {
        return sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>());
    }

    // Convert Server object to String (JSON)
    private String serializeServer(Server server) {
        return gson.toJson(server);
    }

    // Convert String (JSON) to Server object
    private Server deserializeServer(String serverString) {
        return gson.fromJson(serverString, Server.class);
    }
}
