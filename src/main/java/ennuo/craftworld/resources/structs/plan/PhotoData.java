package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PhotoData implements Serializable {
    public static int MAX_SIZE = 0x69 + PhotoMetadata.MAX_SIZE + (PhotoUser.MAX_SIZE * 4);
    
    public ResourcePtr icon;
    public ResourcePtr sticker;
    public PhotoMetadata photoMetadata = new PhotoMetadata();
    public ResourcePtr painting;

    public PhotoData serialize(Serializer serializer, Serializable structure) {
        PhotoData data = (structure == null) ? new PhotoData() : (PhotoData) structure;
        
        data.icon = serializer.resource(data.icon, RType.TEXTURE, true);
        data.sticker = serializer.resource(data.sticker, RType.TEXTURE, true);
        data.photoMetadata = serializer.struct(data.photoMetadata, PhotoMetadata.class);
        if (serializer.revision > 0x395)
            data.painting = serializer.resource(data.painting, RType.PAINTING, true);
        
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
