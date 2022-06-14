package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PEmitter implements Serializable {

    public PEmitter serialize(Serializer serializer, Serializable structure) {
        PEmitter emitter = (structure == null) ? new PEmitter() : (PEmitter) structure;
        
        return emitter;
    }
    
}
