package model;

public class GroupChat extends Chat{
    //needed fields
    private Group group ;

    //constructor
    public GroupChat(String chatId , Group group){
        super(chatId);
        this.group = group ;
    }

    //getters and setters

    public String getDisplayName() {
        return group.getGroupName() ;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
