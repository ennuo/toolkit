package cwlib.resources;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class RMaterial implements Serializable {
    public float traction;
    public float density;
    public float restitution;
    public float slidingFriction, rollingFriction;
    public int soundEnumOverride;
    public boolean grabbable;
    public float gravityMultiplier, airResistanceMultiplier;
    public boolean compactMaterialLimb, shiftGrip;
    public int collideType, collideIgnore;
    public boolean dissolvable, explodable;
    public boolean cuttableByExplosion;
    public boolean breakable;
    public float breakMinVel, breakMinForce;
    public float explosionMinRadius, explosionMaxRadius;
    public float explosionMaxVel, explosionMaxAngVel, explosionMaxForce;
    public float maxForce;
    public boolean bullet, circuitBoard, disableCSG;

    public RMaterial serialize(Serializer serializer, Serializable structure) {
        RMaterial material = (structure == null) ? new RMaterial() : (RMaterial) structure;
        
        material.traction = serializer.f32(material.traction);
        material.density = serializer.f32(material.density);
        
        if (serializer.revision.head > 0x13b)
            material.restitution = serializer.f32(material.restitution);
        
        material.slidingFriction = serializer.f32(material.slidingFriction);
        material.rollingFriction = serializer.f32(material.rollingFriction);
        
        material.soundEnumOverride = serializer.i32(material.soundEnumOverride);
        
        material.grabbable = serializer.bool(material.grabbable);
        
        material.gravityMultiplier = serializer.f32(material.gravityMultiplier);
        material.airResistanceMultiplier = serializer.f32(material.airResistanceMultiplier);
        
        material.compactMaterialLimb = serializer.bool(material.compactMaterialLimb);
        
        material.shiftGrip = serializer.bool(material.shiftGrip);
        
        material.collideType = serializer.i32(material.collideType);
        material.collideIgnore = serializer.i32(material.collideIgnore);
        
        material.dissolvable = serializer.bool(material.dissolvable);
        material.explodable = serializer.bool(material.explodable);
        material.cuttableByExplosion = serializer.bool(material.cuttableByExplosion);
        material.breakable = serializer.bool(material.breakable);
        
        material.breakMinVel = serializer.f32(material.breakMinVel);
        material.breakMinForce = serializer.f32(material.breakMinForce);
        
        material.explosionMinRadius = serializer.f32(material.explosionMinRadius);
        material.explosionMaxRadius = serializer.f32(material.explosionMaxRadius);
        
        material.explosionMaxVel = serializer.f32(material.explosionMaxVel);
        material.explosionMaxAngVel = serializer.f32(material.explosionMaxAngVel);
        
        material.explosionMaxForce = serializer.f32(material.explosionMaxForce);
        
        if (serializer.revision.head >= 0x13c)
            material.maxForce = serializer.f32(material.maxForce);
        
        if (serializer.revision.head >= 0x244)
            material.bullet = serializer.bool(material.bullet);
        
        if (serializer.revision.head >= 0x27b) {
            material.circuitBoard = serializer.bool(material.circuitBoard);
            material.disableCSG = serializer.bool(material.disableCSG);
        }
        
        return material;
    }
}