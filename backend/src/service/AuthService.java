package service;


import repository.UserRepository;

public class AuthService {
    private UserRepository userRepository;

    public AuthService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public void signup(String username, String password, String number){

    }
}
