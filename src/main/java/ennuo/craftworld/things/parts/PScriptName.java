package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PScriptName implements Serializable {

    public PScriptName serialize(Serializer serializer, Serializable structure) {
        PScriptName scriptName = (structure == null) ? new PScriptName() : (PScriptName) structure;
        
        return scriptName;
    }
    
}
