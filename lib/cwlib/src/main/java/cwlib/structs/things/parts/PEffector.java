package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Part that works as the gravity of the world.
 */
public class PEffector implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Vector3f posVel;
    public float angVel, viscosity, density;
    public Vector3f gravity = new Vector3f(0.0f, -2.7f, 0.0f);
    public boolean pushBack, swimmable;
    public float viscosityCheap = 0.05f;
    public float modScale = 1.0f;

    @Override
    public void serialize(Serializer serializer)
    {
        posVel = serializer.v3(posVel);
        angVel = serializer.f32(angVel);
        viscosity = serializer.f32(viscosity);
        density = serializer.f32(density);
        gravity = serializer.v3(gravity);
        pushBack = serializer.bool(pushBack);
        swimmable = serializer.bool(swimmable);
        viscosityCheap = serializer.f32(viscosityCheap);
        modScale = serializer.f32(modScale);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}