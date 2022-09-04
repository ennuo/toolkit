package cwlib.structs.inventory;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class InventoryItemPhotoData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public ResourceDescriptor icon, sticker;
    private PhotoMetadata photoMetadata = new PhotoMetadata();
    @GsonRevision(min=0x3c8)
    public ResourceDescriptor painting;

    @SuppressWarnings("unchecked")
    @Override public InventoryItemPhotoData serialize(Serializer serializer, Serializable structure) {
        InventoryItemPhotoData data = 
            (structure == null) ? new InventoryItemPhotoData() : (InventoryItemPhotoData) structure;

        data.icon = serializer.resource(data.icon, ResourceType.TEXTURE, true);
        data.sticker = serializer.resource(data.sticker, ResourceType.TEXTURE, true);
        data.photoMetadata = serializer.struct(data.photoMetadata, PhotoMetadata.class);
        if (serializer.getRevision().getVersion() > 0x3c7)
            data.painting = serializer.resource(data.painting, ResourceType.PAINTING, true);


        return data;
    }

    @Override public int getAllocatedSize() { 
        return BASE_ALLOCATION_SIZE + this.photoMetadata.getAllocatedSize();
    }

    public PhotoMetadata getPhotoMetadata() { return this.photoMetadata; }
    public void setPhotoMetadata(PhotoMetadata value) { 
        if (value == null)
            throw new NullPointerException("Photo metadata cannot be nulL!");
        this.photoMetadata = value; 
    }
}
