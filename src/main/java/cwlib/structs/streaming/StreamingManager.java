package cwlib.structs.streaming;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class StreamingManager implements Serializable { 
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
    
    @SuppressWarnings("unchecked")
    @Override public StreamingManager serialize(Serializer serializer, Serializable structure) {
        StreamingManager manager = (structure == null) ? new StreamingManager() : (StreamingManager) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion < 0xa1)
            throw new SerializationException("Streaming manager below 0xa1 not supported!");
        
        if (subVersion >= 0x46)
            manager.editingThingsList = serializer.thingarray(manager.editingThingsList);

        if (subVersion < 0x4d) {
            // 00c57988 - islands
        }

        if (subVersion >= 0x4d && subVersion < 0x73) {
            serializer.i32(0); // numChunks
            serializer.i32(0); // numChunks
        }

        if (subVersion >= 0x73 && subVersion < 0x93) {
            // serializer.array(null, ChunkFile.class, true);
        }

        if (subVersion >= 0x93 && subVersion < 0xa1) {
            if (serializer.isWriting()) {
                LevelData data = null;
                if (manager.levelData != null && manager.levelData.length != 0)
                    data = manager.levelData[0];
                serializer.struct(data, LevelData.class);
            } else
                manager.levelData = new LevelData[] { serializer.struct(null, LevelData.class) };
        }

        if (subVersion > 0xa0)
            manager.levelData = serializer.array(manager.levelData, LevelData.class, true);

        if (subVersion > 0x76)
            manager.numIslands = serializer.i32(manager.numIslands);
        if (subVersion > 0x1ff)
            manager.numPendingIslands = serializer.i32(manager.numPendingIslands);

        if (subVersion >= 0x4e && subVersion <= 0x7e)
            serializer.v3(null);

        if (subVersion >= 0x89)
            manager.fartDesc = serializer.resource(manager.fartDesc, ResourceType.FILE_OF_BYTES, true);

        if (subVersion >= 0x8f)
            manager.startingPointPosition = serializer.v3(manager.startingPointPosition);

        if (subVersion >= 0x160) {
            manager.streamingZoneShape = serializer.i32(manager.streamingZoneShape);
            manager.streamingZoneSize = serializer.i32(manager.streamingZoneSize);

            manager.numInUsePlans = serializer.i32(manager.numInUsePlans);
            if (subVersion >= 0x1a9)
                manager.maintainIslandsCount = serializer.i32(manager.maintainIslandsCount);
        }
        

        return manager;
    }

    @Override public int getAllocatedSize() {
        int size = StreamingManager.BASE_ALLOCATION_SIZE;
        return size;
    }
}
