package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BroadcastMicrochipEntry implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public Thing sourceMicrochip;
    public Thing clonedMicrochip;

    @SuppressWarnings("unchecked")
    @Override public BroadcastMicrochipEntry serialize(Serializer serializer, Serializable structure) {
        BroadcastMicrochipEntry entry = (structure == null) ? new BroadcastMicrochipEntry() : (BroadcastMicrochipEntry) structure;

        entry.sourceMicrochip = serializer.thing(entry.sourceMicrochip);
        entry.clonedMicrochip = serializer.thing(entry.clonedMicrochip);
        
        return entry;
    }


    @Override public int getAllocatedSize() {
        return BroadcastMicrochipEntry.BASE_ALLOCATION_SIZE;
    }
}
