package cwlib.structs.streaming;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class StreamingManager implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Thing[] editingThingsList;
    public LevelData[] levelData;
    public int numIslands;
    public int numPendingIslands;
    public ResourceDescriptor fartDesc;
    public Vector3f startingPointPosition;
    public int streamingZoneShape;
    public int streamingZoneSize;
    public int numInUsePlans;
    public int maintainIslandsCount;
    public Thing[][] maintainIslandList;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion < 0xa1)
            throw new SerializationException("Streaming manager below 0xa1 not supported!");

        if (subVersion >= 0x46)
            editingThingsList = serializer.thingarray(editingThingsList);

        if (subVersion < 0x4d)
        {
            // 00c57988 - islands
        }

        if (subVersion >= 0x4d && subVersion < 0x73)
        {
            serializer.i32(0); // numChunks
            serializer.i32(0); // numChunks
        }

        if (subVersion >= 0x73 && subVersion < 0x93)
        {
            // serializer.array(null, ChunkFile.class, true);
        }

        if (subVersion >= 0x93 && subVersion < 0xa1)
        {
            if (serializer.isWriting())
            {
                LevelData data = null;
                if (levelData != null && levelData.length != 0)
                    data = levelData[0];
                serializer.struct(data, LevelData.class);
            }
            else
                levelData = new LevelData[] { serializer.struct(null, LevelData.class) };
        }

        if (subVersion > 0xa0)
            levelData = serializer.array(levelData, LevelData.class, true);

        if (subVersion > 0x76)
            numIslands = serializer.i32(numIslands);
        if (subVersion > 0x1ff)
            numPendingIslands = serializer.i32(numPendingIslands);

        if (subVersion >= 0x4e && subVersion <= 0x7e)
            serializer.v3(null);

        if (subVersion >= 0x89)
            fartDesc = serializer.resource(fartDesc, ResourceType.FILE_OF_BYTES,
                true);

        if (subVersion >= 0x8f)
            startingPointPosition = serializer.v3(startingPointPosition);

        if (subVersion >= 0x160)
        {
            streamingZoneShape = serializer.i32(streamingZoneShape);
            streamingZoneSize = serializer.i32(streamingZoneSize);

            numInUsePlans = serializer.i32(numInUsePlans);
            if (subVersion >= 0x1a9)
                maintainIslandsCount = serializer.i32(maintainIslandsCount);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = StreamingManager.BASE_ALLOCATION_SIZE;
        return size;
    }
}
