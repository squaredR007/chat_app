package repository;

import model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserRepository {

    private List<User> users;

    public UserRepository(){
        users = new CopyOnWriteArrayList<>();
    }

    //save a user
    public boolean addUser(User user){
        if (user == null)
            return false;
        if (existsByUsername(user.getUsername()))
            return false;
        if (getByUserId(user.getUserId()) != null)
            return false;

        users.add(user);
        return true;
    }

    //find a user by username
    public User getByUsername(String username){
        if (username==null)
            return null;

        for (User user: users){
            if (user.getUsername().equals(username))
                return user;
        }

        return null;
    }

    //find a user by unique id
    public User getByUserId(String userId){
        if (userId==null)
            return null;

        for (User user: users){
            if (user.getUserId().equals(userId))
                return user;
        }

        return null;
    }

    //find a user by number
    public User getByNumber(String number){
        if (number==null)
            return null;

        for (User user: users){
            if (user.getNumber().equals(number))
                return user;
        }

        return null;
    }

    //delete a user by id
    public boolean deleteUserByUserId(String userId){
        if (userId==null)
            return false;

        User user=getByUserId(userId);//create a user
        if (user!=null) {
            users.remove(user);
            return true;
        }

        return false;
    }

    //delete a user by number
    public boolean deleteUserByNumber(String number){
        if (number==null)
            return false;

        for (User user: users){
            if (user.getNumber().equals(number)) {
                users.remove(user);
                return true;
            }
        }

        return false;
    }

    //check for the existence of a user by username
    public boolean existsByUsername(String username){
        if (getByUsername(username)==null)
            return false;
        return true;
    }

    //check for the existence of a user by number
    public boolean existsByNumber(String number){
        if (getByNumber(number)==null)
            return false;
        return true;
    }

    //getter
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }
}
