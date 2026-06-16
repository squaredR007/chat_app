package repository;

import model.Group ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

//used HashMap to store data in memory . It is temporary for phase1

public class GroupRepository {

    private Map <String , Group> groups = new HashMap<>() ;

    //saving a new group or updating it

    public void save (Group group) {
        groups.put(group.getGroupId(), group) ;
    }

    //finds a group using its id

    public Group findById(String groupId) {
        return groups.get(groupId) ;
    }

    //returns all of the groups which were stored as a list (Used by Admin)

    public List<Group> findALl () {
        return new ArrayList<>(groups.values()) ;
    }

    //removes a group by its id

    public void delete(String groupId){
        groups.remove(groupId) ;
    }
}
