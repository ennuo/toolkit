package cwlib.structs.things.components.script;

import java.util.ArrayList;

import cwlib.enums.ModifierType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

public class InstanceLayout implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public ArrayList<FieldLayoutDetails> fields = new ArrayList<>();
    public int instanceSize;

    public InstanceLayout() {}
    public InstanceLayout(InstanceLayout layout) {
        for (FieldLayoutDetails field : layout.fields)
            this.fields.add(new FieldLayoutDetails(field));
        this.instanceSize = layout.instanceSize;
    }

    @SuppressWarnings("unchecked")
    @Override public InstanceLayout serialize(Serializer serializer, Serializable structure) {
        InstanceLayout layout = (structure == null) ? new InstanceLayout() : (InstanceLayout) structure;

        layout.fields = serializer.arraylist(layout.fields, FieldLayoutDetails.class);
        if (serializer.getRevision().getVersion() < 0x1ec) {
            if (serializer.isWriting()) {
                MemoryOutputStream stream = serializer.getOutput();
                FieldLayoutDetails[] fields = layout.getFieldsForReflection(false);
                stream.i32(fields.length);
                for (FieldLayoutDetails field : fields) {
                    stream.str(field.name);
                    stream.i32(layout.fields.indexOf(field));
                }
            } else {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                for (int i = 0; i < count; ++i) {
                    stream.str(); // fieldName
                    stream.i32(); // fieldIndex
                }
            }
        } 
        layout.instanceSize = serializer.i32(layout.instanceSize);

        return layout;
    }

    public FieldLayoutDetails[] getFieldsForReflection(boolean reflectDivergent) {
        ArrayList<FieldLayoutDetails> fields = new ArrayList<>(this.fields.size());
        for (FieldLayoutDetails field : this.fields) {
            if (field.modifiers.contains(ModifierType.DIVERGENT) && !reflectDivergent) continue;
            fields.add(field);
        }
        return fields.toArray(FieldLayoutDetails[]::new);
    }

    @Override public int getAllocatedSize() { 
        int size = InstanceLayout.BASE_ALLOCATION_SIZE;
        if (this.fields != null)
            for (FieldLayoutDetails details : this.fields) 
                size += (details.getAllocatedSize() * 2);
        return size;
    }
}