package cwlib.structs.inventory;

import java.util.Date;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;

public class PhotoMetadata implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public ResourceDescriptor photo;
    public SlotID level = new SlotID();
    public String levelName;
    public SHA1 levelHash = new SHA1();
    public PhotoUser[] users;
    public long timestamp = new Date().getTime() / 1000;

    @Override
    public void serialize(Serializer serializer)
    {
        photo = serializer.resource(photo, ResourceType.TEXTURE, true);
        level = serializer.struct(level, SlotID.class);
        levelName = serializer.wstr(levelName);
        levelHash = serializer.sha1(levelHash);
        timestamp = serializer.s64(timestamp);
        users = serializer.array(users, PhotoUser.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.levelName != null)
            size += (levelName.length() * 2);
        if (this.users != null)
            size += (this.users.length * PhotoUser.BASE_ALLOCATION_SIZE);
        return size;
    }
}
