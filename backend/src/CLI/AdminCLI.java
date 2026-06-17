package cli;

import model.User;

import java.util.Scanner;

import repository.GroupRepository;
import repository.UserRepository;
import service.GroupService;
import service.MessageService;

public class AdminCLI {

    //admin information
    private static final String adminUsername1="Reyhane";
    private static final String adminUsername2="Zeinab";
    private static final String adminPassword="06Zein&Hane07";

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final MessageService messageService;


    public AdminCLI(UserRepository userRepository, GroupRepository groupRepository,GroupService groupService, MessageService messageService){
        this.userRepository=userRepository;
        this.groupRepository=groupRepository;
        this.groupService=groupService;
        this.messageService=messageService;
    }


    private boolean adminLogin(String username, String password) {
        if ((username.equals(adminUsername1) || username.equals(adminUsername2)) && password.equals(adminPassword))
            return true;
        return false;
    }

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
                Scanner input=new Scanner(System.in);
                choice= input.nextInt();

                switch (choice){
                    case 0:
                        break;

                    case 1:
                        System.out.println(userRepository.getUsers());
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
                        userRepository.addUser(user1);
                        break;

                    case 3:
                        System.out.println("enter the username: ");
                        String username2=input.next();
                        User user2= userRepository.getByUsername(username2);
                        userRepository.deleteUserByUserId(user2.getUserId());
                        break;

                    case 4:
                        System.out.println(groupRepository.findALl()); //show member later
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
                        break;

                    case 6:
                        //delete group
                        break;

                    case 7:
                        System.out.println("enter the group id: ");
                        String groupId2=input.next();
                        System.out.println("enter the username: ");
                        String username3=input.next();
                        groupService.addMember(groupId2, username3);
                        break;

                    case 8:
                        System.out.println("enter the group id: ");
                        String groupId3=input.next();
                        System.out.println("enter the username: ");
                        String username4=input.next();
                        groupService.removeMember(groupId3, username4);
                        break;

                    case 9:
                        //reports message
                        break;
                }
            }while (choice !=0);
        }
    }



}
