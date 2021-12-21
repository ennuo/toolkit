package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Vector3f;

public class PPhysicsTweak implements Serializable {
    public float activation;
    public float tweakGravity;
    public float tweakBuoyancy;
    public Vector3f tweakDampening;
    public Vector3f input;
    public Vector3f middleVel;
    public float velRange;
    public float accelStrength;
    public float decelStrength;
    public byte directionModifier;
    public byte movementModifier;
    public boolean localSpace;
    public int configuration;
    
    public PPhysicsTweak serialize(Serializer serializer, Serializable structure) {
        PPhysicsTweak physicsTweak = (structure == null) ? new PPhysicsTweak() : (PPhysicsTweak) structure;

        return physicsTweak;
    }
    
}
