package repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.Chat;
import model.GroupChat;
import model.PrivateChat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRepository {

    private final List<Chat> chats;
    private final Path filePath = Paths.get("database/chats.txt");
    private final Gson gson;

    public ChatRepository() {
        this.chats = new CopyOnWriteArrayList<>();

        this.gson = PersistenceGson.getGson();
        loadChatsFromFile();
    }

    private void loadChatsFromFile() {
        List<String> lines = FileDatabase.readLines(filePath);
        for (String line : lines) {
            try {
                JsonObject jsonObject = gson.fromJson(line, JsonObject.class);

                if (jsonObject.has("group")) {
                    GroupChat groupChat = gson.fromJson(line, GroupChat.class);
                    chats.add(groupChat);
                } else {
                    PrivateChat privateChat = gson.fromJson(line, PrivateChat.class);
                    chats.add(privateChat);
                }
            } catch (Exception e) {

                System.err.println("Skipping corrupted chat line: " + e.getMessage());
            }
        }
    }

    public void saveAll() {
        List<String> lines = new ArrayList<>();
        for (Chat chat : chats) {
            lines.add(gson.toJson(chat));
        }
        FileDatabase.writeLines(filePath, lines);
    }

    public void save(Chat chat) {
        if (!chats.contains(chat)) {
            chats.add(chat);
        }
        saveAll();
    }

    public Chat getByChatId(String chatId) {
        if (chatId == null) return null;
        for (Chat chat : chats) {
            if (chat.getChatId().equals(chatId)) {
                return chat;
            }
        }
        return null;
    }


    public boolean delete(String chatId) {
        Chat chat = getByChatId(chatId);
        if (chat == null) {
            return false;
        }
        chats.remove(chat);
        saveAll();
        return true;
    }

    public List<Chat> getChats() {
        return new ArrayList<>(chats);
    }
}