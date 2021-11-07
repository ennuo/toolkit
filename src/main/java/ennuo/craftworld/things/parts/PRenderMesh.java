package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PRenderMesh implements Serializable {

    public PRenderMesh serialize(Serializer serializer, Serializable structure) {
        PRenderMesh renderMesh = (structure == null) ? new PRenderMesh() : (PRenderMesh) structure;
        
        return renderMesh;
    }
    
}
