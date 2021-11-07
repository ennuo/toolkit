package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PGeneratedMesh implements Serializable {

    public PGeneratedMesh serialize(Serializer serializer, Serializable structure) {
        PGeneratedMesh generatedMesh = (structure == null) ? new PGeneratedMesh() : (PGeneratedMesh) structure;
        
        return generatedMesh;
    }
    
}
