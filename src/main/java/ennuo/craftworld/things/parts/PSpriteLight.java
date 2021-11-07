package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PSpriteLight implements Serializable {

    public PSpriteLight serialize(Serializer serializer, Serializable structure) {
        PSpriteLight spriteLight = (structure == null) ? new PSpriteLight() : (PSpriteLight) structure;
        
        return spriteLight;
    }
    
}
