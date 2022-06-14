package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PSwitchKey implements Serializable {
    public int colorIndex;
    
    public PSwitchKey serialize(Serializer serializer, Serializable structure) {
        PSwitchKey switchKey = (structure == null) ? new PSwitchKey() : (PSwitchKey) structure;
        
        switchKey.colorIndex = serializer.i32(switchKey.colorIndex);
        
        return switchKey;
    }
    
}
