package model;

public class GroupChat extends Chat{
    private Group group ;

    public GroupChat(String chatId , Group group){
        super(chatId);
        this.group = group ;
    }

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
