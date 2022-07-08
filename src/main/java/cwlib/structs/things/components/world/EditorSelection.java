package cwlib.structs.things.components.world;

import java.util.ArrayList;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class EditorSelection implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;
    
    public String name;
    public ArrayList<Thing> things = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public EditorSelection serialize(Serializer serializer, Serializable structure) {
        EditorSelection selection = (structure == null) ? new EditorSelection() : (EditorSelection) structure;

        selection.name = serializer.str(selection.name);
        selection.things = serializer.arraylist(selection.things, Thing.class, true);
        
        return selection;
    }

    @Override public int getAllocatedSize() {
        int size = EditorSelection.BASE_ALLOCATION_SIZE;
        if (this.name != null) size += (this.name.length());
        return size;
    }
}
