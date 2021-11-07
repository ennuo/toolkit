package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PMetadata implements Serializable {

    public PMetadata serialize(Serializer serializer, Serializable structure) {
        PMetadata metadata = (structure == null) ? new PMetadata() : (PMetadata) structure;
        
        return metadata;
    }
    
}
