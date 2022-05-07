package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

// NOTE(Aidan): This structure isn't technically accurate,
// but it serves well enough for the purposes of serialization.

public class SceNpId implements Serializable {  
    public String handle;
    public String platformType;
    
    public SceNpId() {};
    public SceNpId(String psid) {
        if (psid == null) return;
        if (psid.length() > 0x14)
            psid = psid.substring(0, 0x14);
        this.handle = psid;
    }

    public SceNpId serialize(Serializer serializer, Serializable structure) {
        SceNpId id = (structure == null) ? new SceNpId() : (SceNpId) structure;
       
        if (serializer.revision.head < 0x234) {
            int size = serializer.i32(0x10);
            id.handle = serializer.str(id.handle, size);
            serializer.i8((byte) 0);
            serializer.i8a(new byte[3]);
            serializer.i8a(new byte[8]);
            serializer.i8a(new byte[8]);
        } else {
            id.handle = serializer.str(id.handle, 0x14);
            id.platformType = serializer.str(id.platformType, 0x10);
        }
        
        return id;
    }
    
    
}
