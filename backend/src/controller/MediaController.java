package controller;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.Filestorageservice;

import java.io.IOException;

public class MediaController implements HttpHandler {

    private final Filestorageservice fileStorageService;

    public MediaController(Filestorageservice fileStorageService) {
        this.fileStorageService = fileStorageService;
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
            if (path.equals("/api/media/upload") && method.equals("POST")) {
                handleUpload(exchange);
            } else if (path.startsWith("/api/media/") && method.equals("GET")) {
                handleGetFile(exchange, path);
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage() != null ? e.getMessage() : "Bad request");
        }
    }

    //Cheking whether the JSON body is null or empty

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

    // Accepts a Base64-encoded file in the request body and saves it to disk.
    private void handleUpload(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);

        String fileData = requireString(body, "fileData");
        String fileName = "file";
        if (body.has("fileName") && !body.get("fileName").isJsonNull()) {
            String provided = body.get("fileName").getAsString();
            if (provided != null && !provided.trim().isEmpty()) {
                fileName = provided;
            }
        }

        String savedPath = fileStorageService.saveFileFromBase64(fileData, fileName);

        JsonObject response = new JsonObject();
        response.addProperty("path", savedPath);
        response.addProperty("contentType", fileStorageService.guessContentType(savedPath));
        HttpUtils.sendResponse(exchange, 200, response);
    }

    // Serves a previously uploaded file's raw bytes
    private void handleGetFile(HttpExchange exchange, String path) throws IOException {
        //Getting the relative storage path
        String relativePath = path.substring("/api/media/".length());

        if (relativePath.trim().isEmpty()) {
            HttpUtils.sendError(exchange, 400, "Missing file path");
            return;
        }

        byte[] fileBytes = fileStorageService.getFile(relativePath);
        String contentType = fileStorageService.guessContentType(relativePath);

        HttpUtils.sendFile(exchange, fileBytes, contentType);
    }
}