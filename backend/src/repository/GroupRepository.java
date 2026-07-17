package repository;

import com.google.gson.Gson;
import model.Group;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupRepository {

    private final Map<String, Group> groups = new ConcurrentHashMap<>();


    private final Path filePath = Paths.get("database/groups.txt");

    private final Gson gson = PersistenceGson.getGson();

    public GroupRepository() {
        load();
    }

    // saving a new group or updating it
    public void save(Group group) {
        groups.put(group.getGroupId(), group);
        save();
    }

    // finds a group using its id
    public Group findById(String groupId) {
        return groups.get(groupId);
    }

    // returns all of the groups which were stored as a list (Used by Admin)
    public List<Group> findALl() {
        return new ArrayList<>(groups.values());
    }

    // removes a group by its id
    public void delete(String groupId) {
        groups.remove(groupId);
        save();
    }

    // save groups to database

    public void save() {
        List<String> lines = new ArrayList<>();
        for (Group group : groups.values()) {
            lines.add(gson.toJson(group));
        }
        FileDatabase.writeLines(filePath, lines);
    }

    // load database
    public void load() {
        List<String> lines = FileDatabase.readLines(filePath);
        groups.clear();

        for (String line : lines) {
            try {
                Group group = gson.fromJson(line, Group.class);
                if (group != null && group.getGroupId() != null) {
                    groups.put(group.getGroupId(), group);
                }
            } catch (Exception e) {
                System.err.println("Skipping corrupted group line: " + e.getMessage());
            }
        }
    }
}