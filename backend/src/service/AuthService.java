package service;


import model.User;
import repository.UserRepository;

public class AuthService {
    private UserRepository userRepository;

    public AuthService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    //signup
    public boolean signup(String username, String password, String number){
        if (username==null || password==null || number==null)
            return false;

        if (userRepository.existsByUsername(username))
            return false;

        if (userRepository.existsByNumber(number))
            return false;

        //create user
        User user=new User.Builder().username(username).password(password).number(number).build();

        //save user
        return userRepository.addAUser(user);
    }

    //login
    public boolean login(String username, String password){
        if (username==null || password==null)
            return false;

        User user=userRepository.getByUsername(username);
        if (user==null)
            return false;
        if (!user.getPassword().equals(password))
            return false;

        return true;
    }
}
