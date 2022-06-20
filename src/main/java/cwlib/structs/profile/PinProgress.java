package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PinProgress implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int progressType, progressCount;

    @SuppressWarnings("unchecked")
    @Override public PinProgress serialize(Serializer serializer, Serializable structure) {
        PinProgress progress = (structure == null) ? new PinProgress() : (PinProgress) structure;

        progress.progressType = serializer.i32(progress.progressType);
        progress.progressCount = serializer.i32(progress.progressCount);

        return progress;
    }

    @Override public int getAllocatedSize() { return PinProgress.BASE_ALLOCATION_SIZE; }
}
