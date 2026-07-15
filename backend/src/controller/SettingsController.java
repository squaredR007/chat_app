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

    //Checking whether a required JSON field is empty or null
    private String requireString(JsonObject body , String field) {
        if (!body.has(field) || body.get(field).isJsonNull()) {
            throw new RuntimeException("Missing required field: " + field) ;
        }
        return body.get(field).getAsString() ;
    }

    //account change handle
    private void handleChangeNumber(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        String number = requireString(body , "number");

        boolean result = settingsService.changeNumber(userId, number);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeUsername(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        String username = requireString(body , "username");

        boolean result = settingsService.changeUsername(userId, username);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangePassword(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        String password = requireString(body , "password");

        boolean result = settingsService.changePassword(userId, password);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeBackground(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        String background = requireString(body , "background");

        boolean result = settingsService.changeBackground(userId, background);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleDeleteAccount(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");

        boolean result = settingsService.deletedAccount(userId);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    //profile change handle
    private void handleChangeProfileImage(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        String profileImage = requireString(body , "profileImage");

        boolean result = settingsService.changeProfileImage(userId, profileImage);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeDisplayName(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        String newName = requireString(body , "newName");

        boolean result = settingsService.changeDisplayName(userId, newName);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }

    private void handleChangeBiography(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        String biography = requireString(body , "biography");

        boolean result = settingsService.changeBiography(userId, biography);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }


    //chat change handle
    private void handleChangeDarkMode(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String userId = requireString(body , "userId");
        if (!body.has("darkmode") || body.get("darkmode").isJsonNull()) {
            throw new RuntimeException("Missing required field: darkmode") ;
        }
        boolean darkMode = body.get("darkmode").getAsBoolean();

        boolean result = settingsService.changeDarkMode(userId, darkMode);

        JsonObject response = new JsonObject();
        response.addProperty("success", result);

        HttpUtils.sendResponse(exchange, 200, response);
    }
}
