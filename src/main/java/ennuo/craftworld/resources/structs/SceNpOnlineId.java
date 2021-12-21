package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

// NOTE(Abz): This structure isn't technically accurate,
// but it serves well enough for the purposes of serialization.

public class SceNpOnlineId implements Serializable {
    public String handle;

    public SceNpOnlineId serialize(Serializer serializer, Serializable structure) {
        SceNpOnlineId id = (structure == null) ? new SceNpOnlineId() : (SceNpOnlineId) structure;
       
        if (serializer.revision.head < 0x234) {
            int size = serializer.i32(0x10);
            id.handle = serializer.str(id.handle, size);
            serializer.i8((byte) 0);
            serializer.i8a(new byte[3]);
        } else 
            id.handle = serializer.str(id.handle, 0x14);
        
        return id;
    }
    
    
}
