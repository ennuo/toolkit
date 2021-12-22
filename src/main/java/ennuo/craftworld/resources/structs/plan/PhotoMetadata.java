package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import java.util.Date;

public class PhotoMetadata implements Serializable {
    public static int MAX_SIZE = 0x22c + SlotID.MAX_SIZE + (4 * PhotoUser.MAX_SIZE);
    
    public ResourceDescriptor photo = new ResourceDescriptor(0, ResourceType.TEXTURE);
    public SlotID level = new SlotID();
    public String levelName;
    public SHA1 levelHash = new SHA1();
    public PhotoUser[] users = new PhotoUser[0];
    public long timestamp = new Date().getTime() / 1000;
    
    public PhotoMetadata serialize(Serializer serializer, Serializable structure) {
        PhotoMetadata metadata = 
                (structure == null) ? new PhotoMetadata() : (PhotoMetadata) structure;
        
        metadata.photo = serializer.resource(metadata.photo, ResourceType.TEXTURE, true);
        metadata.level = serializer.struct(metadata.level, SlotID.class);
        metadata.levelName = serializer.str16(metadata.levelName);
        metadata.levelHash = serializer.sha1(metadata.levelHash);
        metadata.timestamp = serializer.u32d(metadata.timestamp);
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
