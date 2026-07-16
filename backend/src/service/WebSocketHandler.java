package service;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSocketHandler {

    private static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private List<ClientConnection> clients = new CopyOnWriteArrayList<>();
    // One thread per connected client

    private final ExecutorService clientPool = Executors.newCachedThreadPool();

    public void start(int port) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)){
                System.out.println("WebSocket handler listening on port " + port);
                while (true) {
                    Socket socket = serverSocket.accept() ;
                    clientPool.submit(() -> handleClient(socket)) ;
                }
            } catch (IOException e) {
                System.err.println("WebSocket server failed to start: " + e.getMessage());
            }
        },"websocket-accept-thread").start();
    }

    private void handleClient (Socket socket) {
        ClientConnection client = null ;
        try {
            InputStream in = socket.getInputStream() ;
            OutputStream out = socket.getOutputStream() ;

            boolean handshakeOk = performHandshake(in , out) ;
            if (!handshakeOk) {
                socket.close();
                return;
            }
            client = new ClientConnection(socket , out) ;
            clients.add(client) ;
            System.out.println("WebSocket client connected: " + socket.getInetAddress());
            while (true) {
                Frame frame = readFrame(in) ;
                if (frame == null || frame.opcode == 0x8) {
                    break;
                }
                if (frame.opcode == 0x9) {
                    sendPong (out , frame.payload) ;
                }
            }
        } catch (IOException e) {

        } finally {
            if (client != null) {
                clients.remove(client);
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("WebSocket client disconnected");
        }
    }

    private boolean performHandshake(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        String webSocketKey = null;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("sec-websocket-key:")) {
                webSocketKey = line.substring(line.indexOf(':') + 1).trim();
            }
        }

        if (webSocketKey == null) {
            return false;
        }

        String acceptKey;
        try {
            acceptKey = generateAcceptKey(webSocketKey);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }

        String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
        return true;
    }

    private String generateAcceptKey(String clientKey) throws NoSuchAlgorithmException {
        String combined = clientKey + MAGIC_GUID;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(combined.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    // Sends a UTF-8 text frame to every connected client. Safe to call from any thread

    public void broadcast(String message) {
        byte[] frame = encodeTextFrame(message);
        for (ClientConnection client : clients) {
            try {
                synchronized (client) {
                    client.out.write(frame);
                    client.out.flush();
                }
            } catch (IOException e) {
                clients.remove(client);
            }
        }
    }

    public int getConnectedClientCount() {
        return clients.size();
    }

    private void sendPong(OutputStream out, byte[] payload) throws IOException {
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(0x8A); // FIN + Pong opcode
        frame.write(payload.length);
        frame.write(payload);
        out.write(frame.toByteArray());
        out.flush();
    }

    private byte[] encodeTextFrame(String message) {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x81); // FIN + text-frame opcode

        int len = payload.length;
        if (len <= 125) {
            out.write(len);
        } else if (len <= 65535) {
            out.write(126);
            out.write((len >> 8) & 0xFF);
            out.write(len & 0xFF);
        } else {
            out.write(127);
            for (int i = 7; i >= 0; i--) {
                out.write((int) ((len >> (8 * i)) & 0xFF));
            }
        }

        out.write(payload, 0, payload.length);
        return out.toByteArray();
    }

    private Frame readFrame(InputStream in) throws IOException {
        int firstByte = in.read();
        if (firstByte == -1)
            return null;
        int opcode = firstByte & 0x0F;

        int secondByte = in.read();
        if (secondByte == -1)
            return null;
        boolean masked = (secondByte & 0x80) != 0;
        long payloadLength = secondByte & 0x7F;

        if (payloadLength == 126) {
            int b1 = in.read();
            int b2 = in.read();
            if (b1 == -1 || b2 == -1)
                return null;
            payloadLength = (b1 << 8) | b2;
        } else if (payloadLength == 127) {
            payloadLength = 0;
            for (int i = 0; i < 8; i++) {
                int b = in.read();
                if (b == -1) return null;
                payloadLength = (payloadLength << 8) | b;
            }
        }

        byte[] maskKey = new byte[4];
        if (masked) {
            if (in.read(maskKey, 0, 4) != 4) return null;
        }

        byte[] payload = new byte[(int) payloadLength];
        int readTotal = 0;
        while (readTotal < payload.length) {
            int r = in.read(payload, readTotal, payload.length - readTotal);
            if (r == -1) return null;
            readTotal += r;
        }

        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ maskKey[i % 4]);
            }
        }

        Frame frame = new Frame();
        frame.opcode = opcode;
        frame.payload = payload;
        return frame;
    }




    private static class ClientConnection {
        final Socket socket;
        final OutputStream out;

        ClientConnection(Socket socket, OutputStream out) {
            this.socket = socket;
            this.out = out;
        }
    }

    private static class Frame {
        int opcode;
        byte[] payload;
    }

}

