package CLI;

import model.Group;
import model.Message;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import repository.GroupRepository;
import repository.UserRepository;
import service.GroupService;
import service.MessageService;

public class AdminCLI {

    //admin information
    private static final String GroupAdminUsername1="Reyhane";
    private static final String GroupAdminUsername2="Zeinab";
    private static final String adminPassword="06Zein&Hane07";

    //necessary fields for access
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final MessageService messageService;

    //read input
    Scanner input=new Scanner(System.in);


    //constructor
    public AdminCLI(UserRepository userRepository, GroupRepository groupRepository,GroupService groupService, MessageService messageService){
        this.userRepository=userRepository;
        this.groupRepository=groupRepository;
        this.groupService=groupService;
        this.messageService=messageService;
    }


    //admin login check
    private boolean adminLogin(String username, String password) {
        if ((username.equals(GroupAdminUsername1) || username.equals(GroupAdminUsername2)) && password.equals(adminPassword))
            return true;
        return false;
    }

    //admin login
    public void start(){
        System.out.println("please enter the admin username: ");
        String adminUsername=input.next();
        System.out.println("please enter the admin password: ");
        String adminPassword=input.next();

        menu(adminUsername, adminPassword);
    }

    //access menu
    public void menu(String username, String password){
        if (adminLogin(username, password)){
            int choice;
            do{
                System.out.println("Menu:\n"+
                        "1.list users\n" +
                        "2.add user\n" +
                        "3.delete user\n" +
                        "4.list groups and members\n" +
                        "5.add group\n" +
                        "6.delete group\n" +
                        "7.add member to group\n" +
                        "8.remove member from the group\n" +
                        "9.list reported messages and sender user\n" +
                        "0.Exit");
                System.out.println("enter the request number: ");
                choice= input.nextInt();

                switch (choice){
                    case 0:
                        System.out.println("you have successfully logged out.");
                        break;

                    case 1:
                        System.out.println("List of users:");
                        for (User user: userRepository.getUsers())
                            System.out.println(user.getUsername());
                    break;

                    case 2:
                        System.out.println("enter the username: ");
                        String username1=input.next();
                        System.out.println("enter the password: ");
                        String password1=input.next();
                        System.out.println("enter the number: ");
                        String number1=input.next();
                        User user1=new User.Builder()
                                .username(username1)
                                .password(password1)
                                .number(number1)
                                .build();
                        if (userRepository.getByUsername(username1) == null) {
                            userRepository.addUser(user1);
                            System.out.println("user added successfully.");
                        }
                        else System.out.println("username already exists!");
                        break;

                    case 3:
                        System.out.println("enter the username: ");
                        String username2=input.next();
                        User user2= userRepository.getByUsername(username2);
                        if (user2==null){
                            System.out.println("User not found!");
                            break;
                        }
                        userRepository.deleteUserByUserId(user2.getUserId());
                        System.out.println("user deleted successfully.");
                        break;

                    case 4:
                        for (Group group: groupRepository.findALl()) {
                            System.out.println("group name: "+group.getGroupName());
                            System.out.println("members:");
                            for (String member: group.getMembersUsernames())
                                System.out.println(member);
                        }
                        break;

                    case 5:
                        System.out.println("enter the group id: ");
                        String groupId1=input.next();
                        System.out.println("enter the chat id: ");
                        String chatId1=input.next();
                        System.out.println("enter the group name: ");
                        String groupName1=input.next();
                        System.out.println("enter the admin username: ");
                        String adminUsername1=input.next();
                        groupService.creatGroup(groupId1, chatId1, groupName1, adminUsername1);
                        System.out.println("group created successfully.");
                        break;

                    case 6:
                        System.out.println("enter the group id: ");
                        String groupId2=input.next();
                        System.out.println("enter the chat id: ");
                        String chatId2=input.next();
                        groupService.deleteGroup(groupId2, chatId2);
                        System.out.println("group deleted successfully.");
                        break;

                    case 7:
                        System.out.println("enter the group id: ");
                        String groupId3=input.next();
                        System.out.println("enter the username: ");
                        String username3=input.next();
                        groupService.addMember(groupId3, username3);
                        System.out.println("member added to the group successfully.");
                        break;

                    case 8:
                        System.out.println("enter the group id: ");
                        String groupId4=input.next();
                        System.out.println("enter the username: ");
                        String username4=input.next();
                        groupService.removeMember(groupId4, username4);
                        System.out.println("member removed from the group successfully.");
                        break;

                    case 9:
                        List<Message> reportedMessages=messageService.getReportedMessages();
                        for (Message message: reportedMessages){
                            System.out.println("sender: "+message.getSenderUsername());
                            System.out.println("message: "+message.getContent());
                            System.out.println();
                        }
                    break;

                    default:
                        System.out.println("the entered value is invalid!");
                }
            }while (choice !=0);
        }
        else {
            System.out.println("the username or password is incorrect!");
        }
    }



}
