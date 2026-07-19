package controller;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AuthService;
import java.io.IOException;
import model.User ;
import repository.UserRepository ;

public class AuthController implements HttpHandler {

    private final AuthService authService;//access to the service
    private final UserRepository userRepository ;

    //constructor
    public AuthController(AuthService authService , UserRepository userRepository){
        this.authService=authService;
        this.userRepository = userRepository ;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //handle CORS
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            HttpUtils.handleCors(exchange);
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/auth/signup") && method.equals("POST")) {
                handleSignup(exchange);
            } else if (path.equals("/api/auth/login") && method.equals("POST")) {
                handleLogin(exchange);
            } else if (path.equals("/api/auth/blockUser") && method.equals("POST")) {
                handleBlockUser(exchange);
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage() != null ? e.getMessage() : "Bad Request");
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

    //handle user signup
    private void handleSignup(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String username = requireString(body , "username");
        String password = requireString(body , "password");
        String number = requireString(body , "number");

        boolean result = authService.signup(username, password, number);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);
        if (result) {
            User user = userRepository.getByUsername(username);
            if (user != null) {
                response.addProperty("userId", user.getUsername());
                response.addProperty("username", user.getUsername());
                response.addProperty("displayName", user.getDisplayName() != null ? user.getDisplayName() : username);
                response.addProperty("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "");
                response.addProperty("darkMode", user.isDarkMode());
                response.addProperty("background", user.getBackground() != null ? user.getBackground() : "");
            }
        } else {
            response.addProperty("error" , "Signup failed. Username/number may already be taken, or the password may be invalid");
        }

        HttpUtils.sendResponse(exchange, 200, response);
    }

    //handle user login
    private void handleLogin(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String username = requireString(body , "username");
        String password = requireString(body , "password");

        boolean result = authService.login(username, password);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        if (result) {

            User user = userRepository.getByUsername(username);
            if (user != null) {
                response.addProperty("userId", user.getUsername());
                response.addProperty("username", user.getUsername());
                response.addProperty("displayName", user.getDisplayName() != null ? user.getDisplayName() : username);
                response.addProperty("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "");
                response.addProperty("darkMode", user.isDarkMode());
                response.addProperty("background", user.getBackground() != null ? user.getBackground() : "");
            }
        } else {
            response.addProperty("error" , "Wrong username or password, or the account is temporarily locked.");
        }

        HttpUtils.sendResponse(exchange, 200, response);
    }

    //handle block contact
    private void handleBlockUser(HttpExchange exchange) throws IOException {

        JsonObject body = HttpUtils.readBody(exchange);

        String ownerUsername = requireString(body, "ownerUsername");
        String blockedUsername = requireString(body, "blockedUsername");

        boolean result = authService.blockUser(ownerUsername, blockedUsername);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }
}
