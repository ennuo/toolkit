package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PStickers implements Serializable {

    public PStickers serialize(Serializer serializer, Serializable structure) {
        PStickers stickers = (structure == null) ? new PStickers() : (PStickers) structure;
        
        return stickers;
    }
    
}
