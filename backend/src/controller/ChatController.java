package controller;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.ChatService;
import service.MessageService;
import model.Chat;
import model.Message;
import java.io.IOException;
import java.util.List;

public class ChatController implements HttpHandler {

    private final ChatService chatService;
    private final MessageService messageService;

    //constructor
    public ChatController(ChatService chatService, MessageService messageService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //handling CORS
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            HttpUtils.handleCors(exchange);
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
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
            } else if (path.equals("/api/chat/report") && method.equals("POST")) {
                handleReportMessage(exchange);
            } else if (path.equals("/api/chat/poll") && method.equals("GET")) {
                handlePollMessages(exchange);
            } else if (path.equals("/api/chat/edit") && method.equals("POST")) {
                handleEditMessage(exchange);
            } else if (path.equals("/api/chat/delete") && method.equals("POST")) {
                handleDeleteMessage(exchange);
            } else if (path.equals("/api/chat/unpin") && method.equals("POST")) {
                handleUnpinChat(exchange);
            } else if (path.equals("/api/chat/unarchive") && method.equals("POST")) {
                handleUnarchiveChat(exchange);
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage() != null ? e.getMessage() : "Bad request");
        }
    }

    // ── Validation helpers ──

    private String requireString(JsonObject body, String field) {
        if (!body.has(field) || body.get(field).isJsonNull()) {
            throw new RuntimeException("Missing required field: " + field);
        }
        String value = body.get(field).getAsString();
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Field cannot be empty: " + field);
        }
        return value;
    }

    private String requireQueryParam(JsonObject params, String field) {
        if (!params.has(field) || params.get(field).getAsString().isEmpty()) {
            throw new RuntimeException("Missing required query parameter: " + field);
        }
        return params.get(field).getAsString();
    }

    // ── Handlers ──

    //Creating a private chat
    private void handleCreatePrivateChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");
        String user1 = requireString(body, "user1");
        String user2 = requireString(body, "user2");

        chatService.creatPrivateChat(chatId, user1, user2);

        JsonObject response = new JsonObject();
        response.addProperty("status", "chat created");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Returns all chats

    private void handleGetAllChats(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        JsonObject queryParams = HttpUtils.parseQueryString(query);
        String filterUsername = (queryParams.has("username") && !queryParams.get("username").getAsString().isEmpty())
                ? queryParams.get("username").getAsString()
                : null;

        List<Chat> chats = chatService.getAllChats();

        com.google.gson.JsonArray result = new com.google.gson.JsonArray();

        for (Chat chat : chats) {

            if (filterUsername != null && !chatBelongsToUser(chat, filterUsername)) {
                continue;
            }

            JsonObject chatJson = new JsonObject();
            chatJson.addProperty("chatId", chat.getChatId());
            chatJson.addProperty("pinned", chat.isPinned());
            chatJson.addProperty("archived", chat.isArchived());

            // serialize messages
            com.google.gson.JsonArray messagesArray = new com.google.gson.JsonArray();
            if (chat.getMessages() != null) {
                for (Message msg : chat.getMessages()) {
                    JsonObject msgJson = new JsonObject();
                    msgJson.addProperty("id", msg.getId());
                    msgJson.addProperty("senderUsername", msg.getSenderUsername());
                    msgJson.addProperty("content", msg.getContent());
                    msgJson.addProperty("type", msg.getType().toString());
                    msgJson.addProperty("deleted", msg.isDeleted());
                    msgJson.addProperty("edited", msg.isEdited());
                    messagesArray.add(msgJson);
                }
            }
            chatJson.add("messages", messagesArray);

            // PrivateChat specific fields
            if (chat instanceof model.PrivateChat) {
                model.PrivateChat pc = (model.PrivateChat) chat;
                chatJson.addProperty("user1Username", pc.getUser1Username());
                chatJson.addProperty("user2Username", pc.getUser2Username());
            }

            // GroupChat specific fields
            if (chat instanceof model.GroupChat) {
                model.GroupChat gc = (model.GroupChat) chat;
                model.Group group = gc.getGroup();
                if (group != null) {
                    JsonObject groupJson = new JsonObject();
                    groupJson.addProperty("groupId", group.getGroupId());
                    groupJson.addProperty("groupName", group.getGroupName());
                    groupJson.addProperty("adminUsername", group.getAdminUsername());

                    com.google.gson.JsonArray membersArray = new com.google.gson.JsonArray();
                    if (group.getMembersUsernames() != null) {
                        for (String member : group.getMembersUsernames()) {
                            membersArray.add(member);
                        }
                    }
                    groupJson.add("membersUsernames", membersArray);
                    chatJson.add("group", groupJson);
                }
            }

            result.add(chatJson);
        }

        HttpUtils.sendResponse(exchange, 200, result);
    }

    // Determines whether a chat should be visible to the given username
    private boolean chatBelongsToUser(Chat chat, String username) {
        if (chat.getChatId() != null && chat.getChatId().equals("saved_" + username)) {
            return true;
        }
        if (chat instanceof model.PrivateChat) {
            model.PrivateChat pc = (model.PrivateChat) chat;
            return username.equals(pc.getUser1Username()) || username.equals(pc.getUser2Username());
        }
        if (chat instanceof model.GroupChat) {
            model.GroupChat gc = (model.GroupChat) chat;
            return gc.getGroup() != null
                    && gc.getGroup().getMembersUsernames() != null
                    && gc.getGroup().getMembersUsernames().contains(username);
        }
        return false;
    }

    //Returning all of the messages of a chat as a list
    private void handleGetMessages(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        JsonObject queryParams = HttpUtils.parseQueryString(query);
        String chatId = requireQueryParam(queryParams, "chatId");

        List<Message> messages = messageService.getMessages(chatId);
        HttpUtils.sendResponse(exchange, 200, messages);
    }

    //Sending message

    private void handleSendMessage(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");
        String sender = requireString(body, "sender");
        String content = requireString(body, "content");

        Message.MessageType type = Message.MessageType.TEXT;
        if (body.has("type") && !body.get("type").isJsonNull()) {
            String typeStr = body.get("type").getAsString();
            try {
                type = Message.MessageType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid message type: " + typeStr);
            }
        }

        Message message = messageService.sendMessage(chatId, sender, content, type);

        JsonObject response = new JsonObject();
        response.addProperty("messageId", message.getId());
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Archiving a chat
    private void handleArchiveChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");

        chatService.archiveChat(chatId);

        JsonObject response = new JsonObject();
        response.addProperty("status", "archived");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Pining a chat
    private void handlePinChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");

        chatService.pinChat(chatId);

        JsonObject response = new JsonObject();
        response.addProperty("status", "pinned");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Reporting a message (admin can view reported messages via CLI)
    private void handleReportMessage(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");
        String messageId = requireString(body, "messageId");

        messageService.reportMessage(chatId, messageId);

        JsonObject response = new JsonObject();
        response.addProperty("status", "message reported");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Polling for new messages since a given timestamp
    private void handlePollMessages(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        JsonObject queryParams = HttpUtils.parseQueryString(query);
        String chatId = requireQueryParam(queryParams, "chatId");
        String sinceStr = requireQueryParam(queryParams, "since");

        long since;
        try {
            since = Long.parseLong(sinceStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid 'since' timestamp");
        }

        List<Message> newMessages = messageService.getMessagesSince(chatId, since);
        HttpUtils.sendResponse(exchange, 200, newMessages);
    }

    // Editing a message which saves previous content for history
    private void handleEditMessage(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");
        String messageId = requireString(body, "messageId");
        String newContent = requireString(body, "newContent");

        messageService.editMessage(chatId, messageId, newContent);

        JsonObject response = new JsonObject();
        response.addProperty("status", "message edited");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Deleting a message which keeps it visible in history
    private void handleDeleteMessage(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");
        String messageId = requireString(body, "messageId");
        String requestingUsername = requireString(body, "requestingUsername");

        messageService.deleteMessage(chatId, messageId, requestingUsername);

        JsonObject response = new JsonObject();
        response.addProperty("status", "message deleted");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Unpinning a chat
    private void handleUnpinChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");

        chatService.unpinChat(chatId);

        JsonObject response = new JsonObject();
        response.addProperty("status", "unpinned");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Unarchiving a chat
    private void handleUnarchiveChat(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String chatId = requireString(body, "chatId");

        chatService.unarchiveChat(chatId);

        JsonObject response = new JsonObject();
        response.addProperty("status", "unarchived");
        HttpUtils.sendResponse(exchange, 200, response);
    }
}