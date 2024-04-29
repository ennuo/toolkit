package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class RSyncedProfile implements Resource
{
    public static class CreatureAvatar implements Serializable
    {
        public static int BASE_ALLOCATION_SIZE = 0x48;

        public int creature;
        public ResourceDescriptor mainFormCostume;
        public ResourceDescriptor altFormCostume;

        @Override
        public void serialize(Serializer serializer)
        {
            creature = serializer.adventureCreatureReference(creature);
            mainFormCostume = serializer.resource(mainFormCostume, ResourceType.PLAN, true);
            altFormCostume = serializer.resource(altFormCostume, ResourceType.PLAN, true);
        }

        @Override
        public int getAllocatedSize()
        {
            return BASE_ALLOCATION_SIZE;
        }
    }


    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int timePlayed;
    public Thing sackboyAvatarWorld;
    public long uniqueNumber;

    public int primary, secondary, tertiary, emphasis;

    public ArrayList<CreatureAvatar> creatureAvatars = new ArrayList<>(); // + 0x78
    public NetworkPlayerID playerID;
    public int creatureToSpawnAs; // 0xd8
    public boolean spawnAsAlternateForm; // + 200
    public int creatureToPodAs; // + 0xdc
    public boolean podAsAlternateForm; // + 0xc9

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        timePlayed = serializer.i32(timePlayed);
        sackboyAvatarWorld = serializer.thing(sackboyAvatarWorld);

        if (subVersion > 0x17)
            creatureAvatars = serializer.arraylist(creatureAvatars, CreatureAvatar.class);

        if (version < 0x193)
            serializer.array(null, Thing.class, true);

        if (version < 0x13e)
            serializer.resource(null, ResourceType.TEXTURE);

        timePlayed = serializer.i32(timePlayed);
        uniqueNumber = serializer.u64(uniqueNumber);

        if (version > 0x163)
        {
            primary = serializer.i32(primary);
            secondary = serializer.i32(secondary);
            tertiary = serializer.i32(tertiary);
            if (version > 0x3a6)
                emphasis = serializer.i32(emphasis);
        }

        if (version > 0x1a7)
            playerID = serializer.struct(playerID, NetworkPlayerID.class);

        if (version > 0x1c4 && version < 0x213)
            serializer.i32(0);

        if (subVersion >= 0x9e)
            creatureToSpawnAs = serializer.adventureCreatureReference(creatureToSpawnAs);
        if (subVersion >= 0x18a)
            spawnAsAlternateForm = serializer.bool(spawnAsAlternateForm);
        if (subVersion >= 0x18d)
        {
            creatureToPodAs = serializer.adventureCreatureReference(creatureToSpawnAs);
            podAsAlternateForm = serializer.bool(podAsAlternateForm);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        // 16MB buffer for generation of levels, since the allocated size will get
        // stuck in a recursive loop until I fix it.
        Serializer serializer = new Serializer(0x1000000, revision, compressionFlags);
        serializer.struct(this, RSyncedProfile.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.SYNCED_PROFILE,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }
}
