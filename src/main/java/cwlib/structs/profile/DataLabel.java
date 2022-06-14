package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class DataLabel implements Serializable {
    public int labelIndex;
    public String name;
    
    public DataLabel serialize(Serializer serializer, Serializable structure) {
        DataLabel label = 
                (structure == null) ? new DataLabel() : (DataLabel) structure;
        
        label.labelIndex = serializer.i32(label.labelIndex);
        label.name = serializer.str16(label.name);
        
        return label;
    }
    
    
    
}
