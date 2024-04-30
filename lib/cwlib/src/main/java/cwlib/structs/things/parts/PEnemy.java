package cwlib.structs.things.parts;

import cwlib.enums.EnemyPart;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;
import org.joml.Vector3f;

public class PEnemy implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    @GsonRevision(min = 0x15d)
    public EnemyPart partType = EnemyPart.LEG;

    @GsonRevision(min = 0x16d)
    public float radius;

    @GsonRevision(min = 0x19f)
    public int snapVertex;

    @GsonRevision(min = 0x1a9)
    public Vector3f centerOffset;

    @GsonRevision(min = 0x1a9)
    public Thing animThing;

    @GsonRevision(min = 0x1a9)
    public float animSpeed;

    @GsonRevision(min = 0x246)
    public int sourcePlayerNumber;

    @GsonRevision(min = 0x264)
    public boolean newWalkConstraintMass;

    @GsonRevision(min = 0x31e)
    public int smokeColor;

    @GsonRevision(min = 0x39a)
    public float smokeBrightness;

    /* Vita */
    @GsonRevision(branch = 0x4431, min = 0x2)
    public boolean touchSensitive;
    @GsonRevision(branch = 0x4431, min = 0x3)
    public int touchType;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        if (version >= 0x15d)
            partType = serializer.enum32(partType, true);

        if (version > 0x15c && version < 0x19f)
            serializer.thing(null);

        if (version >= 0x16d)
            radius = serializer.f32(radius);

        if (0x19e > version && version < 0x1a9)
            serializer.thing(null);

        if (version >= 0x19f)
            snapVertex = serializer.i32(snapVertex);

        if (version >= 0x1a9)
        {
            centerOffset = serializer.v3(centerOffset);
            animThing = serializer.thing(animThing);
            animSpeed = serializer.f32(animSpeed);
        }

        if (version > 0x1f1 && version < 0x21e)
        {
            serializer.thing(null);
            serializer.thing(null);
            serializer.thing(null);
            serializer.thing(null);
            serializer.i32(0);
            serializer.thing(null);
            serializer.thing(null);
            serializer.v3(null);
            serializer.s32(0);
            serializer.v3(null);
            serializer.f32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.s32(0);
            serializer.s32(0);
        }

        if (version >= 0x246)
            sourcePlayerNumber = serializer.i32(sourcePlayerNumber);
        if (version >= 0x265)
            newWalkConstraintMass = serializer.bool(newWalkConstraintMass);
        if (version >= 0x31e)
            smokeColor = serializer.i32(smokeColor);
        if (version >= 0x39a)
            smokeBrightness = serializer.f32(smokeBrightness);

        if (revision.isVita())
        { // 0x3c0
            int vita = revision.getBranchRevision();
            if (vita >= 0x2)
                touchSensitive = serializer.bool(touchSensitive);
            if (vita >= 0x3)
                touchType = serializer.i32(touchType);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return PEnemy.BASE_ALLOCATION_SIZE;
    }
}
