package cwlib.structs.streaming;

import java.util.ArrayList;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.profile.CollectableData;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;

public class ChunkFile implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public SHA1 chunkHash;
    public ArrayList<StreamingCheckpoint> checkpointList = new ArrayList<>();
    public ArrayList<QuestTracker> questTrackerList = new ArrayList<>();
    public ArrayList<QuestSwitch> questSwitchList = new ArrayList<>();
    public ArrayList<CollectableData> collectablesList = new ArrayList<>();
    public ArrayList<ResourceDescriptor> userResources = new ArrayList<>();
    public Vector3f min, max;
    public boolean hasObjectSaver;
    public boolean deleteObjectSavers;
    public boolean deleteOtherThings;
    public boolean antiStreaming;
    public ArrayList<GUID> guids = new ArrayList<>();
    public ArrayList<SHA1> hashes = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public ChunkFile serialize(Serializer serializer, Serializable structure) {
        ChunkFile chunk = (structure == null) ? new ChunkFile() : (ChunkFile) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion >= 0x73 && subVersion <= 0x130) {
            ResourceDescriptor descriptor = null;
            if (serializer.isWriting()) {
                if (chunk.chunkHash != null)
                    descriptor = new ResourceDescriptor(chunk.chunkHash, ResourceType.STREAMING_CHUNK);
            }
            descriptor = serializer.resource(descriptor, ResourceType.STREAMING_CHUNK, true);
            if (!serializer.isWriting()) {
                if (descriptor != null && descriptor.isHash())
                    chunk.chunkHash = descriptor.getSHA1();
                else
                    chunk.chunkHash = SHA1.EMPTY;
            }
        }

        if (subVersion > 0x130) {
            chunk.chunkHash = serializer.sha1(chunk.chunkHash);
            if (serializer.isWriting() && chunk.chunkHash != null)
                serializer.addDependency(new ResourceDescriptor(chunk.chunkHash, ResourceType.STREAMING_CHUNK));
        }

        if (subVersion > 0x72)
            chunk.checkpointList = serializer.arraylist(chunk.checkpointList, StreamingCheckpoint.class);

        if (subVersion > 0xeb)
            chunk.questTrackerList = serializer.arraylist(chunk.questTrackerList, QuestTracker.class);

        if (subVersion > 0x118)
            chunk.questSwitchList = serializer.arraylist(chunk.questSwitchList, QuestSwitch.class);

        if (subVersion > 0x11d)
            chunk.collectablesList = serializer.arraylist(chunk.collectablesList, CollectableData.class);

        if (subVersion >= 0xde) {
            int numItems = serializer.i32(chunk.userResources != null ? chunk.userResources.size() : 0);
            if (serializer.isWriting()) {
                for (ResourceDescriptor descriptor : chunk.userResources)
                    serializer.resource(descriptor, descriptor.getType(), true, false, true);
            } else {
                chunk.userResources = new ArrayList<>(numItems);
                for (int i = 0; i < numItems; ++i)
                    chunk.userResources.add(serializer.resource(null, null, true, false, true));
            }
        }

        if (subVersion > 0x72) {
            chunk.min = serializer.v3(chunk.min);
            chunk.max = serializer.v3(chunk.max);
        }

        if (subVersion > 0x10d) {
            chunk.hasObjectSaver = serializer.bool(chunk.hasObjectSaver);
            chunk.deleteObjectSavers = serializer.bool(chunk.deleteObjectSavers);
            chunk.deleteOtherThings = serializer.bool(chunk.deleteOtherThings);
        }

        if (subVersion > 0x133)
            chunk.antiStreaming = serializer.bool(chunk.antiStreaming);

        if (subVersion > 0x169) {

            {
                int numItems = serializer.i32(chunk.guids != null ? chunk.guids.size() : 0);
                if (serializer.isWriting()) {
                    for (GUID guid : chunk.guids)
                        serializer.getOutput().guid(guid);
                } else {
                    chunk.guids = new ArrayList<>(numItems);
                    for (int i = 0; i < numItems; ++i)
                        chunk.guids.add(serializer.getInput().guid());
                }
            }

            {
                int numItems = serializer.i32(chunk.hashes != null ? chunk.hashes.size() : 0);
                if (serializer.isWriting()) {
                    for (SHA1 sha1 : chunk.hashes)
                        serializer.getOutput().sha1(sha1);
                } else {
                    chunk.hashes = new ArrayList<>(numItems);
                    for (int i = 0; i < numItems; ++i)
                        chunk.hashes.add(serializer.getInput().sha1());
                }
            }
        }

        return chunk;
    }

    @Override public int getAllocatedSize() {
        return ChunkFile.BASE_ALLOCATION_SIZE;
    }
    
}
