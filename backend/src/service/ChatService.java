package service;
import model.Chat ;
import model.PrivateChat ;
import repository.ChatRepository ;
import repository.UserRepository;

import java.util.List ;

public class ChatService {
    private ChatRepository chatRepository ;
    private UserRepository userRepository ;

    public ChatService(ChatRepository chatRepository , UserRepository userRepository) {
        this.chatRepository = chatRepository ;
        this.userRepository = userRepository ;
    }

    //Creating a private chat between 2 users and save it

    public PrivateChat creatPrivateChat (String chatId , String user1 , String user2) {
        if (userRepository.getByUsername((user1)) == null) {
            throw new RuntimeException() ;
        }
        if (!user1.equals(user2) && userRepository.getByUsername(user2) == null) {
            throw new RuntimeException("User not found" + user2) ;
        }
        if (chatRepository.findById((chatId)) != null) {
            return (PrivateChat) chatRepository.findById(chatId) ;
        }
        PrivateChat chat = new PrivateChat(chatId , user1 , user2) ;
        chatRepository.save(chat);
        return chat ;
    }

    //Creating saved messages
    public PrivateChat createSavedMessagesChat(String username) {
        String savedChatId = "saved_" + username;
        return creatPrivateChat(savedChatId, username, username);
    }

    //Returning a chat by its id

    public Chat getChat (String chatId) {
        return chatRepository.findById(chatId) ;
    }

    //Returning all chats (Used while making Home)

    public List<Chat> getAllChats() {
        return chatRepository.findAll() ;
    }

    //Marking a chat as archived (would be added to archive folder later on front)

    public void archiveChat(String chatId) {
        Chat chat = chatRepository.findById(chatId) ;
        if (chat == null) {
            throw new RuntimeException("Chat not found");
        }
        chat.setArchived(true);
    }

    //Marking a chat as pinned chat

    public void pinChat (String chatId) {
        Chat chat = chatRepository.findById(chatId);
        if (chat == null) {
            throw new RuntimeException("Chat not found") ;
        }
        chat.setPinned(true);
    }

    public void unpinChat(String chatId) {
        Chat chat = chatRepository.findById(chatId);
        if (chat == null) throw new RuntimeException("Chat not found");
        chat.setPinned(false);
    }

    public void unarchiveChat(String chatId) {
        Chat chat = chatRepository.findById(chatId);
        if (chat == null) throw new RuntimeException("Chat not found");
        chat.setArchived(false);
    }
}
