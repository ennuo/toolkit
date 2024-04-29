package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.structs.things.Thing;
import org.joml.Vector3f;

/**
 * Part that essentially works as a
 * physics rigidbody in the world.
 */
public class PBody implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

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
    @GsonRevision(min = 0x147)
    public int frozen;

    /**
     * The player that's currently editing this thing(?)
     */
    @GsonRevision(min = 0x22c)
    public Thing editingPlayer;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        // A lot of fields were removed in 0x13c, across a lot of structures,
        // so I have no idea what they are, nor they do matter in any
        // version of the game anymore.

        if (version < 0x13c)
            serializer.v3(null);

        posVel = serializer.v3(posVel);

        if (version < 0x13c)
        {
            serializer.f32(0.0f);
            serializer.f32(0.0f);
        }

        angVel = serializer.f32(angVel);

        if (version < 0x13c)
        {
            serializer.f32(0.0f);
            serializer.v3(null);

            if (serializer.isWriting()) serializer.getOutput().i32(0);
            else
            {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                for (int i = 0; i < count; ++i)
                    stream.v3();
            }

            serializer.resource(null, ResourceType.MATERIAL);

            serializer.u8(0);
            serializer.f32(0.0f);
            serializer.v4(null);

            serializer.resource(null, ResourceType.TEXTURE);

            serializer.f32(0.0f);
            serializer.i32(0);

            serializer.i32(0);
            serializer.m44(null);
            serializer.i32(0);
            serializer.f32(0.0f);
        }

        if (version >= 0x147)
            frozen = serializer.i32(frozen);
        else
            serializer.bool(false);

        if ((version >= 0x22c && subVersion < 0x84) || subVersion >= 0x8b)
            editingPlayer = serializer.reference(editingPlayer, Thing.class);
    }

    @Override
    public int getAllocatedSize()
    {
        return PBody.BASE_ALLOCATION_SIZE;
    }
}
