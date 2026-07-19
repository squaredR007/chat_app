package service;

import com.google.gson.JsonObject;
import model.Message ;
import model.PrivateChat;
import repository.MessageRepository ;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List ;
import java.util.UUID ;

//This class decides if any action which is going to be done is allowed or not

public class MessageService {

    private MessageRepository messageRepository ;
    private WebSocketHandler webSocketHandler ;

    //Prevents sending way too long messages

    private static final int MAX_MESSAGE_LENGTH = 1000 ;

    //Prevents spamming

    private static final int MAX_MESSAGE_PER_SECOND = 5 ;

    //Constructor

    public MessageService (MessageRepository messageRepository) {
        this(messageRepository , null) ;
    }

    //In phase 1 we used to send a REST every 5 second to update new messages by polling cause websocket was not implemented completely. But now we can use WebSocket for this aim .
    public  MessageService (MessageRepository messageRepository , WebSocketHandler webSocketHandler) {
        this.messageRepository = messageRepository ;
        this.webSocketHandler = webSocketHandler ;
    }

    public void setWebSocketHandler (WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler ;
    }

    //This method throws exception if the user tries to spam or send a way too long message
    //If the message is valid it would be sent

    public Message sendMessage (String chatId , String senderUsername , String content , Message.MessageType type) {
        if (chatId == null || chatId.trim().isEmpty()) {
            throw new RuntimeException("Chat id is required");
        }
        if (senderUsername == null || senderUsername.trim().isEmpty()) {
            throw new RuntimeException("Sender is required");
        }
        if (type == null) {
            type = Message.MessageType.TEXT;
        }

        if (type == Message.MessageType.TEXT) {
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("Message content cannot be empty");
            }
        }
        if (content == null) {
            content = "" ;
        }
        if (content.length() > MAX_MESSAGE_LENGTH){
            throw new RuntimeException("The message is too long") ;
        }
        //isSpamming is written at the following of the code
        if (isSpamming(chatId , senderUsername)) {
            throw new RuntimeException("Too many messages") ;
        }

        // Used UUID library to generate random ids for messages
        Message message = new Message(UUID.randomUUID().toString() , senderUsername , content , type) ;
        messageRepository.save(chatId , message);

        broadcastEvent("new_message", chatId, message);

        return message ;
    }
    //The main anti_spam method used above

    private boolean isSpamming(String chatId , String senderUsername) {
        List<Message> recent = messageRepository.findByChatId(chatId) ;
        long now = System.currentTimeMillis() ;
        int count = 0 ;
        for (Message m : recent) {
            if (m.getSenderUsername().equals(senderUsername) && m.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli() > now - 1000){
                count++ ;
            }
        }
        if (count >= MAX_MESSAGE_PER_SECOND)
            return true;
        else
            return false ;
    }

    //The following method edits a message which will be found among other messages using its id and chatID as well

    public void editMessage (String chatId , String messageId , String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new RuntimeException("New content cannot be empty");
        }
        if (newContent.length() > MAX_MESSAGE_LENGTH) {
            throw new RuntimeException("The message is too long");
        }
        Message m = messageRepository.findById(chatId , messageId) ;
        if (m == null)
            throw new RuntimeException("Message not found");
        if (m.isDeleted())
            throw new RuntimeException("Cannot edit a deleted message");
        m.editContent(newContent);
        messageRepository.save(chatId , m);
        broadcastEvent("message_edited", chatId, m);
    }

    //The following method would delete a message (the content would be kept in history as it is written in Message class)
    //Also someone has this right to just delete their OWN messages

    public void deleteMessage(String chatId, String messageId, String requestingUsername) {
        Message m = messageRepository.findById(chatId, messageId);
        if (m == null) throw new RuntimeException("Message not found");
        if (!m.getSenderUsername().equals(requestingUsername)) {
            throw new RuntimeException("You can only delete your own messages");
        }
        m.markAsDeleted();
        messageRepository.save(chatId , m);

        broadcastEvent("message_deleted", chatId, m);
    }

    //The following method marks a message as reported message (Used by Admin CLI)

    public void reportMessage (String chatId , String messageId) {
        Message m = messageRepository.findById(chatId, messageId) ;
        if (m == null)
            throw new RuntimeException("Message not found");
        m.markAsReported();
        messageRepository.save(chatId , m);
    }

    //Returning only new messages since a given timestamp
    public List<Message> getMessagesSince(String chatId, long sinceTimestamp) {
        List<Message> all = messageRepository.findByChatId(chatId);
        List<Message> result = new ArrayList<>();
        for (Message m : all) {
            if (m.getTimestamp()
                    .toInstant(java.time.ZoneOffset.UTC)
                    .toEpochMilli() > sinceTimestamp) {
                result.add(m);
            }
        }
        return result;
    }

    private void broadcastEvent(String eventType, String chatId, Message message) {
        if (webSocketHandler == null) return;
        try {
            JsonObject event = new JsonObject();
            event.addProperty("event", eventType);
            event.addProperty("chatId", chatId);

            JsonObject msgJson = new JsonObject();
            msgJson.addProperty("id", message.getId());
            msgJson.addProperty("senderUsername", message.getSenderUsername());
            msgJson.addProperty("content", message.getContent());
            msgJson.addProperty("type", message.getType().toString());
            msgJson.addProperty("deleted", message.isDeleted());
            msgJson.addProperty("edited", message.isEdited());
            event.add("message", msgJson);

            webSocketHandler.broadcast(event.toString());
        } catch (Exception e) {
            System.err.println("Failed to broadcast WebSocket event: " + e.getMessage());
        }
    }

    //The following method returns all of the messages of specific chat as a list since they should be visible when you open a chat

    public List<Message> getMessages (String chatId) {
        return messageRepository.findByChatId(chatId);
    }

    //Returns edited/deleted messages of a chat, most recent first (used by the history page)
    public List<Message> getEditedOrDeletedMessages(String chatId) {
        List<Message> result = messageRepository.findEditedOrDeleted(chatId);
        result.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return result;
    }

    //Getting all of the messages which were being reported

    public List<Message> getReportedMessages() {
        return messageRepository.findAllReported();
    }
}
