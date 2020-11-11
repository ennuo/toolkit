package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;

public class PhotoMetadata {
    public static int MAX_SIZE = 0x22c + SlotID.MAX_SIZE + (4 * PhotoUser.MAX_SIZE);
    
    public ResourcePtr photo = new ResourcePtr(null, RType.TEXTURE);
    public SlotID level = new SlotID();
    public String levelName;
    public byte[] levelHash = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } ;
    public PhotoUser[] users = new PhotoUser[0];
    public long timestamp = 0xBF130186L;
    
    public PhotoMetadata() {}
    public PhotoMetadata(Data data) {
        photo = data.resource(RType.TEXTURE, true);
        level = new SlotID(data);
        levelName = data.str16();
        levelHash = data.bytes(0x14);
        timestamp = data.varint();
        int userCount = data.int8();
        if (userCount != 0) {
            users = new PhotoUser[userCount];
            for (int i = 0; i < userCount; ++i)
                users[i] = new PhotoUser(data);
        }
    }
    
    public void serialize(Output output) {
        output.resource(photo, true);
        level.serialize(output);
        output.str16(levelName);
        output.bytes(levelHash);
        output.varint(timestamp);
        output.int8(users.length);
        for (PhotoUser user : users)
            user.serialize(output);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PhotoMetadata)) return false;
        PhotoMetadata d = (PhotoMetadata)o;
        return (photo.equals(d.photo) && level.equals(d.level));
    }
}
