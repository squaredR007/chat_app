package controller;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AuthService;
import java.io.IOException;

public class AuthController implements HttpHandler {

    private final AuthService authService;//access to the service

    //constructor
    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

        HttpUtils.sendResponse(exchange, 200, response);
    }
}
