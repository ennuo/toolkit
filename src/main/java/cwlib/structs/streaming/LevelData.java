package cwlib.structs.streaming;

import java.util.ArrayList;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.GUID;

public class LevelData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public SlotID levelSlot;
    public int type;
    public ArrayList<ChunkFile> chunkFileList = new ArrayList<>();
    public Vector3f offset, min, max;
    public LevelData parent;
    public GUID farcGuid;

    @SuppressWarnings("unchecked")
    @Override public LevelData serialize(Serializer serializer, Serializable structure) {
        LevelData data = (structure == null) ? new LevelData() : (LevelData) structure;

        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x92)
            data.levelSlot = serializer.struct(data.levelSlot, SlotID.class);

        if (subVersion >= 0x93 && subVersion < 0xa1)
            serializer.i32(0);

        if (subVersion > 0x9a)
            data.type = serializer.i32(data.type);

        if (subVersion >= 0x93) 
            data.chunkFileList = serializer.arraylist(data.chunkFileList, ChunkFile.class, true);
        
        if (subVersion > 0x92)
            data.offset = serializer.v3(data.offset);
        if (subVersion > 0xa0) {
            data.min = serializer.v3(data.min);
            data.max = serializer.v3(data.max);
        }

        if (subVersion > 0xab)
            data.parent = serializer.reference(data.parent, LevelData.class);

        if (subVersion < 0x93 || subVersion > 0xa0) {
            if (subVersion >= 0x1ac)
                data.farcGuid = serializer.guid(data.farcGuid);
        } else serializer.reference(null, LevelData.class);

        return data;
    }

    @Override public int getAllocatedSize() {
        return LevelData.BASE_ALLOCATION_SIZE;
    }
    
}
