package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Vector4f;

public class PhotoUser implements Serializable {
    public static int MAX_SIZE = 0x224;
    
    public String PSID;
    public String user;
    public Vector4f bounds = new Vector4f(0, 0, 0, 0);
    
    public PhotoUser() {};
    public PhotoUser(String psid) {
        if (psid == null) return;
        if (psid.length() > 0x14)
            psid = psid.substring(0, 0x14);
        this.PSID = psid;
        this.user = psid;
    }
    
    public PhotoUser serialize(Serializer serializer, Serializable structure) {
        PhotoUser user = (structure == null) ? new PhotoUser() : (PhotoUser) structure;
        
        user.PSID = serializer.str(user.PSID, 0x14);
        user.user  = serializer.str16(user.user);
        user.bounds = serializer.v4(user.bounds);
        
        return user;
    }
    
    @Override public String toString() { return this.PSID; }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PhotoUser)) return false;
        PhotoUser d = (PhotoUser)o;
        return (d.PSID.equals(PSID) && user.equals(user) && d.bounds.equals(bounds));
    }
}
