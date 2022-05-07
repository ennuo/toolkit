package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class CreationHistory implements Serializable {
    public String[] creators;
    
    public CreationHistory serialize(Serializer serializer, Serializable structure) {
        CreationHistory history = 
                (structure == null) ? new CreationHistory() : (CreationHistory) structure;
        
        boolean isFixed = serializer.revision.head > 0x37c;
        if (serializer.isWriting) {
            if (history.creators != null) {
                serializer.output.i32(history.creators.length);
                for (String editor : history.creators) {
                    if (isFixed) serializer.output.str(editor, 0x14);
                    else serializer.output.str16(editor);
                }
            } else serializer.output.i32(0);
            return history;
        }
        
        history.creators = new String[serializer.input.i32()];
        for (int i = 0; i < history.creators.length; ++i) {
            if (isFixed) history.creators[i] = serializer.input.str(0x14);
            else history.creators[i] = serializer.input.str16();
        }
        
        return history;
    }
    
    
}
