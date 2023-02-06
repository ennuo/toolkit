package cwlib.structs.adventure;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;

public class SlotPhotoData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    SlotID id;
    ResourceDescriptor[] photos;

    @SuppressWarnings("unchecked")
    @Override public SlotPhotoData serialize(Serializer serializer, Serializable structure) {
        SlotPhotoData data = (structure == null) ? new SlotPhotoData() : (SlotPhotoData) structure;

        data.id = serializer.struct(data.id, SlotID.class);
        int numPhotos = serializer.i32(data.photos != null ? data.photos.length : 0);
        if (!serializer.isWriting()) 
            data.photos = new ResourceDescriptor[numPhotos];
        for (int i = 0; i < numPhotos; ++i)
            data.photos[i] = serializer.resource(data.photos[i], ResourceType.PLAN, true);
        
        return data;
    }

    @Override public int getAllocatedSize() {
        int size = SlotPhotoData.BASE_ALLOCATION_SIZE;
        if (this.photos != null)
            size += (this.photos.length * 0x24);
        return size;
    }
}
