package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    // basic info
    private String username;
    private String password;
    private String number;


    // security
    private long lockUntil;
    private int failedLoginAttempts;

    // profile
    private String displayName;
    private String profileImage;
    private String biography;
    private String background;
    private long lastSeen;
    private boolean darkMode;

    // social
    private List<String> contacts = new ArrayList<>();
    private List<String> blockedUsers = new ArrayList<>();

    // constructor (Builder)
    private User(Builder builder) {

        this.username = builder.username;
        this.password = builder.password;
        this.number = builder.number;

        this.lockUntil = builder.lockUntil;
        this.failedLoginAttempts=builder.failedLoginAttempts;

        this.displayName = builder.displayName;
        this.profileImage = builder.profileImage;
        this.biography = builder.biography;
        this.background = builder.background;
        this.lastSeen = System.currentTimeMillis();
        this.darkMode=builder.darkMode;

        if (builder.contacts != null) {
            this.contacts = builder.contacts;
        }

        if (builder.blockedUsers != null) {
            this.blockedUsers = builder.blockedUsers;
        }
    }

    //security login
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetLoginAttempts() {
        this.failedLoginAttempts = 0;
    }


    // social methods
    public void addContact(String username) {
        if (!contacts.contains(username) && !this.username.equals(username)) {
            contacts.add(username);
        }
    }

    public void removeContact(String username) {
        if (contacts.contains(username))
            contacts.remove(username);
    }

    public void blockUser(String username) {
        if (!blockedUsers.contains(username))
            blockedUsers.add(username);
    }

    public void unblockUser(String username) {
        if (blockedUsers.contains(username))
            blockedUsers.remove(username);
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

    public long getLockUntil() {
        return lockUntil;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public boolean isDarkMode() { return darkMode; }

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

    public void setLockUntil(long lockUntil) {
        this.lockUntil = lockUntil;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
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


        private long lockUntil = 0;
        private int failedLoginAttempts=0;

        private String displayName;
        private String profileImage;
        private String biography;
        private String background;
        private boolean darkMode;

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

        public Builder lockUntil(long lockUntil) {
            this.lockUntil = lockUntil;
            return this;
        }

        public Builder failedLoginAttempts(int failedLoginAttempts){
            this.failedLoginAttempts = failedLoginAttempts;
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

        public Builder darkMode(boolean darkMode){
            this.darkMode=darkMode;
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