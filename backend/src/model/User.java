package model;

import java.util.ArrayList;
import java.util.List;

public class User {

    // basic info
    private String username;
    private String password;
    private String number;

    // unique id
    private final String userId;

    // security
    private int wrongPasswordCount;
    private long blockedUntil;

    // profile
    private String displayName;
    private String profileImage;
    private String biography;
    private String background; //profile background
    private long lastSeen;

    // social
    private List<String> contacts = new ArrayList<>();
    private List<String> blockedUsers = new ArrayList<>();

    // constructor (Builder)
    private User(Builder builder) {

        this.username = builder.username;
        this.password = builder.password;
        this.number = builder.number;

        this.userId = (builder.userId != null) ? builder.userId : "U" + System.currentTimeMillis();

        this.wrongPasswordCount = builder.wrongPasswordCount;
        this.blockedUntil = builder.blockedUntil;

        this.displayName = builder.displayName;
        this.profileImage = builder.profileImage;
        this.biography = builder.biography;
        this.background = builder.background;
        this.lastSeen = System.currentTimeMillis();

        if (builder.contacts != null) {
            this.contacts = builder.contacts;
        }

        if (builder.blockedUsers != null) {
            this.blockedUsers = builder.blockedUsers;
        }
    }

    // social methods
    public void addContact(String userId) {
        if (!contacts.contains(userId)) {
            contacts.add(userId);
        }
    }

    public void removeContact(String userId) {
        if (contacts.contains(userId))
            contacts.remove(userId);
    }

    public void blockUser(String userId) {
        if (!blockedUsers.contains(userId))
            blockedUsers.add(userId);
    }

    public void unblockUser(String userId) {
        if (blockedUsers.contains(userId))
            blockedUsers.remove(userId);
    }

    // getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNumber() {
        return number;
    }

    public String getUserId() {
        return userId;
    }

    public int getWrongPasswordCount() {
        return wrongPasswordCount;
    }

    public long getBlockedUntil() {
        return blockedUntil;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getBiography() {
        return biography;
    }

    public String getBackground() {
        return background;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public List<String> getBlockedUsers() {
        return blockedUsers;
    }

    // setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setWrongPasswordCount(int wrongPasswordCount) {
        this.wrongPasswordCount = wrongPasswordCount;
    }

    public void setBlockedUntil(long blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void setBackground(String background) {
        this.background = background;
    }


    // builder class
    public static class Builder {

        private String username;
        private String password;
        private String number;

        private String userId;

        private int wrongPasswordCount = 0;
        private long blockedUntil = 0;

        private String displayName;
        private String profileImage;
        private String biography;
        private String background;

        private List<String> contacts;
        private List<String> blockedUsers;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder number(String number) {
            this.number = number;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder wrongPasswordCount(int wrongPasswordCount) {
            this.wrongPasswordCount = wrongPasswordCount;
            return this;
        }

        public Builder blockedUntil(long blockedUntil) {
            this.blockedUntil = blockedUntil;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder profileImage(String profileImage) {
            this.profileImage = profileImage;
            return this;
        }

        public Builder biography(String biography) {
            this.biography = biography;
            return this;
        }

        public Builder background(String background) {
            this.background = background;
            return this;
        }

        public Builder contacts(List<String> contacts) {
            this.contacts = contacts;
            return this;
        }

        public Builder blockedUsers(List<String> blockedUsers) {
            this.blockedUsers = blockedUsers;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}