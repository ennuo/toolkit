package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.structs.Decoration;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PDecorations implements Serializable {
    public Decoration[] decorations;
    
    public PDecorations serialize(Serializer serializer, Serializable structure) {
        PDecorations decorations = (structure == null) ? new PDecorations() : (PDecorations) structure;
        
        decorations.decorations = serializer.array(decorations.decorations, Decoration.class);
        
        return decorations;
    }
    
}
