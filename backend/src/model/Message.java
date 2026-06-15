package model;
import java.time.LocalDateTime ;

public class Message {
    //needed fields
    private String id ;
    private String senderUsername ;
    private String content ;
    private String previousContent ;
    private MessageType type ;
    private LocalDateTime timestamp ;
    private boolean isDeleted ;
    private boolean isEdited ;
    private boolean isReported ;

    //enum class to identify if the message is text or media
    public enum MessageType {
        TEXT ,
        MEDIA
    }

    //constructor
    public Message (String id , String senderUsername , String content , MessageType type) {
        this.id = id ;
        this.senderUsername = senderUsername ;
        this.content = content ;
        previousContent = null ;
        this.type = type ;
        timestamp = LocalDateTime.now() ;
        isEdited = false;
        isDeleted = false ;
        isReported = false ;
    }

    //a method for editing a message while the previous content is saved
    public void editContent (String newContent) {
        previousContent = content ;
        content = newContent ;
        isEdited = true ;
    }

    //mark the message as deleted
    public void markAsDeleted () {
        isDeleted = true ;
    }

    //mark the message as reported
    public void markAsReported () {
        isReported = true ;
    }

    //getters and setters
    public String getId() {
        return id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }
}
