package service;
import model.Group ;
import model.GroupChat;
import repository.GroupRepository ;
import repository.ChatRepository ;

public class GroupService {

    private GroupRepository groupRepository ;
    private ChatRepository chatRepository ;

    //constructor
    public GroupService (GroupRepository groupRepository , ChatRepository chatRepository) {
        this.groupRepository = groupRepository ;
        this.chatRepository = chatRepository ;
    }

    //Creating a group and also a group chat for it

    public GroupChat creatGroup (String groupId , String chatId , String groupName , String adminUsername) {
        Group group = new Group(groupId, groupName, adminUsername) ;
        groupRepository.save(group);
        GroupChat groupChat = new GroupChat(chatId , group) ;
        chatRepository.save(groupChat);
        return groupChat ;
    }

    //Adding a new member to the group

    public void addMember(String groupId, String username) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throw new RuntimeException("Group not found");
        }
        group.addMember(username);
    }

    //Removing a member from the group (Even if they have left themselves)

    public void removeMember (String groupId , String username) {
        Group group = groupRepository.findById(groupId) ;
        if (group == null) {
            throw new RuntimeException("Group not found");
        }
        group.removeMember(username);
    }

    //Returning the whole group (It's data's actually)

    public Group getGroup(String groupId) {
        return groupRepository.findById(groupId) ;
    }
}
