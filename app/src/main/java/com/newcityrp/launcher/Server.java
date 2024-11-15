package com.newcityrp.launcher;

public class Server {
    private String name;
    private String ip;
    private int port;
    private boolean hasPassword;
    private int onlinePlayers;
    private int maxPlayers;

    public Server(String name, String ip, int port, boolean hasPassword, int onlinePlayers, int maxPlayers) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.hasPassword = hasPassword;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    // Getters
    public String getName() { return name; }
    public String getIp() { return ip; }
    public int getPort() { return port; }
    public boolean hasPassword() { return hasPassword; }
    public int getOnlinePlayers() { return onlinePlayers; }
    public int getMaxPlayers() { return maxPlayers; }

    // Setters
    public void setName(String name) {this.name = name;}
    public void setHasPassword(boolean password) {this.hasPassword = password;}
    public void setOnlinePlayers(int onlineplayer) {this.onlinePlayers = onlineplayer;}
    public void setMaxPlayers(int maxplayer) {this.maxPlayers = maxplayer;}
}