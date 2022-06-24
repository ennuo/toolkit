package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PYellowHead implements Serializable {
    @SuppressWarnings("unchecked")
    @Override public PYellowHead serialize(Serializer serializer, Serializable structure) {
        PYellowHead yellowHead = (structure == null) ? new PYellowHead() : (PYellowHead) structure;
        
        return yellowHead;
    }

    @Override public int getAllocatedSize() { return 0; }
}
