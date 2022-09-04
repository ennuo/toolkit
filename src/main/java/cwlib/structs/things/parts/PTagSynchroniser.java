package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PTagSynchroniser implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int type, colorIndex;

    @GsonRevision(min=0x2dc) public String name;
    
    public float radius, minRadius, scaleX, scaleY;
    public boolean useLayers, visible;
    
    @SuppressWarnings("unchecked")
    @Override public PTagSynchroniser serialize(Serializer serializer, Serializable structure) {
        PTagSynchroniser tag = (structure == null) ? new PTagSynchroniser() : (PTagSynchroniser) structure;

        tag.type = serializer.s32(tag.type);

        tag.colorIndex = serializer.s32(tag.colorIndex);
        if (serializer.getRevision().getVersion() >= 0x2dc)
            tag.name = serializer.wstr(tag.name);
        
        tag.radius = serializer.f32(tag.radius);
        tag.minRadius = serializer.f32(tag.minRadius);
        tag.scaleX = serializer.f32(tag.scaleX);
        tag.scaleY = serializer.f32(tag.scaleY);

        tag.useLayers = serializer.bool(tag.useLayers);
        tag.visible = serializer.bool(tag.visible);

        return tag;
    }

    @Override public int getAllocatedSize() { 
        int size = PTagSynchroniser.BASE_ALLOCATION_SIZE;
        if (this.name != null) size += (this.name.length() * 0x2);
        return size;
    }
}
