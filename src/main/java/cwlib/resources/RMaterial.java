package cwlib.resources;

import cwlib.enums.AudioMaterial;
import cwlib.enums.CollideType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
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
    public float restitution = 0.3f, slidingFriction = 0.8f, rollingFriction = 1.8f;
    public AudioMaterial soundEnum = AudioMaterial.NONE;
    public boolean grabbable = false;
    public float gravityMultiplier = 1.0f, airResistanceMultiplier = 1.0f;
    public boolean limb, shiftGrip;
    public int collideType = CollideType.NORMAL, collideIgnore = CollideType.NONE;
    public boolean dissolvable, explodable, cuttableByExplosion = true, breakable;
    public float breakMinVel, breakMinForce;
    public float explosionMinRadius, explosionMaxRadius;
    public float explosionMaxVel, explosionMaxAngVel;
    public float explosionMaxForce;
    public float maxForce = Float.NaN;
    public boolean bullet, circuitBoard;
    public boolean disableCSG;

    @SuppressWarnings("unchecked")
    @Override public RMaterial serialize(Serializer serializer, Serializable structure) {
        RMaterial material = (structure == null) ? new RMaterial() : (RMaterial) structure;

        int head = serializer.getRevision().getVersion();
        if (head < 0x13c)
            throw new SerializationException("RMaterial serialization below r316 is not supported.");

        material.traction = serializer.f32(material.traction);
        material.density = serializer.f32(material.density);
        material.restitution = serializer.f32(material.restitution);
        material.slidingFriction = serializer.f32(material.slidingFriction);
        material.rollingFriction = serializer.f32(material.rollingFriction);
        
        material.soundEnum = AudioMaterial.fromValue(serializer.i32(material.soundEnum.getValue()));

        material.grabbable = serializer.bool(material.grabbable);

        material.gravityMultiplier = serializer.f32(material.gravityMultiplier);
        material.airResistanceMultiplier = serializer.f32(material.airResistanceMultiplier);

        if (head < 0x167)
            serializer.f32(0); // Unknown value
        
        material.limb = serializer.bool(material.limb);

        if (head < 0x1d3)
            serializer.bool(false); // Unknown value
        
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

    @Override public int getAllocatedSize() { return RMaterial.BASE_ALLOCATION_SIZE; }

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