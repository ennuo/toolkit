package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class PPowerUp implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xF0;

    public Matrix4f spawnedRootMatrix, spawnedChipMatrix;
    public Vector4f rootHandle;
    public ResourceDescriptor plan;
    public Thing powerUpThing, powerUpHandle;
    public boolean flipped, justFlipped;
    public int fireStartTime;
    public float initialRotation;
    public Vector3f deterministicPosition;
    public float deterministicRotation, offsetForEmitters, prevAngle, currAngle;
    
    @SuppressWarnings("unchecked")
    @Override public PPowerUp serialize(Serializer serializer, Serializable structure) {
        PPowerUp powerup = (structure == null) ? new PPowerUp() : (PPowerUp) structure;
    
        if (serializer.getRevision().getSubVersion() < 0x18d) return powerup;

        powerup.spawnedRootMatrix = serializer.m44(powerup.spawnedRootMatrix);
        powerup.spawnedChipMatrix = serializer.m44(powerup.spawnedChipMatrix);
        powerup.rootHandle = serializer.v4(powerup.rootHandle);
        powerup.plan = serializer.resource(powerup.plan, ResourceType.PLAN, true);
        powerup.powerUpThing = serializer.thing(powerup.powerUpThing);
        powerup.powerUpHandle = serializer.thing(powerup.powerUpHandle);
        powerup.flipped = serializer.bool(powerup.flipped);
        powerup.justFlipped = serializer.bool(powerup.justFlipped);
        powerup.fireStartTime = serializer.i32(powerup.fireStartTime);
        powerup.initialRotation = serializer.f32(powerup.initialRotation);
        powerup.deterministicPosition = serializer.v3(powerup.deterministicPosition);
        powerup.deterministicRotation = serializer.f32(powerup.deterministicRotation);
        powerup.offsetForEmitters = serializer.f32(powerup.offsetForEmitters);
        powerup.prevAngle = serializer.f32(powerup.prevAngle);
        powerup.currAngle = serializer.f32(powerup.currAngle);

        return powerup;
    }
    
    @Override public int getAllocatedSize() { return PPowerUp.BASE_ALLOCATION_SIZE; }
}
