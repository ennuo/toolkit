package ennuo.craftworld.things.parts;

import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;

public class PScriptName implements Part {
    String name;
    
    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        name = serializer.input.str8();
    }
    
}
