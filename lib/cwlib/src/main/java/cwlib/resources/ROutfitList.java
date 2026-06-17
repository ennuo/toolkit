package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.inventory.Outfit;
import cwlib.types.data.Revision;

public class ROutfitList extends ArrayList<Outfit> implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    @Override
    public void serialize(Serializer serializer) 
    {
        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            stream.i32(size());
            for (Outfit outfit : this)
                serializer.struct(outfit, Outfit.class);
        }
        else
        {
            MemoryInputStream stream = serializer.getInput();
            int count = stream.i32();
            for (int i = 0; i < count; ++i)
                add(serializer.struct(null, Outfit.class));
        }
    }

    @Override
    public int getAllocatedSize() 
    {
        int size = BASE_ALLOCATION_SIZE;
        for (Outfit outfit : this)
            size += outfit.getAllocatedSize();
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, ROutfitList.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.OUTFIT_LIST,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }
}
