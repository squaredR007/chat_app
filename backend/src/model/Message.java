package model;
import java.time.LocalDateTime ;

public class Message {
    private String id ;
    private String senderUsername ;
    private String content ;
    private String previousContent ;
    private MessageType type ;
    private LocalDateTime timestamp ;
    private boolean isDeleted ;
    private boolean isEdited ;
    private boolean isReported ;

    public enum MessageType {
        TEXT ,
        MEDIA
    }

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

    public void editContent (String newContent) {
        previousContent = content ;
        content = newContent ;
        isEdited = true ;
    }

    public void markAsDeleted () {
        isDeleted = true ;
    }

    public void markAsReported () {
        isReported = true ;
    }

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
