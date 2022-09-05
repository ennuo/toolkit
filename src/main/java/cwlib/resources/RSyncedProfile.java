package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.Revision;

public class RSyncedProfile implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int timePlayed;
    public Thing platonicAvatar;
    public long uniqueNumber;

    public int primary, secondary, tertiary;

    public NetworkPlayerID playerID;
    
    @SuppressWarnings("unchecked")
    @Override public RSyncedProfile serialize(Serializer serializer, Serializable structure) {
        RSyncedProfile profile = (structure == null) ? new RSyncedProfile() : (RSyncedProfile) structure;

        int version = serializer.getRevision().getVersion();

        profile.timePlayed = serializer.i32(profile.timePlayed);
        profile.platonicAvatar = serializer.thing(profile.platonicAvatar);

        if (version < 0x13e)
            serializer.resource(null, ResourceType.TEXTURE);
        
        profile.timePlayed = serializer.i32(profile.timePlayed);
        profile.uniqueNumber = serializer.i64(profile.uniqueNumber);

        if (version > 0x163) {
            profile.primary = serializer.i32(profile.primary);
            profile.secondary = serializer.i32(profile.secondary);
            profile.tertiary = serializer.i32(profile.tertiary);
        }

        if (version > 0x1a7)
            profile.playerID = serializer.struct(profile.playerID, NetworkPlayerID.class);

        if (version > 0x1c4 && version < 0x213)
            serializer.i32(0);

        return profile;
    }

    @Override public int getAllocatedSize() { 
        return BASE_ALLOCATION_SIZE;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
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
