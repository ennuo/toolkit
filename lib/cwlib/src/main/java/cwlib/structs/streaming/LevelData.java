package cwlib.structs.streaming;

import java.util.ArrayList;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.GUID;

public class LevelData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public SlotID levelSlot;
    public int type;
    public ArrayList<ChunkFile> chunkFileList = new ArrayList<>();
    public Vector3f offset, min, max;
    public LevelData parent;
    public GUID farcGuid;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x92)
            levelSlot = serializer.struct(levelSlot, SlotID.class);

        if (subVersion >= 0x93 && subVersion < 0xa1)
            serializer.i32(0);

        if (subVersion > 0x9a)
            type = serializer.i32(type);

        if (subVersion >= 0x93)
            chunkFileList = serializer.arraylist(chunkFileList, ChunkFile.class, true);

        if (subVersion > 0x92)
            offset = serializer.v3(offset);
        if (subVersion > 0xa0)
        {
            min = serializer.v3(min);
            max = serializer.v3(max);
        }

        if (subVersion > 0xab)
            parent = serializer.reference(parent, LevelData.class);

        if (subVersion < 0x93 || subVersion > 0xa0)
        {
            if (subVersion >= 0x1ac)
                farcGuid = serializer.guid(farcGuid);
        }
        else serializer.reference(null, LevelData.class);
    }

    @Override
    public int getAllocatedSize()
    {
        return LevelData.BASE_ALLOCATION_SIZE;
    }

}
