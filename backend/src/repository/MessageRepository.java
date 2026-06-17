package repository;

import model.Message ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

// In this class we store messages of a specific chat but not in a single global list which is useful
public class MessageRepository {

    //list of the messages belonging to that chat
    private Map<String , List<Message>> messagesByChatId = new HashMap<>() ;

    //Adds a new message to the given chat's message list

    public void save(String chatId , Message message) {
        //* computeIfAbsent creates a new empty list automatically the first time
        messagesByChatId.computeIfAbsent(chatId , k-> new ArrayList<>()).add(message);
    }

    //Returns all of the messages for a given chat as a list

    public List<Message> findByChatId (String chatId) {
        return messagesByChatId.getOrDefault(chatId , new ArrayList<>()) ;
    }

    //Returning all reported messages as a List(Used in Admins CLI)

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

    // Finds a message by its id which was set in message class as a field

    public Message findById(String chatId , String messageId) {
        for (Message m : findByChatId(chatId)) {
            if (m.getId().equals(messageId))
                return m;
        }
        return null ;
    }

    //Deletes a message by only marking it as deleted cause deleted messages must be available in history

    public void delete(String chatId , String messageId) {
        Message m = findById(chatId , messageId) ;
        if (m != null)
            m.markAsDeleted();
    }
}
