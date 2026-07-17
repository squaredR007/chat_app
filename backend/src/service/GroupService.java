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
        // FIX: removed a stray "this.groupRepository.load();" that used to be here -
        // GroupRepository's own constructor already loads its data once.
    }

    //Creating a group and also a group chat for it

    public GroupChat creatGroup (String groupId , String chatId , String groupName , String adminUsername) {
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new RuntimeException("Group id is required");
        }
        if (chatId == null || chatId.trim().isEmpty()) {
            throw new RuntimeException("Chat id is required");
        }
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new RuntimeException("Group name is required");
        }
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            throw new RuntimeException("Admin username is required");
        }
        if (groupRepository.findById(groupId) != null) {
            throw new RuntimeException("A group with this id already exists");
        }
        if (chatRepository.getByChatId(chatId) != null) {
            throw new RuntimeException("A chat with this id already exists");
        }
        Group group = new Group(groupId, groupName, adminUsername) ;

        // FIX: this used to call groupRepository.save(group) TWICE in a row (an
        // accidental duplicate line). Harmless once save() itself is correct, but
        // it was still a wasted, redundant disk write on every single group creation.
        groupRepository.save(group);

        GroupChat groupChat = new GroupChat(chatId , group) ;
        chatRepository.save(groupChat);
        return groupChat ;
    }

    //Deleting groups method which is used by Admin CLI

    public void deleteGroup(String groupId, String chatId) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throw new RuntimeException("Group not found");
        }
        groupRepository.delete(groupId);

        chatRepository.delete(chatId);
    }

    //Adding a new member to the group

    public void addMember(String groupId, String username) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throw new RuntimeException("Group not found");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        // FIX: synchronize per-group object so two concurrent addMember (or
        // addMember/removeMember) calls on the same group can't both pass
        // their "contains" check before either one actually applies its change.
        synchronized (group) {
            if (group.getMembersUsernames().contains(username)) {
                throw new RuntimeException("User is already a member of this group");
            }
            group.addMember(username);

            groupRepository.save(group);
        }
    }

    //Removing a member from the group (Even if they have left themselves)

    public void removeMember (String groupId , String username) {
        Group group = groupRepository.findById(groupId) ;
        if (group == null) {
            throw new RuntimeException("Group not found");
        }
        // FIX: same per-group synchronization as addMember, see comment there.
        synchronized (group) {
            if (username == null || !group.getMembersUsernames().contains(username)) {
                throw new RuntimeException("User is not a member of this group");
            }
            boolean wasAdmin = username.equals(group.getAdminUsername()) ;
            group.removeMember(username);

            if (wasAdmin) {
                if (!group.getMembersUsernames().isEmpty()) {
                    group.setAdminUsername(group.getMembersUsernames().get(0));
                } else {
                    group.setAdminUsername(null);
                }
            }

            groupRepository.save(group);
        }
    }

    //Returning the whole group (It's data's actually)

    public Group getGroup(String groupId) {
        return groupRepository.findById(groupId) ;
    }
}