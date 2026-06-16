package service;

import java.io.* ;
import java.net.* ;
import java.util.ArrayList ;
import java.util.List ;

public class WebSocketHandler {

    private List<Socket> connectedClients = new ArrayList<>() ;

    public void start(int port) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("WebSocket handler listening on port " + port);
                while (true) {
                    Socket client = serverSocket.accept();
                    connectedClients.add(client);
                    System.out.println("New client connected: " + client.getInetAddress());
                    //handshake and message handling will be left for second phase of the project
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void broadcast(String message) {
        //full websocket protocol handling will be added in phase 2 and after that this method can get completed
    }
}
