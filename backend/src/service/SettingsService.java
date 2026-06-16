package service;

import model.User;
import repository.UserRepository;

public class SettingsService {

    private UserRepository userRepository;

    public SettingsService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    //account change methodes
    public boolean changeNumber(String userId, String number){
        User user= userRepository.getByUserId(userId);
        if (user==null || number==null)
            return false;
        user.setNumber(number);
        return true;
    }

    public boolean changeUsername(String userId, String username){
        User user= userRepository.getByUserId(userId);
        if (user==null || username==null)
            return false;
        user.setUsername(username);
        return true;
    }

    public boolean changeBackground(String userId, String background){
        User user= userRepository.getByUserId(userId);
        if (user==null || background==null)
            return false;
        user.setBackground(background);
        return true;
    }

    public boolean deletedAccount(String userId){
        return userRepository.deleteUserByUserId(userId);
    }


    //profile change methodes
    public boolean changeProfileImage(String userId, String profileImage){
        User user= userRepository.getByUserId(userId);
        if (user==null || profileImage==null)
            return false;
        user.setProfileImage(profileImage);
        return true;
    }

    public boolean changeDisplayName(String userId, String newName){
        User user= userRepository.getByUserId(userId);
        if (user==null || newName==null)
            return false;
        user.setDisplayName(newName);
        return true;
    }

    public boolean changeBiography(String userId, String biography){
        User user= userRepository.getByUserId(userId);
        if (user==null || biography==null)
            return false;
        user.setBiography(biography);
        return true;
    }


    //chat change methodes
    public boolean changeDarkMode(String userId, boolean darkmode){
        User user= userRepository.getByUserId(userId);
        if (user==null)
            return false;
        user.setDarkMode(darkmode);
        return true;
    }

}
