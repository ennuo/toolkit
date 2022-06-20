package cwlib.structs.slot;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Represents a label tag that gets
 * assigned to a slot.
 */
public class Label implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x8;

    public int key;
    public int order;
    
    @SuppressWarnings("unchecked")
    @Override public Label serialize(Serializer serializer, Serializable structure) {
        Label label = (structure == null) ? new Label() : (Label) structure;
        
        label.key = serializer.i32(label.key);
        label.order = serializer.i32(label.order);
        
        return label;
    }

    @Override public int getAllocatedSize() { return Label.BASE_ALLOCATION_SIZE; }
}
