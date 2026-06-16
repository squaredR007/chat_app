package controller;

import service.AuthService;

public class AuthController {

    private AuthService authService;//access to the service

    //constructor
    public AuthController(AuthService authService){
        this.authService=authService;
    }

    //handle user signup
    public boolean signup(String username, String password, String number){
        return authService.signup(username, password, number);
    }

    //handle user login
    public boolean login(String username, String password){
        return authService.login(username, password);
    }
}
