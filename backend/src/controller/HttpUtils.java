package controller;
import com.google.gson.Gson ;
import com.google.gson.JsonObject ;
import com.sun.net.httpserver.HttpExchange ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.io.BufferedReader ;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets ;

public class HttpUtils {
    private static final Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }

    //Reads the full request body and parses it into a JsonObject

    public static JsonObject readBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        String jsonString = body.toString().trim();
        if (jsonString.isEmpty())
            return new JsonObject();
        return gson.fromJson(jsonString, JsonObject.class);
    }

    //Parsing the string into JSON Object to make it easy to read

    public static JsonObject parseQueryString(String query) {
        JsonObject jsonObject = new JsonObject();
        if (query == null || query.isEmpty())
            return jsonObject;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2)
                jsonObject.addProperty(parts[0], parts[1]);
            else if (parts.length == 1)
                jsonObject.addProperty(parts[0], "");
        }
        return jsonObject;
    }

    //Sending response

    public static void sendResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String responseBody = gson.toJson(data);
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    //Handling CORS

    public static void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    //Sending error

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        sendResponse(exchange, statusCode, error);
    }
}