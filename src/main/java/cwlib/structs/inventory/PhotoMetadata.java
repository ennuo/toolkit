package cwlib.structs.inventory;

import java.util.Date;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;

public class PhotoMetadata implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public ResourceDescriptor photo;
    public SlotID level = new SlotID();
    public String levelName;
    public SHA1 levelHash = new SHA1();
    public PhotoUser[] users;
    public long timestamp = new Date().getTime() / 1000;

    @SuppressWarnings("unchecked")
    @Override public PhotoMetadata serialize(Serializer serializer, Serializable structure) {
        PhotoMetadata metadata = 
            (structure == null) ? new PhotoMetadata() : (PhotoMetadata) structure;

        metadata.photo = serializer.resource(metadata.photo, ResourceType.TEXTURE, true);
        metadata.level = serializer.struct(metadata.level, SlotID.class);
        metadata.levelName = serializer.wstr(metadata.levelName);
        metadata.levelHash = serializer.sha1(metadata.levelHash);
        metadata.timestamp = serializer.i64(metadata.timestamp);
        metadata.users = serializer.array(metadata.users, PhotoUser.class);

        return metadata;
    }

    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        if (this.levelName != null)
            size += (levelName.length() * 2);
        if (this.users != null)
            size += (this.users.length * PhotoUser.BASE_ALLOCATION_SIZE);
        return size;
    }
}
