package cwlib.structs.adventure;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;

public class SlotPhotoData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    SlotID id;
    ResourceDescriptor[] photos;

    @Override
    public void serialize(Serializer serializer)
    {

        id = serializer.struct(id, SlotID.class);
        int numPhotos = serializer.i32(photos != null ? photos.length : 0);
        if (!serializer.isWriting())
            photos = new ResourceDescriptor[numPhotos];
        for (int i = 0; i < numPhotos; ++i)
            photos[i] = serializer.resource(photos[i], ResourceType.PLAN, true);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = SlotPhotoData.BASE_ALLOCATION_SIZE;
        if (this.photos != null)
            size += (this.photos.length * 0x24);
        return size;
    }
}
