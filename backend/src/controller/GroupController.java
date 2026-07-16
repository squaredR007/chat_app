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
            HttpUtils.sendError(exchange, 400, e.getMessage() != null ? e.getMessage() : "Bad Request");
        }
    }

    //Checking whether a required JSON field is empty or null

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

    //Creating a group

    private void handleCreateGroup(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String groupId = requireString(body , "groupId");
        String chatId = requireString(body , "chatId");
        String groupName = requireString(body , "groupName");
        String adminUsername = requireString(body , "adminUsername");

        groupService.creatGroup(groupId, chatId, groupName, adminUsername);

        JsonObject response = new JsonObject();
        response.addProperty("status", "group created");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Adding a member to the group

    private void handleAddMember(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String groupId = requireString(body , "groupId");
        String username = requireString(body , "username");

        groupService.addMember(groupId, username);

        JsonObject response = new JsonObject();
        response.addProperty("status", "member added");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Removing a member

    private void handleRemoveMember(HttpExchange exchange) throws IOException {
        JsonObject body = HttpUtils.readBody(exchange);
        String groupId = requireString(body ,"groupId");
        String username = requireString(body , "username");

        groupService.removeMember(groupId, username);

        JsonObject response = new JsonObject();
        response.addProperty("status", "member removed");
        HttpUtils.sendResponse(exchange, 200, response);
    }

    //Returning groups data's
    //Fixed item : there was a bug here in this method which was avoiding members count to be sent to front which is fixed now

    private void handleGetGroupInfo(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        JsonObject queryParams = HttpUtils.parseQueryString(query);
        if (!queryParams.has("groupId") || queryParams.get("groupId").getAsString().isEmpty()) {
            HttpUtils.sendError(exchange , 400 , "Missing required query parameter: groupId");
            return;
        }
        String groupId = queryParams.get("groupId").getAsString();

        Group group = groupService.getGroup(groupId);
        if (group == null) {
            HttpUtils.sendError(exchange , 404 , "Group not found");
            return;
        }

        JsonObject response = new JsonObject() ;
        response.addProperty("groupId" , group.getGroupId());
        response.addProperty("groupName" , group.getGroupName());
        response.addProperty("adminUsername", group.getAdminUsername());
        response.addProperty("description", group.getDescription());
        response.addProperty("groupPhotoPath", group.getGroupPhotoPath());
        response.addProperty("memberCount", group.getMembersUsernames() != null ? group.getMembersUsernames().size() : 0);

        com.google.gson.JsonArray membersArray = new com.google.gson.JsonArray();
        if (group.getMembersUsernames() != null) {
            for (String member : group.getMembersUsernames()) {
                membersArray.add(member);
            }
        }
        response.add("membersUsernames", membersArray);


        HttpUtils.sendResponse(exchange, 200, response);
    }
}