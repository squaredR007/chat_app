package model;

import java.util.ArrayList ;
import java.util.List ;

public class Group {
    //needed fields
    private String groupId ;
    private String groupName ;
    private String groupPhotoPath ;
    private String description ;
    private List<String> membersUsernames ;
    private String adminUsername ;

    //constructor
    public Group (String groupId , String groupName , String adminUsername) {
        this.groupId = groupId ;
        this.groupName = groupName ;
        this.adminUsername = adminUsername ;
        membersUsernames = new ArrayList<>();
        membersUsernames.add(adminUsername) ;
    }

    // adding and removing members methods

    public void addMember(String username) {
        membersUsernames.add(username) ;
    }

    public void removeMember(String username) {
        membersUsernames.remove(username) ;
    }

    //getter and setter

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPhotoPath() {
        return groupPhotoPath;
    }

    public void setGroupPhotoPath(String groupPhotoPath) {
        this.groupPhotoPath = groupPhotoPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMembersUsernames() {
        return membersUsernames;
    }

    public void setMembersUsernames(List<String> membersUsernames) {
        this.membersUsernames = membersUsernames;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
}
