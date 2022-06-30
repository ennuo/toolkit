package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PLevelSettings implements Serializable {

    public PLevelSettings serialize(Serializer serializer, Serializable structure) {
        PLevelSettings levelSettings = (structure == null) ? new PLevelSettings() : (PLevelSettings) structure;
        
        return levelSettings;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
