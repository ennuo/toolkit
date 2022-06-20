package cwlib.structs.inventory;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PhotoUser implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public String PSID;
    public String user;
    public Vector4f bounds = new Vector4f().zero();

    @SuppressWarnings("unchecked")
    @Override public PhotoUser serialize(Serializer serializer, Serializable structure) {
        PhotoUser user = (structure == null) ? new PhotoUser() : (PhotoUser) structure;

        user.PSID = serializer.str(user.PSID, 0x14);
        user.user  = serializer.wstr(user.user);
        user.bounds = serializer.v4(user.bounds);

        return user;
    }

    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        if (this.user != null)
            size += (user.length() * 2);
        return size;
    }
}
