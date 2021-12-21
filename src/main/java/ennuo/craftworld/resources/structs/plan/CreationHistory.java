package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class CreationHistory implements Serializable {
    public String[] creators;
    
    public CreationHistory serialize(Serializer serializer, Serializable structure) {
        CreationHistory history = 
                (structure == null) ? new CreationHistory() : (CreationHistory) structure;
        
        if (serializer.isWriting) serializer.output.i32(history.creators.length);
        else history.creators = new String[serializer.input.i32()];
        for (int i = 0; i < history.creators.length; ++i)
            history.creators[i] = serializer.str(history.creators[i], 0x14);
        
        return history;
    }
    
    
}
