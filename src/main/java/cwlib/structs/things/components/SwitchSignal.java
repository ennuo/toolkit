package cwlib.structs.things.components;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class SwitchSignal implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public float activation;
    public int ternary;
    public int player = -1;

    public SwitchSignal() {}
    public SwitchSignal(float activation) {
        this.activation = activation;
    }

    @SuppressWarnings("unchecked")
    @Override public SwitchSignal serialize(Serializer serializer, Serializable structure) {
        SwitchSignal signal = (structure == null) ? new SwitchSignal() : (SwitchSignal) structure;

        int version = serializer.getRevision().getVersion();

        signal.activation = serializer.f32(signal.activation);
        if (version >= 0x310)
            signal.ternary = serializer.i32d(signal.ternary);
        if (version >= 0x2a3)
            signal.player = serializer.i32(signal.player);

        return signal;
    }

    @Override public int getAllocatedSize() { return SwitchSignal.BASE_ALLOCATION_SIZE; }


}
