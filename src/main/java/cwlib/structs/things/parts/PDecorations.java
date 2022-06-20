package cwlib.structs.things.parts;

import cwlib.structs.things.Decoration;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PDecorations implements Serializable {
    public Decoration[] decorations;
    
    @SuppressWarnings("unchecked")
    @Override public PDecorations serialize(Serializer serializer, Serializable structure) {
        PDecorations decorations = (structure == null) ? new PDecorations() : (PDecorations) structure;
        
        decorations.decorations = serializer.array(decorations.decorations, Decoration.class);
        
        return decorations;
    }

    
    
}
