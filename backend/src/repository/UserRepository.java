package repository;

import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private List<User> users;

    public UserRepository(){
        users=new ArrayList<>();
    }

    //save a user
    public void addAUser(User user){
        if (user!=null && !findByUsername(user.getUsername()) && !findByUserId(user.getUserId()))
            users.add(user);
    }

    //find a user by username
    public boolean findByUsername(String username){
        for (User user: users){
            if (user.getUsername().equals(username))
                return true;
        }
        return false;
    }

    //find a user by unique id
    public boolean findByUserId(String userId){
        for (User user: users){
            if (user.getUserId().equals(userId))
                return true;
        }
        return false;
    }

    //delete a user
    public void deleteUser(User user){
        if (user!=null && users.contains(user))
            users.remove(user);
    }

    //getter
    public List<User> getUsers() {
        return users;
    }
}
