package cwlib.structs.streaming;

import java.util.ArrayList;

import org.joml.Vector3f;

import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RPlan;
import cwlib.structs.profile.CollectableData;
import cwlib.structs.things.Thing;
import cwlib.types.SerializedResource;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;

public class StreamingIsland implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public int TimeZone;
    public int Flags;
    public Vector3f Min = new Vector3f().zero();
    public Vector3f Max = new Vector3f().zero();
    public byte[] PlanData = {};

    public ArrayList<StreamingCheckpoint> CheckpointList = new ArrayList<>();
    public ArrayList<QuestTracker> QuestTrackerList = new ArrayList<>();
    public ArrayList<QuestSwitch> QuestSwitchList = new ArrayList<>();
    public ArrayList<CollectableData> CollectablesList = new ArrayList<>();

    public ArrayList<GUID> GUIDs = new ArrayList<>();
    public ArrayList<SHA1> Hashes = new ArrayList<>();

    @Override public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        // This resource wasn't added until this revision
        if (subVersion <= 0x3e) return;

        if (subVersion < 0x59) throw new SerializationException("Unsupported RStreamingChunk revision!");

        TimeZone = serializer.i32(TimeZone);
        Flags = serializer.i32(Flags);
        Min = serializer.v3(Min);
        Max = serializer.v3(Max);
        PlanData = serializer.bytearray(PlanData);

        if (subVersion > 0x74)
            CheckpointList = serializer.arraylist(CheckpointList, StreamingCheckpoint.class);
        if (subVersion > 0xeb)
            QuestTrackerList = serializer.arraylist(QuestTrackerList, QuestTracker.class);
        if (subVersion > 0x118)
            QuestSwitchList = serializer.arraylist(QuestSwitchList, QuestSwitch.class);
        if (subVersion > 0x11d)
            CollectablesList = serializer.arraylist(CollectablesList, CollectableData.class);


        if (subVersion >= 0x16a)
        {
            GUIDs = serializer.guidlist(GUIDs);
            Hashes = serializer.hashlist(Hashes);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (PlanData != null) size += PlanData.length;



        size += GUIDs.size() * 0x5;
        size += Hashes.size() * 0x14;

        return size;
    }
}
