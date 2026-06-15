package model;

public class User {

    private String username;
    private String password;
    private String number;

    private int wrongPasswordCount;
    private boolean blocked;

    private String profileName;
    private String profileImage;
    private String biography;

    private String background;

    private User(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.number = builder.number;
        this.wrongPasswordCount = builder.wrongPasswordCount;
        this.profileName = builder.profileName;
        this.profileImage = builder.profileImage;
        this.biography = builder.biography;
        this.background = builder.background;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getWrongPasswordCount() {
        return wrongPasswordCount;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public static class Builder {
        private String username;
        private String password;
        private String number;
        private int wrongPasswordCount = 0;
        private boolean blocked=false;
        private String profileName;
        private String profileImage;
        private String biography;
        private String background;

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

        public Builder wrongPasswordCount(int wrongPasswordCount) {
            this.wrongPasswordCount = wrongPasswordCount;
            return this;
        }

        public Builder blocked(boolean blocked){
            this.blocked=blocked;
            return this;
        }

        public Builder profileName(String profileName) {
            this.profileName = profileName;
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

        public User build() {
            return new User(this);
        }
    }
}