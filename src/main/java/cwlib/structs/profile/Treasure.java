package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Treasure implements Serializable {
    public int treasureID;
    public int planID;
    public int timestamp;
    
    public Treasure serialize(Serializer serializer, Serializable structure) {
        Treasure treasure = 
                (structure == null) ? new Treasure() : (Treasure) structure;
        
        treasure.treasureID = serializer.i32(treasure.treasureID);
        treasure.planID = serializer.i32(treasure.planID);
        treasure.timestamp = serializer.i32(treasure.timestamp);

        return treasure;
    }
    
    
    
}
