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

public class ChunkFile implements Serializable
{
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

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion >= 0x73 && subVersion <= 0x130)
        {
            ResourceDescriptor descriptor = null;
            if (serializer.isWriting())
            {
                if (chunkHash != null)
                    descriptor = new ResourceDescriptor(chunkHash,
                        ResourceType.STREAMING_CHUNK);
            }
            descriptor = serializer.resource(descriptor, ResourceType.STREAMING_CHUNK, true);
            if (!serializer.isWriting())
            {
                if (descriptor != null && descriptor.isHash())
                    chunkHash = descriptor.getSHA1();
                else
                    chunkHash = SHA1.EMPTY;
            }
        }

        if (subVersion > 0x130)
        {
            chunkHash = serializer.sha1(chunkHash);
            if (serializer.isWriting() && chunkHash != null)
                serializer.addDependency(new ResourceDescriptor(chunkHash,
                    ResourceType.STREAMING_CHUNK));
        }

        if (subVersion > 0x72)
            checkpointList = serializer.arraylist(checkpointList,
                StreamingCheckpoint.class);

        if (subVersion > 0xeb)
            questTrackerList = serializer.arraylist(questTrackerList,
                QuestTracker.class);

        if (subVersion > 0x118)
            questSwitchList = serializer.arraylist(questSwitchList, QuestSwitch.class);

        if (subVersion > 0x11d)
            collectablesList = serializer.arraylist(collectablesList,
                CollectableData.class);

        if (subVersion >= 0xde)
        {
            int numItems = serializer.i32(userResources != null ?
                userResources.size() : 0);
            if (serializer.isWriting())
            {
                for (ResourceDescriptor descriptor : userResources)
                    serializer.resource(descriptor, descriptor.getType(), true, false,
                        true);
            }
            else
            {
                userResources = new ArrayList<>(numItems);
                for (int i = 0; i < numItems; ++i)
                    userResources.add(serializer.resource(null, null, true, false, true));
            }
        }

        if (subVersion > 0x72)
        {
            min = serializer.v3(min);
            max = serializer.v3(max);
        }

        if (subVersion > 0x10d)
        {
            hasObjectSaver = serializer.bool(hasObjectSaver);
            deleteObjectSavers = serializer.bool(deleteObjectSavers);
            deleteOtherThings = serializer.bool(deleteOtherThings);
        }

        if (subVersion > 0x133)
            antiStreaming = serializer.bool(antiStreaming);

        if (subVersion > 0x169)
        {

            {
                int numItems = serializer.i32(guids != null ? guids.size() : 0);
                if (serializer.isWriting())
                {
                    for (GUID guid : guids)
                        serializer.getOutput().guid(guid);
                }
                else
                {
                    guids = new ArrayList<>(numItems);
                    for (int i = 0; i < numItems; ++i)
                        guids.add(serializer.getInput().guid());
                }
            }

            {
                int numItems = serializer.i32(hashes != null ? hashes.size() : 0);
                if (serializer.isWriting())
                {
                    for (SHA1 sha1 : hashes)
                        serializer.getOutput().sha1(sha1);
                }
                else
                {
                    hashes = new ArrayList<>(numItems);
                    for (int i = 0; i < numItems; ++i)
                        hashes.add(serializer.getInput().sha1());
                }
            }
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return ChunkFile.BASE_ALLOCATION_SIZE;
    }

}
