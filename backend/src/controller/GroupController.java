package controller;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.GroupService;
import model.Group;
import java.io.IOException;

public class GroupController implements HttpHandler {

    private final GroupService groupService;

    //constructor

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            HttpUtils.handleCors(exchange);
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/group/create") && method.equals("POST")) {
                handleCreateGroup(exchange);
            } else if (path.equals("/api/group/addMember") && method.equals("POST")) {
                handleAddMember(exchange);
            } else if (path.equals("/api/group/removeMember") && method.equals("POST")) {
                handleRemoveMember(exchange);
            } else if (path.equals("/api/group/info") && method.equals("GET")) {
                handleGetGroupInfo(exchange);
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (RuntimeException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }

    //Creating a group

    private void handleCreateGroup(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String groupId = body.get("groupId").getAsString();
        String chatId = body.get("chatId").getAsString();
        String groupName = body.get("groupName").getAsString();
        String adminUsername = body.get("adminUsername").getAsString();

        groupService.creatGroup(groupId, chatId, groupName, adminUsername);

        JsonObject response = new JsonObject();
        response.addProperty("status", "group created");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Adding a member to the group

    private void handleAddMember(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String groupId = body.get("groupId").getAsString();
        String username = body.get("username").getAsString();

        groupService.addMember(groupId, username);

        JsonObject response = new JsonObject();
        response.addProperty("status", "member added");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Removing a member

    private void handleRemoveMember(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String groupId = body.get("groupId").getAsString();
        String username = body.get("username").getAsString();

        groupService.removeMember(groupId, username);

        JsonObject response = new JsonObject();
        response.addProperty("status", "member removed");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Returning groups data's

    private void handleGetGroupInfo(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        JsonObject queryParams = HttpUtils.parseQueryString(query);
        String groupId = queryParams.get("groupId").getAsString();

        Group group = groupService.getGroup(groupId);


        HttpUtils.sendResponse(exchange, 200, group);
    }
}