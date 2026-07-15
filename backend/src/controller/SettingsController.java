package controller;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.SettingsService;
import java.io.IOException;

public class SettingsController implements HttpHandler {

    private final SettingsService settingsService; //access to the service


    //constructor
    public SettingsController(SettingsService settingsService){
        this.settingsService=settingsService;
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
            if (path.equals("/api/settings/changeNumber") && method.equals("POST")) {
                handleChangeNumber(exchange);

            } else if (path.equals("/api/settings/changeUsername") && method.equals("POST")) {
                handleChangeUsername(exchange);

            } else if (path.equals("/api/settings/changePassword") && method.equals("POST")) {
                handleChangePassword(exchange);

            } else if (path.equals("/api/settings/changeBackground") && method.equals("POST")) {
                handleChangeBackground(exchange);

            } else if (path.equals("/api/settings/deleteAccount") && method.equals("POST")) {
                handleDeleteAccount(exchange);

            } else if (path.equals("/api/settings/changeProfileImage") && method.equals("POST")) {
                handleChangeProfileImage(exchange);

            } else if (path.equals("/api/settings/changeDisplayName") && method.equals("POST")) {
                handleChangeDisplayName(exchange);

            } else if (path.equals("/api/settings/changeBiography") && method.equals("POST")) {
                handleChangeBiography(exchange);

            } else if (path.equals("/api/settings/changeDarkMode") && method.equals("POST")) {
                handleChangeDarkMode(exchange);

            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }

        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }

    //account change handle
    private void handleChangeNumber(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        String number = body.get("number").getAsString();

        boolean result = settingsService.changeNumber(userId, number);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeUsername(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        String username = body.get("username").getAsString();

        boolean result = settingsService.changeUsername(userId, username);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangePassword(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        String password = body.get("password").getAsString();

        boolean result = settingsService.changePassword(userId, password);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeBackground(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        String background = body.get("background").getAsString();

        boolean result = settingsService.changeBackground(userId, background);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleDeleteAccount(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();

        boolean result = settingsService.deletedAccount(userId);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    //profile change handle
    private void handleChangeProfileImage(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        String profileImage = body.get("profileImage").getAsString();

        boolean result = settingsService.changeProfileImage(userId, profileImage);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeDisplayName(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        String newName = body.get("newName").getAsString();

        boolean result = settingsService.changeDisplayName(userId, newName);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeBiography(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        String biography = body.get("biography").getAsString();

        boolean result = settingsService.changeBiography(userId, biography);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }


    //chat change handle
    private void handleChangeDarkMode(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = body.get("userId").getAsString();
        boolean darkMode = body.get("darkmode").getAsBoolean();

        boolean result = settingsService.changeDarkMode(userId, darkMode);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }
}
