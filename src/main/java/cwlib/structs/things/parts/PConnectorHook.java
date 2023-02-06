package cwlib.structs.things.parts;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PConnectorHook implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public int mode, inputAction;
    public float poweredSpeed;
    public float accel, decel;

    @GsonRevision(lbp3=true, min=0x7c)
    public float delay;

    @GsonRevision(lbp3=true, min=0x7d)
    public boolean reverse;

    @GsonRevision(lbp3=true, min=0xfe)
    public boolean tryLatch, clamped;

    @GsonRevision(lbp3=true, min=0xfe)
    public int collidable;

    @GsonRevision(lbp3=true, min=0xfe)
    public float angle, friction;

    @GsonRevision(lbp3=true, min=0xfe)
    public Vector4f pivot;

    @GsonRevision(lbp3=true, min=0x104)
    public float spinDamping;

    @GsonRevision(lbp3=true, min=0x13e)
    public boolean audioEnable;

    @GsonRevision(lbp3=true, min=0x14f)
    public int hookType;

    @SuppressWarnings("unchecked")
    @Override public PConnectorHook serialize(Serializer serializer, Serializable structure) {
        PConnectorHook hook = (structure == null) ? new PConnectorHook() : (PConnectorHook) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        hook.mode = serializer.i32(hook.mode);

        if (subVersion <= 0xbb)
            serializer.i32(0);

        hook.inputAction = serializer.i32(hook.inputAction);

        if (subVersion <= 0xbd)
            serializer.v3(null);

        hook.poweredSpeed = serializer.f32(hook.poweredSpeed);
        hook.accel = serializer.f32(hook.accel);
        hook.decel = serializer.f32(hook.decel);

        if (subVersion > 0x7b)
            hook.delay = serializer.f32(hook.delay);

        if (subVersion > 0x7c)
            hook.reverse = serializer.bool(hook.reverse);

        if (subVersion > 0xfd) {
            hook.tryLatch = serializer.bool(hook.tryLatch);
            hook.clamped = serializer.bool(hook.clamped);
            hook.collidable = serializer.i32(hook.collidable);
            hook.angle = serializer.f32(hook.angle);
            hook.friction = serializer.f32(hook.friction);
            hook.pivot = serializer.v4(hook.pivot);
        }

        if (subVersion >= 0xff && subVersion < 0x15d)
            serializer.i32(0);

        if (subVersion > 0x103)
            hook.spinDamping = serializer.f32(hook.spinDamping);

        if (subVersion > 0x13d)
            hook.audioEnable = serializer.bool(hook.audioEnable);
        
        if (subVersion > 0x14e)
            hook.hookType = serializer.i32(hook.hookType);
        
        return hook;
    }

    @Override public int getAllocatedSize() {
        return PConnectorHook.BASE_ALLOCATION_SIZE;
    }
}
