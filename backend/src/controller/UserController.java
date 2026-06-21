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
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }

    // blocks a user
    private void handleBlockUser(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String userId = body.get("userId").getAsString();
        String targetUserId = body.get("targetUserId").getAsString();

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
        String userId = body.get("userId").getAsString();
        String targetUserId = body.get("targetUserId").getAsString();

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
        String userId = body.get("userId").getAsString();
        String contactUserId = body.get("contactUserId").getAsString();

        User user = userRepository.getByUserId(userId);
        if (user == null) throw new RuntimeException("User not found");
        user.addContact(contactUserId);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        HttpUtils.sendResponse(exchange, 200, response);
    }
}