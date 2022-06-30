package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Part that works as the gravity of the world.
 */
public class PEffector implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Vector3f posVel;
    public float angVel, viscosity, density;
    public Vector3f gravity = new Vector3f(0.0f, -2.7f, 0.0f);
    public boolean pushBack, swimmable;
    public float viscosityCheap = 0.05f;
    public float modScale = 1.0f;

    @SuppressWarnings("unchecked")
    @Override public PEffector serialize(Serializer serializer, Serializable structure) {
        PEffector effector = (structure == null) ? new PEffector() : (PEffector) structure;

        effector.posVel = serializer.v3(effector.posVel);
        effector.angVel = serializer.f32(effector.angVel);
        effector.viscosity = serializer.f32(effector.viscosity);
        effector.density = serializer.f32(effector.density);
        effector.gravity = serializer.v3(effector.gravity);
        effector.pushBack = serializer.bool(effector.pushBack);
        effector.swimmable = serializer.bool(effector.swimmable);
        effector.viscosityCheap = serializer.f32(effector.viscosityCheap);
        effector.modScale = serializer.f32(effector.modScale);

        return effector;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}