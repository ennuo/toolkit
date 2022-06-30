package cwlib.structs.things.parts;

import cwlib.enums.GroupFlags;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.ResourceDescriptor;

/**
 * Part that represents a grouping of Things.
 */
public class PGroup implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public ResourceDescriptor planDescriptor;
    public NetworkPlayerID creator = new NetworkPlayerID();
    public Thing emitter;
    public int lifetime, aliveFrames;
    public int flags;

    @SuppressWarnings("unchecked")
    @Override public PGroup serialize(Serializer serializer, Serializable structure) {
        PGroup group = (structure == null) ? new PGroup() : (PGroup) structure;

        int version = serializer.getRevision().getVersion();
        boolean isWriting = serializer.isWriting();

        if (version >= 0x18e && version <= 0x340) {
            if (isWriting) serializer.getOutput().bool((group.flags & GroupFlags.COPYRIGHT) != 0);
            else if (serializer.getInput().bool())
                group.flags |= GroupFlags.COPYRIGHT;
        }

        if (version >= 0x235)
            group.creator = serializer.struct(group.creator, NetworkPlayerID.class);

        group.planDescriptor = serializer.resource(group.planDescriptor, ResourceType.PLAN, true);

        if (version >= 0x25e && version <= 0x340) {
            if (isWriting) serializer.getOutput().bool((group.flags & GroupFlags.EDITABLE) != 0);
            else if (serializer.getInput().bool())
                group.flags |= GroupFlags.EDITABLE;
        }

        if (version >= 0x267) {
            group.emitter = serializer.reference(group.emitter, Thing.class);
            group.lifetime = serializer.i32(group.lifetime);
            group.aliveFrames = serializer.i32(group.aliveFrames);
        }

        if (version >= 0x26e && version <= 0x340) {
            if (isWriting) serializer.getOutput().bool((group.flags & GroupFlags.PICKUP_ALL_MEMBERS) != 0);
            else if (serializer.getInput().bool())
                group.flags |= GroupFlags.PICKUP_ALL_MEMBERS;
        }

        // PGroup.MainSelectableObject, not valid past this revision
        if (version >= 0x30f && version <= 0x340)
            serializer.bool(false);

        if (version >= 0x341)
            group.flags = serializer.u8(group.flags);

        return group;
    }

    @Override public int getAllocatedSize() {  return PGroup.BASE_ALLOCATION_SIZE; }
}