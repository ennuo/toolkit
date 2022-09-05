package cwlib.structs.things.components.poppet;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PoppetMode implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x20;

    public int mode, subMode;

    @SuppressWarnings("unchecked")
    @Override public PoppetMode serialize(Serializer serializer, Serializable structure) {
        PoppetMode mode = (structure == null) ? new PoppetMode() : (PoppetMode) structure;

        int version = serializer.getRevision().getVersion();
        
        mode.mode = serializer.i32(mode.mode);
        mode.subMode = serializer.i32(mode.subMode);

        if (version < 0x18b) serializer.i32(0);
        if (version > 0x1b7 && version < 0x1ba) {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }

        return mode;
    }


    @Override public int getAllocatedSize() { return PoppetMode.BASE_ALLOCATION_SIZE; }
    

}