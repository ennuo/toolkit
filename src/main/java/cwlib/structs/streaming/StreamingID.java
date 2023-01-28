package cwlib.structs.streaming;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class StreamingID implements Serializable { 
    public static final int BASE_ALLOCATION_SIZE = 0x30;
    
    public String name;
    public int type;

    @SuppressWarnings("unchecked")
    @Override public StreamingID serialize(Serializer serializer, Serializable structure) {
        StreamingID id = (structure == null) ? new StreamingID() : (StreamingID) structure;

        id.name = serializer.wstr(id.name);
        id.type = serializer.i32(id.type);

        return id;
    }

    @Override public int getAllocatedSize() {
        int size = StreamingID.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length() * 0x2);
        return size;
    }
}
