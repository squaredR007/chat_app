package service;

import model.Message ;
import repository.MessageRepository ;

import java.time.ZoneOffset;
import java.util.List ;
import java.util.UUID ;

//This class decides if any action which is going to be done is allowed or not

public class MessageService {

    private MessageRepository messageRepository ;

    //Prevents sending way too long messages

    private static final int MAX_MESSAGE_LENGTH = 1000 ;

    //Prevents spamming

    private static final int MAX_MESSAGE_PER_SECOND = 5 ;

    //Constructor

    public MessageService (MessageRepository messageRepository) {
        this.messageRepository = messageRepository ;
    }

    //This method throws exception if the user tries to spam or send a way too long message
    //If the message is valid it would be sent

    public Message sendMessage (String chatId , String senderUsername , String content , Message.MessageType type) {
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
        Message m = messageRepository.findById(chatId , messageId) ;
        if (m == null)
            throw new RuntimeException("Message not found");
        m.editContent(newContent);
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
    }

    //The following method marks a message as reported message (Used by Admin CLI)

    public void reportMessage (String chatId , String messageId) {
        Message m = messageRepository.findById(chatId, messageId) ;
        if (m == null)
            throw new RuntimeException("Message not found");
        m.markAsReported();
    }

    //The following method returns all of the messages of specific chat as a list since they should be visible when you open a chat

    public List<Message> getMessages (String chatId) {
        return messageRepository.findByChatId(chatId);
    }
}
