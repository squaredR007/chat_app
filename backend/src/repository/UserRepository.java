package repository;

import model.User;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.google.gson.Gson;

public class UserRepository {

    private final List<User> users;
    private final Path filePath = Paths.get("database/users.txt");
    private final Gson gson = PersistenceGson.getGson();

    public UserRepository() {
        this.users = new CopyOnWriteArrayList<>();
        load();
    }

    // save a user
    public boolean addUser(User user) {
        if (user == null)
            return false;
        if (existsByUsername(user.getUsername()))
            return false;


        users.add(user);
        save();
        return true;
    }

    // find a user by username
    public User getByUsername(String username) {
        if (username == null)
            return null;

        for (User user : users) {
            if (user.getUsername().equals(username))
                return user;
        }

        return null;
    }

    // find a user by number
    public User getByNumber(String number) {
        if (number == null)
            return null;

        for (User user : users) {
            if (user.getNumber().equals(number))
                return user;
        }

        return null;
    }

    // delete a user by number
    public boolean deleteUserByNumber(String number) {
        if (number == null)
            return false;

        for (User user : users) {
            if (user.getNumber().equals(number)) {
                users.remove(user);
                save();
                return true;
            }
        }

        return false;
    }

    // check for the existence of a user by username
    public boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    // check for the existence of a user by number
    public boolean existsByNumber(String number) {
        return getByNumber(number) != null;
    }

    // getter
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    // save users to database

    public void save() {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(gson.toJson(user));
        }
        FileDatabase.writeLines(filePath, lines);
    }

    // load database
    public void load() {
        List<String> lines = FileDatabase.readLines(filePath);
        users.clear();

        for (String line : lines) {
            try {
                User user = gson.fromJson(line, User.class);
                if (user != null && user.getUsername() != null) {
                    users.add(user);
                }
            } catch (Exception e) {
                System.err.println("Skipping corrupted user line: " + e.getMessage());
            }
        }
    }
}