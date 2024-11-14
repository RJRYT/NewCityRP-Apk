package com.newcityrp.launcher;

import android.content.Context;
import android.os.Build.VERSION;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.StringTokenizer;

public final class ServerQuery {
    private DatagramSocket socket = null;
    private InetAddress serverAddress = null;
    private String serverIp;
    private String serverHostAddress;
    private int port;
    private Random random;
    private boolean isInitialized;

    private LogManager logManager; // Assuming logManager is initialized elsewhere

    public ServerQuery(String ip, int port, Context context) {
        this.serverIp = "";
        this.serverHostAddress = "";
        this.port = 0;
        this.random = new Random();
        this.isInitialized = true;
        this.logManager = new LogManager(context);

        try {
            this.serverIp = ip;
            this.serverAddress = InetAddress.getByName(ip);
            this.serverHostAddress = serverAddress.getHostAddress();
        } catch (UnknownHostException e) {
            logManager.logDebug("ServerQuery: Initialization failed", e.toString());
            this.isInitialized = false;
        }

        try {
            this.socket = new DatagramSocket();
            this.socket.setSoTimeout(2000);
        } catch (SocketException e) {
            logManager.logDebug("ServerQuery: Socket creation failed", e.toString());
            this.isInitialized = false;
        }

        this.port = port;
    }

    private DatagramPacket createPacket(String command) {
        try {
            // Construct the initial byte array
            byte[] data = new byte[10 + command.length()];
            StringTokenizer tokenizer = new StringTokenizer(serverHostAddress, ".");

            // Prepare the prefix "SAMP"
            data[0] = 'S'; data[1] = 'A'; data[2] = 'M'; data[3] = 'P';

            // Add IP address bytes
            data[4] = (byte) Integer.parseInt(tokenizer.nextToken());
            data[5] = (byte) Integer.parseInt(tokenizer.nextToken());
            data[6] = (byte) Integer.parseInt(tokenizer.nextToken());
            data[7] = (byte) Integer.parseInt(tokenizer.nextToken());

            // Add port bytes
            data[8] = (byte) (port & 0xFF);
            data[9] = (byte) ((port >> 8) & 0xFF);

            // Add command in ASCII format
            byte[] commandBytes = command.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(commandBytes, 0, data, 10, commandBytes.length);

            return new DatagramPacket(data, data.length, serverAddress, port);
        } catch (Exception e) {
            logManager.logDebug("ServerQuery: Packet creation failed", e.toString());
            return null;
        }
    }

    private String receiveResponse() {
        if (socket == null) {
            logManager.logDebug("ServerQuery", "Socket is not initialized.");
            return "";
        }
        try {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);
            String response = new String(packet.getData()).trim();
            logManager.logDebug("ServerQuery: Received response", response);
            return response;
        } catch (IOException e) {
            logManager.logDebug("ServerQuery: Receive failed", e.toString());
            return "";
        }
    }

    private byte[] receiveData() {
        if (socket == null) {
            logManager.logDebug("ServerQuery", "Socket is not initialized.");
            return new byte[3072];
        }
        try {
            DatagramPacket packet = new DatagramPacket(new byte[3072], 3072);
            socket.receive(packet);
            logManager.logDebug("ServerQuery: Data received", new String(packet.getData()).trim(), StandardCharsets.US_ASCII);
            return packet.getData();
        } catch (IOException e) {
            logManager.logDebug("ServerQuery: Data reception failed", e.toString());
            return new byte[3072];
        }
    }

    private void sendPacket(DatagramPacket packet) {
        try {
            if (socket != null) {
                socket.send(packet);
                logManager.logDebug("ServerQuery: Packet sent", new String(packet.getData()).trim());
            }
        } catch (IOException e) {
            logManager.logDebug("ServerQuery: Packet sending failed", e.toString());
        }
    }

    public void closeSocket() {
        if (socket != null) {
            socket.close();
            logManager.logDebug("ServerQuery", "Socket closed.");
        }
    }

    public boolean pingServer() {
        if (!isInitialized) {
            return false;
        }

        int uniqueId = random.nextInt(8999) + 1000;
        String command = "p" + uniqueId;
        sendPacket(createPacket(command));

        try {
            String response = receiveResponse();
            //boolean success = response.startsWith(command);
            // Clean the response to remove non-ASCII characters
String cleanResponse = response.replaceAll("[^\\x00-\\x7F]", "");

// Check if the cleaned response contains the command
boolean success = cleanResponse.contains(command);
            logManager.logDebug("ServerQuery: Ping success", String.valueOf(success), "response: ", cleanResponse, "cmd: ", command);
            return success;
        } catch (Exception e) {
            logManager.logDebug("ServerQuery: Ping failed", e.toString());
            return false;
        }
    }

    public String[] getServerInfo() {
        sendPacket(createPacket("i"));
        ByteBuffer buffer = ByteBuffer.wrap(receiveData());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(11);

        String[] serverInfo = new String[6];
        try {
            serverInfo[0] = Byte.toString(buffer.get()); // server is locked or not(password enabled)
            serverInfo[1] = Short.toString(buffer.getShort()); // players online
            serverInfo[2] = Short.toString(buffer.getShort()); // max players

            // Read server name
            int nameLength = buffer.getInt();
            byte[] nameBytes = new byte[nameLength];
            buffer.get(nameBytes);
            serverInfo[3] = new String(nameBytes, "windows-1251");

            // Read server mode
            int modeLength = buffer.getInt();
            byte[] modeBytes = new byte[modeLength];
            buffer.get(modeBytes);
            serverInfo[4] = new String(modeBytes);

            // Read map name
            int mapLength = buffer.getInt();
            byte[] mapBytes = new byte[mapLength];
            buffer.get(mapBytes);
            serverInfo[5] = new String(mapBytes);

            logManager.logDebug("ServerQuery: Server info retrieved", String.join(", ", serverInfo));
        } catch (UnsupportedEncodingException e) {
            logManager.logDebug("ServerQuery: Encoding error", e.toString());
        }

        return serverInfo;
    }

    public long getPingTime() {
        int uniqueId = random.nextInt(8999) + 1000;
        String command = "p" + uniqueId;
        DatagramPacket packet = createPacket(command);
        long startTime = System.currentTimeMillis();
        sendPacket(packet);
        receiveData();
        long pingTime = System.currentTimeMillis() - startTime;
        logManager.logDebug("ServerQuery: Ping time", String.valueOf(pingTime));
        return pingTime;
    }
}