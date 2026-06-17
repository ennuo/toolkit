package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.streaming.StreamingIsland;
import cwlib.types.data.Revision;

public class RStreamingChunk implements Resource {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public ArrayList<StreamingIsland> IslandList = new ArrayList<>();
    public int[] IslandChunkCodeList = {};

    @Override 
    public void serialize(Serializer serializer)
    {
        IslandList = serializer.arraylist(IslandList, StreamingIsland.class, true);
        IslandChunkCodeList = serializer.intvector(IslandChunkCodeList);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;

        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RStreamingChunk.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.STREAMING_CHUNK,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }
}
