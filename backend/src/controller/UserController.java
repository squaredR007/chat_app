package controller;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import model.User;
import repository.UserRepository;
import java.io.IOException;


public class UserController implements HttpHandler {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // handle CORS preflight
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            HttpUtils.handleCors(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/user/block") && method.equals("POST")) {
                handleBlockUser(exchange);
            } else if (path.equals("/api/user/unblock") && method.equals("POST")) {
                handleUnblockUser(exchange);
            } else if (path.equals("/api/user/addContact") && method.equals("POST")) {
                handleAddContact(exchange);
            } else if (path.equals("/api/user/info") && method.equals("GET")) {
                handleGetUserInfo(exchange);
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }

    //Checking whether a required JSON field is empty or null
    private String requireString(JsonObject body , String field) {
        if (!body.has(field) || body.get(field).isJsonNull()) {
            throw new RuntimeException("Missing required field: " + field) ;
        }
        String value = body.get(field).getAsString() ;
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Field cannot be empty: " + field) ;
        }
        return value ;
    }

    // blocks a user
    private void handleBlockUser(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String userId = requireString(body , "userId");
        String targetUserId = requireString(body ,"targetUserId");

        User user = userRepository.getByUserId(userId);
        if (user == null) throw new RuntimeException("User not found");
        user.blockUser(targetUserId);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // unblocks a user
    private void handleUnblockUser(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String userId = requireString(body , "userId");
        String targetUserId = requireString(body , "targetUserId");

        User user = userRepository.getByUserId(userId);
        if (user == null) throw new RuntimeException("User not found");
        user.unblockUser(targetUserId);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // adds a contact
    private void handleAddContact(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String userId = requireString(body , "userId");
        String contactUserId = requireString(body , "contactUserId");

        User user = userRepository.getByUserId(userId);
        if (user == null) throw new RuntimeException("User not found");
        user.addContact(contactUserId);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Returns basic public profile info for a user
    private void handleGetUserInfo(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        JsonObject params = HttpUtils.parseQueryString(query);
        if (!params.has("userId") || params.get("userId").getAsString().isEmpty()) {
            HttpUtils.sendError(exchange , 400 , "Missing required query parameter: userId");
            return;
        }
        String userId = params.get("userId").getAsString();

        User user = userRepository.getByUserId(userId);
        if (user == null) {
            HttpUtils.sendError(exchange, 404, "User not found");
            return;
        }

        //Response
        JsonObject response = new JsonObject();
        response.addProperty("username", user.getUsername());
        response.addProperty("displayName", user.getDisplayName() != null ? user.getDisplayName() : "");
        response.addProperty("biography", user.getBiography() != null ? user.getBiography() : "");
        response.addProperty("number", user.getNumber() != null ? user.getNumber() : "");
        response.addProperty("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "");
        response.addProperty("lastSeen", user.getLastSeen());

        HttpUtils.sendResponse(exchange, 200, response);
    }
}
