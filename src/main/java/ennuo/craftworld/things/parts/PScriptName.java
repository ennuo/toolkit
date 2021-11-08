package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PScriptName implements Serializable {
    public String name;
    
    public PScriptName serialize(Serializer serializer, Serializable structure) {
        PScriptName scriptName = (structure == null) ? new PScriptName() : (PScriptName) structure;
        
        scriptName.name = serializer.str8(scriptName.name);
        
        return scriptName;
    }
    
}
