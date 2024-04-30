package cwlib.resources;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.CollideType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.gson.AudioMaterialSerializer;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

/**
 * Resource that stores physical properties
 * of objects in-game.
 */
public class RMaterial implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public float traction = 1.0f, density = 20.0f;
    @GsonRevision(max = 0x13b)
    public byte field000, field001;
    public float restitution = 0.3f, slidingFriction = 0.8f;
    @GsonRevision(max = 0x13b)
    public float field002;
    public float rollingFriction = 1.8f;
    @GsonRevision(max = 0x13b)
    public byte field003, field004, field005, field006, field007;

    @JsonAdapter(AudioMaterialSerializer.class)
    public int soundEnum;

    public boolean grabbable = false;
    @GsonRevision(max = 0x13b)
    public byte field008, field009;
    public float gravityMultiplier = 1.0f, airResistanceMultiplier = 1.0f;
    @GsonRevision(max = 0x167)
    public float field010;
    public boolean limb;
    @GsonRevision(max = 0x1d3)
    public byte field011;
    public boolean shiftGrip;
    public int collideType = CollideType.NORMAL, collideIgnore = CollideType.NONE;
    public boolean dissolvable, explodable, cuttableByExplosion = true;
    @GsonRevision(min = 0x167)
    public boolean breakable;
    @GsonRevision(min = 0x167)
    public float breakMinVel, breakMinForce;
    @GsonRevision(min = 0x167)
    public float explosionMinRadius, explosionMaxRadius, explosionMaxVel;
    @GsonRevision(min = 0x168)
    public float explosionMaxAngVel;
    @GsonRevision(min = 0x167)
    public float explosionMaxForce;
    @GsonRevision(min = 0x13c)
    public float maxForce = Float.NaN;
    @GsonRevision(min = 0x244)
    public boolean bullet;
    @GsonRevision(min = 0x27b)
    public boolean circuitBoard, disableCSG;

    @Override
    public void serialize(Serializer serializer)
    {

        int head = serializer.getRevision().getVersion();

        traction = serializer.f32(traction);
        density = serializer.f32(density);

        if (head < 0x13c)
        {
            field000 = serializer.i8(field000);
            field001 = serializer.i8(field001);
        }

        // if (head >= 0x292)
        restitution = serializer.f32(restitution);
        // else
        //     serializer.f32(0); // Is this supposed to be restitution still?

        slidingFriction = serializer.f32(slidingFriction);

        if (head < 0x13c)
            field002 = serializer.f32(field002);

        rollingFriction = serializer.f32(rollingFriction);

        if (head < 0x13c)
        {
            field003 = serializer.i8(field003);
            field004 = serializer.i8(field004);
            field005 = serializer.i8(field005);
            field006 = serializer.i8(field006);
            field007 = serializer.i8(field007);
        }

        soundEnum = serializer.i32(soundEnum);

        grabbable = serializer.bool(grabbable);

        if (head < 0x13c)
        {
            field008 = serializer.i8(field008);
            field009 = serializer.i8(field009);
        }

        gravityMultiplier = serializer.f32(gravityMultiplier);
        airResistanceMultiplier = serializer.f32(airResistanceMultiplier);

        if (head < 0x167)
            field010 = serializer.f32(field010); // breakResistance?

        limb = serializer.bool(limb);

        if (head < 0x1d3)
            field011 = serializer.i8(field011); // creativeZone?

        shiftGrip = serializer.bool(shiftGrip);

        collideType = serializer.i32(collideType);
        collideIgnore = serializer.i32(collideIgnore);

        dissolvable = serializer.bool(dissolvable);
        explodable = serializer.bool(explodable);
        cuttableByExplosion = serializer.bool(cuttableByExplosion);

        if (head >= 0x167)
        {
            breakable = serializer.bool(breakable);
            breakMinVel = serializer.f32(breakMinVel);
            breakMinForce = serializer.f32(breakMinForce);

            explosionMinRadius = serializer.f32(explosionMinRadius);
            explosionMaxRadius = serializer.f32(explosionMaxRadius);
            explosionMaxVel = serializer.f32(explosionMaxVel);
            if (head >= 0x168)
                explosionMaxAngVel = serializer.f32(explosionMaxAngVel);
            explosionMaxForce = serializer.f32(explosionMaxForce);
        }

        if (head >= 0x13c)
            maxForce = serializer.f32(maxForce);

        if (head >= 0x244)
            bullet = serializer.bool(bullet);

        if (head >= 0x27b)
        {
            circuitBoard = serializer.bool(circuitBoard);
            disableCSG = serializer.bool(disableCSG);
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
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
        serializer.struct(this, RMaterial.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.MATERIAL,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }
}