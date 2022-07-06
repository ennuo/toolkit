package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

import org.joml.Matrix4f;

/**
 * This part represents a Thing's
 * position in the world, as well
 * as information about a bone.
 */
public class PPos implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public Thing thingOfWhichIAmABone;
    public int animHash;
    public Matrix4f localPosition;
    public Matrix4f worldPosition;

    @SuppressWarnings("unchecked")
    @Override public PPos serialize(Serializer serializer, Serializable structure) {
        PPos pos = (structure == null) ? new PPos() : (PPos) structure;
        
        int version = serializer.getRevision().getVersion();

        pos.thingOfWhichIAmABone = serializer.reference(pos.thingOfWhichIAmABone, Thing.class);
        pos.animHash = serializer.i32(pos.animHash);
        
        if (version <= 0x340)
            pos.localPosition = serializer.m44(pos.localPosition);

        pos.worldPosition = serializer.m44(pos.worldPosition);

        // Unknown value, depreciated very early
        if (version < 0x155)
            serializer.i32(0);
        
        return pos;
    }
    
    @Override public int getAllocatedSize() { return PPos.BASE_ALLOCATION_SIZE; }
}
