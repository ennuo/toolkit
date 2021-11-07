package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PLevelSettings implements Serializable {

    public PLevelSettings serialize(Serializer serializer, Serializable structure) {
        PLevelSettings levelSettings = (structure == null) ? new PLevelSettings() : (PLevelSettings) structure;
        
        return levelSettings;
    }
    
}
