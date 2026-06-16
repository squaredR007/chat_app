package controller ;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.ChatService;
import service.MessageService;
import model.Chat;
import model.Message;
import java.io.IOException;
import java.util.List;

public class ChatController implements HttpHandler{

    private final ChatService chatService ;
    private final MessageService messageService ;

    //constructor
    public ChatController (ChatService chatService , MessageService messageService) {
        this.chatService = chatService ;
        this.messageService = messageService ;
    }

    @Override
    public void handle (HttpExchange exchange)throws IOException {
        String path = exchange.getRequestURI().getPath() ;
        String method = exchange.getRequestMethod() ;
        try {
            if (path.equals("/api/chat/create") && method.equals("POST")) {
                handleCreatePrivateChat(exchange);
            } else if (path.equals("/api/chat/list") && method.equals("GET")) {
                handleGetAllChats(exchange);
            } else if (path.equals("/api/chat/messages") && method.equals("GET")) {
                handleGetMessages(exchange);
            } else if (path.equals("/api/chat/send") && method.equals("POST")) {
                handleSendMessage(exchange);
            } else if (path.equals("/api/chat/archive") && method.equals("POST")) {
                handleArchiveChat(exchange);
            } else if (path.equals("/api/chat/pin") && method.equals("POST")) {
                handlePinChat(exchange);
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }

    //Creating a private chat

    private void handleCreatePrivateChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = body.get("chatId").getAsString();
        String user1 = body.get("user1").getAsString();
        String user2 = body.get("user2").getAsString();

        chatService.creatPrivateChat(chatId, user1, user2);

        JsonObject response = new JsonObject();
        response.addProperty("status", "chat created");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Returning all of the chats as a list

    private void handleGetAllChats(HttpExchange exchange) throws IOException {
        List<Chat> chats = chatService.getAllChats();
        HttpUtils.sendResponse(exchange, 200, chats);
    }

    //Returning all of the messages as a list

    private void handleGetMessages(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        JsonObject queryParams = HttpUtils.parseQueryString(query);
        String chatId = queryParams.get("chatId").getAsString();

        List<Message> messages = messageService.getMessages(chatId);
        HttpUtils.sendResponse(exchange, 200, messages);
    }

    //Sending message

    private void handleSendMessage(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = body.get("chatId").getAsString();
        String sender = body.get("sender").getAsString();
        String content = body.get("content").getAsString();

        Message message = messageService.sendMessage(chatId, sender, content, Message.MessageType.TEXT);

        JsonObject response = new JsonObject();
        response.addProperty("messageId", message.getId());
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Archiving a chat

    private void handleArchiveChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = body.get("chatId").getAsString();

        chatService.archiveChat(chatId);

        JsonObject response = new JsonObject();
        response.addProperty("status", "archived");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Pining a chat

    private void handlePinChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = body.get("chatId").getAsString();

        chatService.pinChat(chatId);

        JsonObject response = new JsonObject();
        response.addProperty("status", "pinned");
        HttpUtils.sendResponse(exchange, 200, response);
    }
}
