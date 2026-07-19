package repository;

import com.google.gson.Gson;
import model.Message;
import service.EncryptionService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageRepository {

    private final Map<String, List<Message>> messagesByChatId = new ConcurrentHashMap<>();
    private final Gson gson;
    private final String messagesFolderPath = "database/messages/";
    private final EncryptionService encryptionService;

    public MessageRepository() {

        this.gson = PersistenceGson.getGson();
        this.encryptionService = new EncryptionService();
        loadAllMessagesFromFiles();
    }

    private void loadAllMessagesFromFiles() {
        File folder = new File(messagesFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return;

        for (File file : files) {
            String fileName = file.getName();
            String chatId = fileName.substring(0, fileName.length() - ".txt".length());

            Path path = file.toPath();
            List<String> lines = FileDatabase.readLines(path);
            List<Message> chatMessages = messagesByChatId.computeIfAbsent(chatId, k -> new CopyOnWriteArrayList<>());

            for (String line : lines) {
                try {
                    Message message = gson.fromJson(line, Message.class);


                    if (message.getType() == Message.MessageType.TEXT && message.getContent() != null) {
                        message.setContent(encryptionService.decrypt(message.getContent()));
                    }
                    if (message.getType() == Message.MessageType.TEXT && message.getPreviousContent() != null) {
                        message.setPreviousContent(encryptionService.decrypt(message.getPreviousContent()));
                    }
                    chatMessages.add(message);
                } catch (Exception e) {
                    System.err.println("Skipping corrupted message in " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    public synchronized void updateMessagePersistence(String chatId) {
        List<Message> chatMessages = messagesByChatId.get(chatId);
        if (chatMessages == null) return;

        Path path = Paths.get(messagesFolderPath + chatId + ".txt");
        List<String> lines = new ArrayList<>();

        for (Message originalMsg : chatMessages) {
            String contentForFile = originalMsg.getContent();
            String previousContentForFile = originalMsg.getPreviousContent();

            if (originalMsg.getType() == Message.MessageType.TEXT && contentForFile != null) {
                contentForFile = encryptionService.encrypt(contentForFile);
            }
            if (originalMsg.getType() == Message.MessageType.TEXT && previousContentForFile != null) {
                previousContentForFile = encryptionService.encrypt(previousContentForFile);
            }

            Message copyForFile = new Message(
                    originalMsg.getId(),
                    originalMsg.getSenderUsername(),
                    contentForFile,
                    originalMsg.getType()
            );
            copyForFile.setTimestamp(originalMsg.getTimestamp());
            copyForFile.setEdited(originalMsg.isEdited());
            copyForFile.setDeleted(originalMsg.isDeleted());
            copyForFile.setReported(originalMsg.isReported());
            copyForFile.setPreviousContent(previousContentForFile);

            lines.add(gson.toJson(copyForFile));
        }
        FileDatabase.writeLines(path, lines);
    }

    public void save(String chatId, Message message) {
        List<Message> chatMessages = messagesByChatId.computeIfAbsent(chatId, k -> new CopyOnWriteArrayList<>());
        boolean alreadyPresent = false;
        for (Message m : chatMessages) {
            if (m.getId().equals(message.getId())) {
                alreadyPresent = true;
                break;
            }
        }
        if (!alreadyPresent) {
            chatMessages.add(message);
        }
        updateMessagePersistence(chatId);
    }

    public List<Message> findByChatId(String chatId) {
        List<Message> chatMessages = messagesByChatId.get(chatId);
        if (chatMessages == null) return new ArrayList<>();
        return new ArrayList<>(chatMessages);
    }

    //Returns messages of a chat that were edited and/or deleted (used by the per-chat history page)
    public List<Message> findEditedOrDeleted(String chatId) {
        List<Message> result = new ArrayList<>();
        for (Message m : findByChatId(chatId)) {
            if (m.isEdited() || m.isDeleted()) {
                result.add(m);
            }
        }
        return result;
    }

    public List<Message> findAllReported() {
        List<Message> reported = new ArrayList<>();
        for (List<Message> chatMessages : messagesByChatId.values()) {
            for (Message m : chatMessages) {
                if (m.isReported()) {
                    reported.add(m);
                }
            }
        }
        return reported;
    }

    public Message findById(String chatId, String messageId) {
        for (Message m : findByChatId(chatId)) {
            if (m.getId().equals(messageId))
                return m;
        }
        return null;
    }

    public void delete(String chatId, String messageId) {
        Message m = findById(chatId, messageId);
        if (m != null) {
            m.markAsDeleted();
            updateMessagePersistence(chatId);
        }
    }
}