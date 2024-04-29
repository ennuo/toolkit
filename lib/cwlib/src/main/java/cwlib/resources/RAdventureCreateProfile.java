package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.structs.adventure.PlanetArea;
import cwlib.structs.adventure.SlotPhotoData;
import cwlib.structs.level.AdventureData;
import cwlib.structs.profile.PlayedLevelData;
import cwlib.structs.profile.SlotLink;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.structs.things.Thing;
import cwlib.io.Resource;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;

import java.util.HashMap;
import java.util.Set;

public class RAdventureCreateProfile implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public HashMap<SlotID, Slot> adventureSlots = new HashMap<>();
    public SlotID adventureID;

    @GsonRevision(lbp3 = true, min = 0x138)
    public Thing planetWorld;

    @GsonRevision(lbp3 = true, min = 0x71, max = 0x136)
    @Deprecated
    public ResourceDescriptor planetLevel;

    @GsonRevision(lbp3 = true, min = 0xd5)
    public SlotLink[] visibleLinks;

    @GsonRevision(lbp3 = true, min = 0x10a)
    PlanetArea[] planetAreas;

    @GsonRevision(lbp3 = true, min = 0x11c)
    public PlayedLevelData[] startingPlayedLevelData;

    @GsonRevision(lbp3 = true, max = 0x13f)
    @Deprecated
    public AdventureData sharedData;

    @GsonRevision(lbp3 = true, min = 0x140)
    public ResourceDescriptor sharedDataDescriptor;

    @GsonRevision(lbp3 = true, min = 0x175)
    SlotPhotoData[] photoList;

    @GsonRevision(lbp3 = true, min = 0x17e)
    public SlotID startLevelSlotID;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x55)
        {
            if (serializer.isWriting())
            {
                Set<SlotID> keys = adventureSlots.keySet();
                serializer.getOutput().i32(keys.size());
                for (SlotID key : keys)
                {
                    serializer.struct(key, SlotID.class);
                    serializer.struct(adventureSlots.get(key), Slot.class);
                }
            }
            else
            {
                int count = serializer.getInput().i32();
                adventureSlots = new HashMap<SlotID, Slot>(count);
                for (int i = 0; i < count; ++i)
                    adventureSlots.put(
                        serializer.struct(null, SlotID.class),
                        serializer.struct(null, Slot.class));
            }

            adventureID = serializer.struct(adventureID, SlotID.class);
        }

        if (subVersion > 0x137)
            planetWorld = serializer.thing(planetWorld);
        if (subVersion >= 0x71 && subVersion < 0x137)
            serializer.resource(null, ResourceType.LEVEL, true);

        if (subVersion > 0xd4)
            visibleLinks = serializer.array(visibleLinks, SlotLink.class);

        if (subVersion > 0x109)
            planetAreas = serializer.array(planetAreas, PlanetArea.class);

        if (subVersion > 0x11b)
            startingPlayedLevelData = serializer.array(startingPlayedLevelData,
                PlayedLevelData.class);

        if (subVersion < 0x140)
            sharedData = serializer.struct(sharedData, AdventureData.class);
        else
            sharedDataDescriptor = serializer.resource(sharedDataDescriptor,
                ResourceType.ADVENTURE_SHARED_DATA, true);

        if (subVersion >= 0xee && subVersion < 0xf1)
        {
            // I'm fairly sure it's mostly the same as modern adventure data
            // just with different revisions?
            throw new SerializationException("Legacy adventure data not supported in " +
                                             "serialization!");
        }

        if (subVersion > 0x174)
            photoList = serializer.array(photoList, SlotPhotoData.class);

        if (subVersion >= 0x17e)
            startLevelSlotID = serializer.struct(startLevelSlotID, SlotID.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = RAdventureCreateProfile.BASE_ALLOCATION_SIZE;
        for (Slot slot : this.adventureSlots.values())
            size += slot.getAllocatedSize() + SlotID.BASE_ALLOCATION_SIZE;
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(0x1000000, revision, compressionFlags);
        serializer.struct(this, RAdventureCreateProfile.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.ADVENTURE_CREATE_PROFILE,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }

    public HashMap<SlotID, Slot> getAdventureSlots()
    {
        return this.adventureSlots;
    }
}
