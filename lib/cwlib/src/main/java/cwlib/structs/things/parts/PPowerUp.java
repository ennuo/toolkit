package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class PPowerUp implements Serializable
{
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

    @Override
    public void serialize(Serializer serializer)
    {
        if (serializer.getRevision().getSubVersion() < 0x18d) return;

        spawnedRootMatrix = serializer.m44(spawnedRootMatrix);
        spawnedChipMatrix = serializer.m44(spawnedChipMatrix);
        rootHandle = serializer.v4(rootHandle);
        plan = serializer.resource(plan, ResourceType.PLAN, true);
        powerUpThing = serializer.thing(powerUpThing);
        powerUpHandle = serializer.thing(powerUpHandle);
        flipped = serializer.bool(flipped);
        justFlipped = serializer.bool(justFlipped);
        fireStartTime = serializer.i32(fireStartTime);
        initialRotation = serializer.f32(initialRotation);
        deterministicPosition = serializer.v3(deterministicPosition);
        deterministicRotation = serializer.f32(deterministicRotation);
        offsetForEmitters = serializer.f32(offsetForEmitters);
        prevAngle = serializer.f32(prevAngle);
        currAngle = serializer.f32(currAngle);
    }

    @Override
    public int getAllocatedSize()
    {
        return PPowerUp.BASE_ALLOCATION_SIZE;
    }
}
