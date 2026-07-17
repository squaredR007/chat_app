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
    public boolean changeNumber(String username, String number){
        User user= userRepository.getByUsername(username);
        if (user==null || number==null)
            return false;
        if (userRepository.existsByNumber(number))
            return false;
        user.setNumber(number);
        userRepository.save();
        return true;
    }

    public boolean changeUsername(String oldUsername, String newUsername){
        User user= userRepository.getByUsername(oldUsername);
        if (user==null || newUsername==null)
            return false;
        if (userRepository.existsByUsername(newUsername) && !user.getUsername().equals(newUsername))
            return false ;
        user.setUsername(newUsername);
        userRepository.save();
        return true;
    }

    public boolean changePassword(String username, String newPassword){
        User user = userRepository.getByUsername(username);
        if (user == null || newPassword == null)
            return false;
        if (!isValidPassword(newPassword, user.getUsername()))
            return false;

        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        userRepository.save();
        return true;
    }

    public boolean changeBackground(String username, String background){
        User user= userRepository.getByUsername(username);
        if (user==null || background==null)
            return false;
        user.setBackground(background);
        userRepository.save();
        return true;
    }

    public boolean deletedAccount(String number){
        return userRepository.deleteUserByNumber(number);
    }


    //profile change methodes
    public boolean changeProfileImage(String username, String profileImage){
        User user= userRepository.getByUsername(username);
        if (user==null || profileImage==null)
            return false;
        user.setProfileImage(profileImage);
        userRepository.save();
        return true;
    }

    public boolean changeDisplayName(String username, String newName){
        User user= userRepository.getByUsername(username);
        if (user==null || newName==null)
            return false;
        user.setDisplayName(newName);
        userRepository.save();
        return true;
    }

    public boolean changeBiography(String username, String biography){
        User user= userRepository.getByUsername(username);
        if (user==null || biography==null)
            return false;
        user.setBiography(biography);
        userRepository.save();
        return true;
    }


    //chat change methodes
    public boolean changeDarkMode(String username, boolean darkmode){
        User user= userRepository.getByUsername(username);
        if (user==null)
            return false;
        user.setDarkMode(darkmode);
        userRepository.save();
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
