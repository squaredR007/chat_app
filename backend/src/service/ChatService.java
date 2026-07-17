package service;

import model.Chat;
import model.PrivateChat;
import repository.ChatRepository;
import repository.UserRepository;

import java.util.List;

public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    // Creating a private chat between 2 users and save it
    public PrivateChat creatPrivateChat(String chatId, String user1, String user2) {
        if (userRepository.getByUsername(user1) == null) {
            throw new RuntimeException();
        }
        if (!user1.equals(user2) && userRepository.getByUsername(user2) == null) {
            throw new RuntimeException("User not found: " + user2);
        }

        if (chatRepository.getByChatId(chatId) != null) {
            return (PrivateChat) chatRepository.getByChatId(chatId);
        }

        PrivateChat chat = new PrivateChat(chatId, user1, user2);
        chatRepository.save(chat);
        return chat;
    }

    // Creating saved messages
    public PrivateChat createSavedMessagesChat(String username) {
        String savedChatId = "saved_" + username;
        return creatPrivateChat(savedChatId, username, username);
    }

    // Returning a chat by its id
    public Chat getChat(String chatId) {
        return chatRepository.getByChatId(chatId);
    }

    // Returning all chats (Used while making Home)
    public List<Chat> getAllChats() {
        return chatRepository.getChats();
    }

    // Marking a chat as archived (would be added to archive folder later on front)
    public void archiveChat(String chatId) {
        Chat chat = chatRepository.getByChatId(chatId);
        if (chat == null) {
            throw new RuntimeException("Chat not found");
        }
        chat.setArchived(true);

        chatRepository.save(chat);
    }

    // Marking a chat as pinned chat
    public void pinChat(String chatId) {
        Chat chat = chatRepository.getByChatId(chatId);
        if (chat == null) {
            throw new RuntimeException("Chat not found");
        }
        chat.setPinned(true);

        chatRepository.save(chat);
    }

    public void unpinChat(String chatId) {
        Chat chat = chatRepository.getByChatId(chatId);
        if (chat == null) throw new RuntimeException("Chat not found");
        chat.setPinned(false);
        chatRepository.save(chat);
    }

    public void unarchiveChat(String chatId) {
        Chat chat = chatRepository.getByChatId(chatId);
        if (chat == null) throw new RuntimeException("Chat not found");
        chat.setArchived(false);


        chatRepository.save(chat);
    }

    //A method for counting unseen messages

    public void markAsRead(String chatId, String username) {
        Chat chat = chatRepository.getByChatId(chatId);
        if (chat == null) {
            throw new RuntimeException("Chat not found");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        chat.markAsRead(username);
        chatRepository.save(chat);
    }
}