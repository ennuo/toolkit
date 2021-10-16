package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector4f;

public class PhotoUser {
    public static int MAX_SIZE = 0x224;
    
    public String PSID;
    public String user;
    public Vector4f bounds = new Vector4f(0, 0, 0, 0);
    
    public PhotoUser() {}
    public PhotoUser(Data data) {
        PSID = data.str(0x14);
        user = data.str16();
        bounds = data.v4();
    }
    
    public void serialize(Output output) {
        output.str(PSID, 0x14);
        output.str16(user);
        output.v4(bounds);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PhotoUser)) return false;
        PhotoUser d = (PhotoUser)o;
        return (d.PSID.equals(PSID) && user.equals(user) && d.bounds.equals(bounds));
    }
    
    
}
