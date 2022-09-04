package cwlib.structs.things.parts;

import cwlib.enums.GroupFlags;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.ResourceDescriptor;

/**
 * Part that represents a grouping of Things.
 */
public class PGroup implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    @Deprecated public transient Thing[] things;

    @GsonRevision(min=0x25e)
    public ResourceDescriptor planDescriptor;

    @GsonRevision(min=0x18e)
    public NetworkPlayerID creator = new NetworkPlayerID();

    @GsonRevision(min=0x267)
    public Thing emitter;

    @GsonRevision(min=0x267)
    public int lifetime, aliveFrames;

    public int flags;

    @GsonRevision(min=0x30f,max=340)
    @Deprecated public boolean mainSelectableObject;

    @SuppressWarnings("unchecked")
    @Override public PGroup serialize(Serializer serializer, Serializable structure) {
        PGroup group = (structure == null) ? new PGroup() : (PGroup) structure;

        int version = serializer.getRevision().getVersion();
        boolean isWriting = serializer.isWriting();

        if (!(version < 0x18e || version > 0x1b0))
            group.things = serializer.array(group.things, Thing.class, true);

        if (version >= 0x18e && version < 0x341) {
            if (isWriting) serializer.getOutput().bool((group.flags & GroupFlags.COPYRIGHT) != 0);
            else if (serializer.getInput().bool())
                group.flags |= GroupFlags.COPYRIGHT;
        }

        if (version >= 0x18e)
            group.creator = serializer.struct(group.creator, NetworkPlayerID.class);

        if (version >= 0x25e || version == 0x252)
            group.planDescriptor = serializer.resource(group.planDescriptor, ResourceType.PLAN, true);

        if (version >= 0x25e && version < 0x341) {
            if (isWriting) serializer.getOutput().bool((group.flags & GroupFlags.EDITABLE) != 0);
            else if (serializer.getInput().bool())
                group.flags |= GroupFlags.EDITABLE;
        }

        if (version >= 0x267) {
            group.emitter = serializer.reference(group.emitter, Thing.class);
            group.lifetime = serializer.i32(group.lifetime);
            group.aliveFrames = serializer.i32(group.aliveFrames);
        }

        if (version >= 0x26e && version < 0x341) {
            if (isWriting) serializer.getOutput().bool((group.flags & GroupFlags.PICKUP_ALL_MEMBERS) != 0);
            else if (serializer.getInput().bool())
                group.flags |= GroupFlags.PICKUP_ALL_MEMBERS;
        }

        if (version >= 0x30f && version < 0x341)
            group.mainSelectableObject = serializer.bool(group.mainSelectableObject);

        if (version >= 0x341)
            group.flags = serializer.u8(group.flags);

        return group;
    }

    @Override public int getAllocatedSize() {  return PGroup.BASE_ALLOCATION_SIZE; }
}