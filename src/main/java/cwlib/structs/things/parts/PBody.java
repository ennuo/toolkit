package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

/**
 * Part that essentially works as a
 * physics rigidbody in the world.
 */
public class PBody implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    /**
     * Positional velocity
     */
    public Vector3f posVel;

    /**
     * Angular velocity
     */
    public float angVel;

    /**
     * The state of this object
     */
    public int frozen;

    /**
     * The player that's currently editing this thing(?)
     */
    public Thing editingPlayer;

    @SuppressWarnings("unchecked")
    @Override public PBody serialize(Serializer serializer, Serializable structure) {
        PBody body = (structure == null) ? new PBody() : (PBody) structure;

        int head = serializer.getRevision().getVersion();

        body.posVel = serializer.v3(body.posVel);
        body.angVel = serializer.f32(body.angVel);
        if (head >= 0x147)
            body.frozen = serializer.i32(body.frozen);
        if (head >= 0x22c)
            body.editingPlayer = serializer.reference(body.editingPlayer, Thing.class);

        return body;
    }

    @Override public int getAllocatedSize() { return PBody.BASE_ALLOCATION_SIZE; }
}
