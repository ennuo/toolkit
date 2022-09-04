package cwlib.resources;

import cwlib.enums.AudioMaterial;
import cwlib.enums.CollideType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

/**
 * Resource that stores physical properties
 * of objects in-game.
 */
public class RMaterial implements Compressable, Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public float traction = 1.0f, density = 20.0f;
    @GsonRevision(max=0x13b)
    public byte field000, field001;
    public float restitution = 0.3f, slidingFriction = 0.8f;
    @GsonRevision(max=0x13b)
    public float field002;
    public float rollingFriction = 1.8f;
    @GsonRevision(max=0x13b)
    public byte field003, field004, field005, field006, field007;
    public AudioMaterial soundEnum = AudioMaterial.NONE;
    public boolean grabbable = false;
    @GsonRevision(max=0x13b)
    public byte field008, field009;
    public float gravityMultiplier = 1.0f, airResistanceMultiplier = 1.0f;
    @GsonRevision(max=0x167)
    public float field010;
    public boolean limb;
    @GsonRevision(max=0x1d3)
    public byte field011;
    public boolean shiftGrip;
    public int collideType = CollideType.NORMAL, collideIgnore = CollideType.NONE;
    public boolean dissolvable, explodable, cuttableByExplosion = true;
    @GsonRevision(min=0x167)
    public boolean breakable;
    @GsonRevision(min=0x167)
    public float breakMinVel, breakMinForce;
    @GsonRevision(min=0x167)
    public float explosionMinRadius, explosionMaxRadius, explosionMaxVel;
    @GsonRevision(min=0x168)
    public float explosionMaxAngVel;
    @GsonRevision(min=0x167)
    public float explosionMaxForce;
    @GsonRevision(min=0x13c)
    public float maxForce = Float.NaN;
    @GsonRevision(min=0x244)
    public boolean bullet;
    @GsonRevision(min=0x27b)
    public boolean circuitBoard, disableCSG;

    @SuppressWarnings("unchecked")
    @Override public RMaterial serialize(Serializer serializer, Serializable structure) {
        RMaterial material = (structure == null) ? new RMaterial() : (RMaterial) structure;

        int head = serializer.getRevision().getVersion();
        
        material.traction = serializer.f32(material.traction);
        material.density = serializer.f32(material.density);

        if (head < 0x13c) {
            material.field000 = serializer.i8(material.field000);
            material.field001 = serializer.i8(material.field001);
        }

        // if (head >= 0x292)
            material.restitution = serializer.f32(material.restitution);
        // else
        //     serializer.f32(0); // Is this supposed to be restitution still?
        
        material.slidingFriction = serializer.f32(material.slidingFriction);

        if (head < 0x13c)
            material.field002 = serializer.f32(material.field002);

        material.rollingFriction = serializer.f32(material.rollingFriction);
        
        if (head < 0x13c) {
            material.field003 = serializer.i8(material.field003);
            material.field004 = serializer.i8(material.field004);
            material.field005 = serializer.i8(material.field005);
            material.field006 = serializer.i8(material.field006);
            material.field007 = serializer.i8(material.field007);
        }

        material.soundEnum = AudioMaterial.fromValue(serializer.i32(material.soundEnum.getValue()));

        material.grabbable = serializer.bool(material.grabbable);

        if (head < 0x13c) {
            material.field008 = serializer.i8(material.field008);
            material.field009 = serializer.i8(material.field009);
        }

        material.gravityMultiplier = serializer.f32(material.gravityMultiplier);
        material.airResistanceMultiplier = serializer.f32(material.airResistanceMultiplier);

        if (head < 0x167)
            material.field010 = serializer.f32(material.field010); // breakResistance?
        
        material.limb = serializer.bool(material.limb);

        if (head < 0x1d3)
            material.field011 = serializer.i8(material.field011); // creativeZone?
        
        material.shiftGrip = serializer.bool(material.shiftGrip);

        material.collideType = serializer.i32(material.collideType);
        material.collideIgnore = serializer.i32(material.collideIgnore);

        material.dissolvable = serializer.bool(material.dissolvable);
        material.explodable = serializer.bool(material.explodable);
        material.cuttableByExplosion = serializer.bool(material.cuttableByExplosion);

        if (head >= 0x167) {
            material.breakable = serializer.bool(material.breakable);
            material.breakMinVel = serializer.f32(material.breakMinVel);
            material.breakMinForce = serializer.f32(material.breakMinForce);

            material.explosionMinRadius = serializer.f32(material.explosionMinRadius);
            material.explosionMaxRadius = serializer.f32(material.explosionMaxRadius);
            material.explosionMaxVel = serializer.f32(material.explosionMaxVel);
            if (head >= 0x168)
                material.explosionMaxAngVel = serializer.f32(material.explosionMaxAngVel);
            material.explosionMaxForce = serializer.f32(material.explosionMaxForce);
        }

        if (head >= 0x13c)
            material.maxForce = serializer.f32(material.maxForce);

        if (head >= 0x244)
            material.bullet = serializer.bool(material.bullet);

        if (head >= 0x27b) {
            material.circuitBoard = serializer.bool(material.circuitBoard);
            material.disableCSG = serializer.bool(material.disableCSG);
        }

        return material;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
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