package cwlib.structs.things.components;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class CompactComponent implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Thing thing;
    public float x, y, angle, scaleX = 1, scaleY = 1;
    public boolean flipped;

    @SuppressWarnings("unchecked")
    @Override public CompactComponent serialize(Serializer serializer, Serializable structure) {
        CompactComponent component = (structure == null) ? new CompactComponent() : (CompactComponent) structure;
        
        component.thing = serializer.thing(component.thing);
        component.x = serializer.f32(component.x);
        component.y = serializer.f32(component.y);
        component.angle = serializer.f32(component.angle);
        component.scaleX = serializer.f32(component.scaleX);
        component.scaleY = serializer.f32(component.scaleY);
        component.flipped = serializer.bool(component.flipped);

        return component;
    }

    @Override public int getAllocatedSize() { return CompactComponent.BASE_ALLOCATION_SIZE; }
}
