package ennuo.craftworld.things.parts;

import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;

public class PScript implements Part {
    public ResourcePtr script = new ResourcePtr(null, RType.SCRIPT);
    public ScriptInstance instanceLayout;
    
    
    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        script = serializer.input.resource(RType.SCRIPT);
        if (serializer.input.bool())
            instanceLayout = (ScriptInstance) serializer.deserializePart("SCRIPTINSTANCE");
    }
    
}
