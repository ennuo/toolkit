package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class SoftbodyVertEquivalence implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public short first, count;

    public SoftbodyVertEquivalence() {};
    public SoftbodyVertEquivalence(int first, int count) {
        this.first = (short) first;
        this.count = (short) count;
    }
    
    @SuppressWarnings("unchecked")
    @Override public SoftbodyVertEquivalence serialize(Serializer serializer, Serializable structure) {
        SoftbodyVertEquivalence equiv = 
            (structure == null) ? new SoftbodyVertEquivalence() : (SoftbodyVertEquivalence) structure;

        equiv.first = serializer.i16(equiv.first);
        equiv.count = serializer.i16(equiv.count);

        return equiv;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
