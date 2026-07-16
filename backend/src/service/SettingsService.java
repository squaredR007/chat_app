package service;

import model.User;
import repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;

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
        if (userRepository.existsByNumber(number))
            return false;
        user.setNumber(number);
        return true;
    }

    public boolean changeUsername(String userId, String username){
        User user= userRepository.getByUserId(userId);
        if (user==null || username==null)
            return false;
        if (userRepository.existsByUsername(username) && !user.getUsername().equals(username))
            return false ;
        user.setUsername(username);
        return true;
    }

    public boolean changePassword(String userId, String password){
        User user = userRepository.getByUserId(userId);
        if (user == null || password == null)
            return false;
        if (!isValidPassword(password, user.getUsername()))
            return false;

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashedPassword);
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

    //check the password

    private boolean patternPassword(String password){
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[*&^%$#@!])[A-Za-z\\d*&^%$#@!]{8,}$";

        if (!Pattern.matches(regex, password))
            return false;
        return true;
    }

    private boolean isValidPassword(String password, String username) {
        if (password.length() < 8)
            return false;

        //check for inequality with username
        if (password.toLowerCase().contains(username.toLowerCase()))
            return false;

        //check the password pattern
        if (!patternPassword(password))
            return false;

        return true;
    }

}