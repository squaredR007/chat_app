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
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }

    //handle user signup
    private void handleSignup(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String username = body.get("username").getAsString();
        String password = body.get("password").getAsString();
        String number = body.get("number").getAsString();

        boolean result = authService.signup(username, password, number);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);
        if (result) {
            // return user info so frontend can store it immediately
            User user = userRepository.getByUsername(username);
            response.addProperty("userId", user.getUserId());
            response.addProperty("username", user.getUsername());
            response.addProperty("displayName", user.getDisplayName() != null ? user.getDisplayName() : username);
        }

        HttpUtils.sendResponse(exchange, 200, response);
    }

    //handle user login
    private void handleLogin(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String username = body.get("username").getAsString();
        String password = body.get("password").getAsString();

        boolean result = authService.login(username, password);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        if (result) {
            // turning back users info to be saved in local storage

            User user = userRepository.getByUsername(username);
            response.addProperty("userId", user.getUserId());
            response.addProperty("username", user.getUsername());
            response.addProperty("displayName", user.getDisplayName() != null ? user.getDisplayName() : username);
        }

        HttpUtils.sendResponse(exchange, 200, response);
    }
}
