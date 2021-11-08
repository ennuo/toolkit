package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PSwitchKey implements Serializable {
    public int colorIndex;
    
    public PSwitchKey serialize(Serializer serializer, Serializable structure) {
        PSwitchKey switchKey = (structure == null) ? new PSwitchKey() : (PSwitchKey) structure;
        
        switchKey.colorIndex = serializer.i32(switchKey.colorIndex);
        
        return switchKey;
    }
    
}
