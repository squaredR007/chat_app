package model;

import java.util.ArrayList ;
import java.util.List ;

public abstract class Chat {
    protected String chatId ;
    protected List<Message> messages ;
    protected boolean isPinned ;
    protected boolean isArchived ;

    public Chat(String chatId) {
        this.chatId = chatId ;
        messages = new ArrayList<>();
        isPinned = false ;
        isArchived = false ;
    }

    public void addMessage (Message message) {
        messages.add(message) ;
    }

    public abstract String getDisplayName () ;

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
}
