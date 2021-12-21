package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class Label implements Serializable {
    public static int MAX_SIZE = 0x6;
    
    public long key;
    public int category;
    public String translated;
    
    public Label serialize(Serializer serializer, Serializable structure) {
        Label label = (structure == null) ? new Label() : (Label) structure;
        
        label.key = serializer.u32(label.key);
        label.category = serializer.i32(label.category);
        
        return label;
    }
}
