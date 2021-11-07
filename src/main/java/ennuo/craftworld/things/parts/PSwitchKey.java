package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PSwitchKey implements Serializable {

    public PSwitchKey serialize(Serializer serializer, Serializable structure) {
        PSwitchKey switchKey = (structure == null) ? new PSwitchKey() : (PSwitchKey) structure;
        
        return switchKey;
    }
    
}
