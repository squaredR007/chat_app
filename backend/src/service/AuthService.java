package service;


import model.User;
import repository.UserRepository;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private static final int maxFailedAttempts = 5;//locked after 5 unsuccessful attempts
    private static final long lockDuration = (long) 5 * 60 * 1000;//lock for 5 minutes
    private UserRepository userRepository;
    private ChatService chatService ;

    public AuthService(UserRepository userRepository , ChatService chatService){
        this.userRepository=userRepository;
        this.chatService = chatService ;

    }

    //signup
    public boolean signup(String username, String password, String number){
        if (username==null || password==null || number==null)
            return false;

        if (!isValidPassword(password, username))
            return false;

        if (userRepository.existsByUsername(username))
            return false;

        if (userRepository.existsByNumber(number))
            return false;

        //change password to hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        //create user and saved message
        User user=new User.Builder().username(username).password(hashedPassword).number(number).failedLoginAttempts(0).lockUntil(0).build();

        // write with stale data.
        boolean saved = userRepository.addUser(user) ;

        if (saved) {
            chatService.createSavedMessagesChat(username) ;
        }
        return saved ;
    }

    //login
    public boolean login(String username, String password){
        if (username==null || password==null)
            return false;

        User user=userRepository.getByUsername(username);
        if (user==null)
            return false;

        synchronized (user) {
            long now = System.currentTimeMillis();

            // if the account is locked
            if (user.getLockUntil() > now)
                return false;

            //unlock account
            if (user.getLockUntil() > 0 && user.getLockUntil() <= now) {
                user.resetLoginAttempts();
                user.setLockUntil(0);

                userRepository.save();
            }

            //if the wrong password is entered
            if (!BCrypt.checkpw(password, user.getPassword())) {
                user.incrementFailedLoginAttempts();
                if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
                    user.setLockUntil(now + lockDuration);
                }
                userRepository.save();
                return false;
            }

            //successful status
            user.resetLoginAttempts();
            user.setLockUntil(0);
            userRepository.save();
            return true;
        }
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


    //check the password pattern
    private boolean patternPassword(String password){
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[*&^%$#@!])[A-Za-z\\d*&^%$#@!]{8,}$";

        if (!Pattern.matches(regex, password))
            return false;
        return true;
    }
}