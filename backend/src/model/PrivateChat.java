package model;

public class PrivateChat extends Chat{
    //needed fields
    private String user1Username ;
    private String user2Username ;

    //constructor
    public PrivateChat(String chatId , String user1Username , String user2Username) {
        super(chatId);
        this.user1Username = user1Username ;
        this.user2Username = user2Username ;
    }

    //getters and setters
    public String getDisplayName () {
        return user2Username ;
    }

    public String getUser1Username() {
        return user1Username;
    }

    public void setUser1Username(String user1Username) {
        this.user1Username = user1Username;
    }

    public String getUser2Username() {
        return user2Username;
    }

    public void setUser2Username(String user2Username) {
        this.user2Username = user2Username;
    }
}
