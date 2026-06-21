package service;
import model.Chat ;
import model.PrivateChat ;
import repository.ChatRepository ;
import java.util.List ;

public class ChatService {
    private static ChatRepository chatRepository ;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository ;
    }

    //Creating a private chat between 2 users and save it

    public PrivateChat creatPrivateChat (String chatId , String user1 , String user2) {
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
}
