package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Chat {
    //needed fields
    protected String chatId;
    protected List<Message> messages;
    protected boolean isPinned;
    protected boolean isArchived;

    //tracks, per username, the last time that user "read" this chat

    protected Map<String, Long> lastReadTimestamps;

    // constructor
    public Chat(String chatId) {
        this.chatId = chatId;
        messages = new ArrayList<>();
        isPinned = false;
        isArchived = false;
        lastReadTimestamps = new HashMap<>();
    }

    //getters and setters

    public void addMessage(Message message) {
        messages.add(message);
    }

    public abstract String getDisplayName();

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    //marks this chat as read "now" for the given username
    public void markAsRead(String username) {
        if (lastReadTimestamps == null) {
            lastReadTimestamps = new HashMap<>();
        }
        lastReadTimestamps.put(username, System.currentTimeMillis());
    }

    //returns the last time the given username read this chat, or 0 if never
    public long getLastReadTimestamp(String username) {
        if (lastReadTimestamps == null || !lastReadTimestamps.containsKey(username)) {
            return 0L;
        }
        return lastReadTimestamps.get(username);
    }

    public Map<String, Long> getLastReadTimestamps() {
        return lastReadTimestamps;
    }

    public void setLastReadTimestamps(Map<String, Long> lastReadTimestamps) {
        this.lastReadTimestamps = lastReadTimestamps;
    }
}