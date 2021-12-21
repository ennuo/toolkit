package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PStickers implements Serializable {

    public PStickers serialize(Serializer serializer, Serializable structure) {
        PStickers stickers = (structure == null) ? new PStickers() : (PStickers) structure;
        
        return stickers;
    }
    
}
