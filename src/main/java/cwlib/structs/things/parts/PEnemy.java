package cwlib.structs.things.parts;

import cwlib.enums.EnemyPart;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

import org.joml.Vector3f;

public class PEnemy implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    @GsonRevision(min=0x15d)
    public EnemyPart partType = EnemyPart.LEG;

    @GsonRevision(min=0x16d)
    public float radius;

    @GsonRevision(min=0x19f)
    public int snapVertex;

    @GsonRevision(min=0x1a9)
    public Vector3f centerOffset;

    @GsonRevision(min=0x1a9)
    public Thing animThing;

    @GsonRevision(min=0x1a9)
    public float animSpeed;

    @GsonRevision(min=0x246)
    public int sourcePlayerNumber;

    @GsonRevision(min=0x264)
    public boolean newWalkConstraintMass;

    @GsonRevision(min=0x31e)
    public int smokeColor;

    @GsonRevision(min=0x39a)
    public float smokeBrightness;

    /* Vita */
    @GsonRevision(branch=0x4431,min=0x2)
    public boolean touchSensitive;
    @GsonRevision(branch=0x4431,min=0x3)
    public int touchType;

    @SuppressWarnings("unchecked")
    @Override public PEnemy serialize(Serializer serializer, Serializable structure) {
        PEnemy enemy = (structure == null) ? new PEnemy() : (PEnemy) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        if (version >= 0x15d)
            enemy.partType = serializer.enum32(enemy.partType, true);

        if (version > 0x15c && version < 0x19f)
            serializer.thing(null);

        if (version >= 0x16d)
            enemy.radius = serializer.f32(enemy.radius);

        if (0x19e > version && version < 0x1a9)
            serializer.thing(null);

        if (version >= 0x19f)
            enemy.snapVertex = serializer.i32(enemy.snapVertex);
        
        if (version >= 0x1a9) {
            enemy.centerOffset = serializer.v3(enemy.centerOffset);
            enemy.animThing = serializer.thing(enemy.animThing);
            enemy.animSpeed = serializer.f32(enemy.animSpeed);
        }

        if (version > 0x1f1 && version < 0x21e) {
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
            enemy.sourcePlayerNumber = serializer.i32(enemy.sourcePlayerNumber);
        if (version >= 0x265)
            enemy.newWalkConstraintMass = serializer.bool(enemy.newWalkConstraintMass);
        if (version >= 0x31e)
            enemy.smokeColor = serializer.i32(enemy.smokeColor);
        if (version >= 0x39a)
            enemy.smokeBrightness = serializer.f32(enemy.smokeBrightness);

        if (revision.isVita()) { // 0x3c0
            int vita = revision.getBranchRevision();
            if (vita >= 0x2)
                enemy.touchSensitive = serializer.bool(enemy.touchSensitive);
            if (vita >= 0x3)
                enemy.touchType = serializer.i32(enemy.touchType);
        }
        
        return enemy;
    }

    @Override public int getAllocatedSize() { return PEnemy.BASE_ALLOCATION_SIZE; }
}
