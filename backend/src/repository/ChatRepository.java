package repository;

import model.Chat ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.ConcurrentHashMap ;

//used HashMap to store data in memory . It is temporary for phase1

public class ChatRepository {

    //The chat object can be stored here by using chatId as key

   private Map<String , Chat> chats = new ConcurrentHashMap<>() ;

   //saving or editing a chat

    public void save (Chat chat) {
        chats.put(chat.getChatId(), chat) ;
    }

    //finding a chat using its id .

    public Chat findById (String chatId) {
        return chats.get(chatId) ;
    }

    //returning all chats in an arrayList

    public List<Chat> findAll() {
        return new ArrayList<>(chats.values()) ;
    }

    //removing a chat by its id

    public void delete(String chatId) {
        chats.remove(chatId) ;
    }


}
