package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PDecorations implements Serializable {

    public PDecorations serialize(Serializer serializer, Serializable structure) {
        PDecorations decorations = (structure == null) ? new PDecorations() : (PDecorations) structure;
        
        return decorations;
    }
    
}
