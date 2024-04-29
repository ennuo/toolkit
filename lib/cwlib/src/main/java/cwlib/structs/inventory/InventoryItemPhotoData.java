package cwlib.structs.inventory;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class InventoryItemPhotoData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public ResourceDescriptor icon, sticker;
    private PhotoMetadata photoMetadata = new PhotoMetadata();
    @GsonRevision(min = 0x3c8)
    public ResourceDescriptor painting;

    @Override
    public void serialize(Serializer serializer)
    {
        icon = serializer.resource(icon, ResourceType.TEXTURE, true);
        sticker = serializer.resource(sticker, ResourceType.TEXTURE, true);
        photoMetadata = serializer.struct(photoMetadata, PhotoMetadata.class);
        if (serializer.getRevision().getVersion() > 0x3c7)
            painting = serializer.resource(painting, ResourceType.PAINTING, true);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE + this.photoMetadata.getAllocatedSize();
    }

    public PhotoMetadata getPhotoMetadata()
    {
        return this.photoMetadata;
    }

    public void setPhotoMetadata(PhotoMetadata value)
    {
        if (value == null)
            throw new NullPointerException("Photo metadata cannot be nulL!");
        this.photoMetadata = value;
    }
}
