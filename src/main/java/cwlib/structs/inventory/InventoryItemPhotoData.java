package cwlib.structs.inventory;

import cwlib.types.data.ResourceReference;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class InventoryItemPhotoData implements Serializable {
    public static int MAX_SIZE = 0x69 + PhotoMetadata.MAX_SIZE + (PhotoUser.MAX_SIZE * 4);
    
    public ResourceReference icon;
    public ResourceReference sticker;
    public PhotoMetadata photoMetadata = new PhotoMetadata();
    public ResourceReference painting;

    public InventoryItemPhotoData serialize(Serializer serializer, Serializable structure) {
        InventoryItemPhotoData data = (structure == null) ? new InventoryItemPhotoData() : (InventoryItemPhotoData) structure;
        
        data.icon = serializer.resource(data.icon, ResourceType.TEXTURE, true);
        data.sticker = serializer.resource(data.sticker, ResourceType.TEXTURE, true);
        data.photoMetadata = serializer.struct(data.photoMetadata, PhotoMetadata.class);
        if (serializer.revision.head > 0x3c7)
            data.painting = serializer.resource(data.painting, ResourceType.PAINTING, true);
        
        return data;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof InventoryItemPhotoData)) return false;
        InventoryItemPhotoData d = (InventoryItemPhotoData)o;
        return (
                icon.equals(d.icon) &&
                sticker.equals(d.sticker) &&
                photoMetadata.equals(d.photoMetadata) &&
                painting.equals(d.painting)
        );
    }
}
