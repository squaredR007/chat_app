package server ;

import com.sun.net.httpserver.HttpServer;
import controller.ChatController;
import controller.GroupController;
import controller.AuthController;
import controller.SettingsController;
import controller.UserController ;
import repository.ChatRepository;
import repository.GroupRepository;
import repository.MessageRepository;
import repository.UserRepository;
import service.ChatService;
import service.GroupService;
import service.MessageService;
import service.AuthService;
import service.SettingsService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import CLI.AdminCLI;

public class Server {

    private static final int PORT = 7600 ;

    public static void main (String[] args) throws IOException{
        //Repositories cause for phase 1 we use in-memory storage
        ChatRepository chatRepository = new ChatRepository() ;
        GroupRepository groupRepository = new GroupRepository() ;
        MessageRepository messageRepository = new MessageRepository() ;
        UserRepository userRepository = new UserRepository() ;

        //Services (business logic)
        ChatService chatService = new ChatService(chatRepository);
        GroupService groupService = new GroupService(groupRepository , chatRepository);
        MessageService messageService = new MessageService(messageRepository);
        AuthService authService = new AuthService(userRepository , chatService);
        SettingsService settingsService = new SettingsService(userRepository);

        //Server set up using thread pool
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT) , 0) ;
        server.setExecutor(Executors.newFixedThreadPool(10));

        //Registering controllers
        server.createContext("/api/chat", new ChatController(chatService, messageService));
        server.createContext("/api/group", new GroupController(groupService));
        server.createContext("/api/auth", new AuthController(authService));
        server.createContext("/api/settings", new SettingsController(settingsService));
        server.createContext("/api/user", new UserController(userRepository));

        server.start();
        CLI.AdminCLI adminCLI = new CLI.AdminCLI(userRepository, groupRepository, groupService, messageService);
        new Thread(() -> adminCLI.start()).start();
        System.out.println("Server started on port " + PORT);
    }
}