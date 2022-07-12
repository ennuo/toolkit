package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

import org.joml.Vector3f;

public class PEnemy implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public int partType;
    public float radius;
    public int snapVertex;
    public Vector3f centerOffset;
    public Thing animThing;
    public float animSpeed;
    public int sourcePlayerNumber;
    public boolean newWalkConstraintMass;
    public int smokeColor;
    public float smokeBrightness;

    @SuppressWarnings("unchecked")
    @Override public PEnemy serialize(Serializer serializer, Serializable structure) {
        PEnemy enemy = (structure == null) ? new PEnemy() : (PEnemy) structure;
        
        int version = serializer.getRevision().getVersion();

        if (version >= 0x15d)
            enemy.partType = serializer.s32(enemy.partType);

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

        return enemy;
    }

    @Override public int getAllocatedSize() { return PEnemy.BASE_ALLOCATION_SIZE; }
}
