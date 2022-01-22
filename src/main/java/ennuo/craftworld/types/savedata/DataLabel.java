package ennuo.craftworld.types.savedata;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

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
