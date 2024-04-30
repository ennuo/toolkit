package cwlib.structs.things.components.switches;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class SwitchSignal implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public float activation;
    @GsonRevision(min = 0x310)
    public int ternary;
    @GsonRevision(min = 0x2a3)
    public int player = -1;

    public SwitchSignal() { }

    public SwitchSignal(float activation)
    {
        this.activation = activation;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        activation = serializer.f32(activation);
        if (version >= 0x310)
            ternary = serializer.s32(ternary);
        if (version >= 0x2a3)
            player = serializer.i32(player);
    }

    @Override
    public int getAllocatedSize()
    {
        return SwitchSignal.BASE_ALLOCATION_SIZE;
    }


}
