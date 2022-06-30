package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PCreature implements Serializable {

    public PCreature serialize(Serializer serializer, Serializable structure) {
        PCreature creature = (structure == null) ? new PCreature() : (PCreature) structure;
        
        return creature;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
