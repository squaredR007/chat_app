package server;

import com.sun.net.httpserver.HttpServer;
import controller.ChatController;
import controller.GroupController;
import controller.AuthController;
import controller.SettingsController;
import controller.UserController;
import controller.MediaController;
import repository.ChatRepository;
import repository.GroupRepository;
import repository.MessageRepository;
import repository.UserRepository;
import service.ChatService;
import service.GroupService;
import service.MessageService;
import service.AuthService;
import service.SettingsService;
import service.WebSocketHandler;
import service.Filestorageservice;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import CLI.AdminCLI;

public class Server {

    private static final int PORT = 8080;


    private static final int WEBSOCKET_PORT = 8081;

    public static void main(String[] args) throws IOException {
        //Repositories cause for phase 1 we use in-memory storage
        ChatRepository chatRepository = new ChatRepository();
        GroupRepository groupRepository = new GroupRepository();
        MessageRepository messageRepository = new MessageRepository();
        UserRepository userRepository = new UserRepository();

        // Starts the WebSocket server so it can accept connections and be
        // handed to MessageService below for live broadcasting.
        WebSocketHandler webSocketHandler = new WebSocketHandler();
        webSocketHandler.start(WEBSOCKET_PORT);

        //handles saving/reading uploaded media files (images, documents, etc.)
        Filestorageservice fileStorageService = new Filestorageservice();

        //Services (business logic)
        ChatService chatService = new ChatService(chatRepository, userRepository);
        GroupService groupService = new GroupService(groupRepository, chatRepository);
        MessageService messageService = new MessageService(messageRepository, webSocketHandler);
        AuthService authService = new AuthService(userRepository, chatService);
        SettingsService settingsService = new SettingsService(userRepository);

        //Server set up using thread pool
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        //Registering controllers
        server.createContext("/api/chat", new ChatController(chatService, messageService));
        server.createContext("/api/group", new GroupController(groupService));
        server.createContext("/api/auth", new AuthController(authService, userRepository));
        server.createContext("/api/settings", new SettingsController(settingsService));
        server.createContext("/api/user", new UserController(userRepository));
        server.createContext("/api/media", new MediaController(fileStorageService));

        server.start();
        CLI.AdminCLI adminCLI = new CLI.AdminCLI(userRepository, groupRepository, groupService, messageService);
        new Thread(() -> adminCLI.start()).start();

        System.out.println("Server started on port " + PORT);
        System.out.println("WebSocket server started on port " + WEBSOCKET_PORT);
    }
}