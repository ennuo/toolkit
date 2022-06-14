package cwlib.structs.inventory;

import cwlib.types.data.ResourceReference;
import cwlib.enums.ResourceType;
import cwlib.types.data.SHA1;
import cwlib.structs.slot.SlotID;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import java.util.Date;

public class PhotoMetadata implements Serializable {
    public static int MAX_SIZE = 0x22c + SlotID.MAX_SIZE + (4 * PhotoUser.MAX_SIZE);
    
    public ResourceReference photo = new ResourceReference(0, ResourceType.TEXTURE);
    public SlotID level = new SlotID();
    public String levelName;
    public SHA1 levelHash = new SHA1();
    public PhotoUser[] users;
    public long timestamp = new Date().getTime() / 1000;
    
    public PhotoMetadata serialize(Serializer serializer, Serializable structure) {
        PhotoMetadata metadata = 
                (structure == null) ? new PhotoMetadata() : (PhotoMetadata) structure;
        
        metadata.photo = serializer.resource(metadata.photo, ResourceType.TEXTURE, true);
        metadata.level = serializer.struct(metadata.level, SlotID.class);
        metadata.levelName = serializer.str16(metadata.levelName);
        metadata.levelHash = serializer.sha1(metadata.levelHash);
        metadata.timestamp = serializer.i64d(metadata.timestamp);
        metadata.users = serializer.array(metadata.users, PhotoUser.class);
        
        return metadata;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PhotoMetadata)) return false;
        PhotoMetadata d = (PhotoMetadata)o;
        return (photo.equals(d.photo) && level.equals(d.level));
    }
}
