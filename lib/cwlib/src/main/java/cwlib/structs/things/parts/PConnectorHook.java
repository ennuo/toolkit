package cwlib.structs.things.parts;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PConnectorHook implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public int mode, inputAction;
    public float poweredSpeed;
    public float accel, decel;

    @GsonRevision(lbp3 = true, min = 0x7c)
    public float delay;

    @GsonRevision(lbp3 = true, min = 0x7d)
    public boolean reverse;

    @GsonRevision(lbp3 = true, min = 0xfe)
    public boolean tryLatch, clamped;

    @GsonRevision(lbp3 = true, min = 0xfe)
    public int collidable;

    @GsonRevision(lbp3 = true, min = 0xfe)
    public float angle, friction;

    @GsonRevision(lbp3 = true, min = 0xfe)
    public Vector4f pivot;

    @GsonRevision(lbp3 = true, min = 0x104)
    public float spinDamping;

    @GsonRevision(lbp3 = true, min = 0x13e)
    public boolean audioEnable;

    @GsonRevision(lbp3 = true, min = 0x14f)
    public int hookType;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        mode = serializer.i32(mode);

        if (subVersion <= 0xbb)
            serializer.i32(0);

        inputAction = serializer.i32(inputAction);

        if (subVersion <= 0xbd)
            serializer.v3(null);

        poweredSpeed = serializer.f32(poweredSpeed);
        accel = serializer.f32(accel);
        decel = serializer.f32(decel);

        if (subVersion > 0x7b)
            delay = serializer.f32(delay);

        if (subVersion > 0x7c)
            reverse = serializer.bool(reverse);

        if (subVersion > 0xfd)
        {
            tryLatch = serializer.bool(tryLatch);
            clamped = serializer.bool(clamped);
            collidable = serializer.i32(collidable);
            angle = serializer.f32(angle);
            friction = serializer.f32(friction);
            pivot = serializer.v4(pivot);
        }

        if (subVersion >= 0xff && subVersion < 0x15d)
            serializer.i32(0);

        if (subVersion > 0x103)
            spinDamping = serializer.f32(spinDamping);

        if (subVersion > 0x13d)
            audioEnable = serializer.bool(audioEnable);

        if (subVersion > 0x14e)
            hookType = serializer.i32(hookType);
    }

    @Override
    public int getAllocatedSize()
    {
        return PConnectorHook.BASE_ALLOCATION_SIZE;
    }
}
