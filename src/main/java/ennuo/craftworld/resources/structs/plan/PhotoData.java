package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PhotoData implements Serializable {
    public static int MAX_SIZE = 0x69 + PhotoMetadata.MAX_SIZE + (PhotoUser.MAX_SIZE * 4);
    
    public ResourceDescriptor icon;
    public ResourceDescriptor sticker;
    public PhotoMetadata photoMetadata = new PhotoMetadata();
    public ResourceDescriptor painting;

    public PhotoData serialize(Serializer serializer, Serializable structure) {
        PhotoData data = (structure == null) ? new PhotoData() : (PhotoData) structure;
        
        data.icon = serializer.resource(data.icon, ResourceType.TEXTURE, true);
        data.sticker = serializer.resource(data.sticker, ResourceType.TEXTURE, true);
        data.photoMetadata = serializer.struct(data.photoMetadata, PhotoMetadata.class);
        if (serializer.revision.head > 0x395)
            data.painting = serializer.resource(data.painting, ResourceType.PAINTING, true);
        
        return data;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PhotoData)) return false;
        PhotoData d = (PhotoData)o;
        return (
                icon.equals(d.icon) &&
                sticker.equals(d.sticker) &&
                photoMetadata.equals(d.photoMetadata) &&
                painting.equals(d.painting)
        );
    }
}
