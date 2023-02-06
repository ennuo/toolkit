package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.CompactComponent;

public class PMicrochip implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xC0;

    public Thing circuitBoardThing;

    @GsonRevision(max=0x2e3)
    @Deprecated public boolean circuitBoardVisible;

    @GsonRevision(max=0x2e8)
    @Deprecated public Thing parentThing;

    @GsonRevision(max=0x2e8)
    @Deprecated public Matrix4f cachedParentPos, localTransform;

    @GsonRevision(min=0x283)
    public boolean hideInPlayMode;
    
    @GsonRevision(min=0x2b8)
    public boolean wiresVisible;

    @GsonRevision(min=0x2e4)
    public int lastTouched;

    @GsonRevision(min=0x2e9)
    public Vector4f offset;

    @GsonRevision(min=0x34d)
    public String name;

    @GsonRevision(min=0x34d)
    public CompactComponent[] components;

    @GsonRevision(min=0x34d)
    public float circuitBoardSizeX, circuitBoardSizeY;

    @GsonRevision(lbp3=true, min=0x1d)
    public boolean keepVisualVertical;

    @GsonRevision(lbp3=true, min=0x2d)
    public byte broadcastType;

    @SuppressWarnings("unchecked")
    @Override public PMicrochip serialize(Serializer serializer, Serializable structure) {
        PMicrochip microchip = (structure == null) ? new PMicrochip() : (PMicrochip) structure;
        
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        microchip.circuitBoardThing = serializer.thing(microchip.circuitBoardThing);
        if (version < 0x2e4) 
            microchip.circuitBoardVisible = serializer.bool(microchip.circuitBoardVisible);
        if (version < 0x2e9) {
            microchip.parentThing = serializer.thing(microchip.parentThing);
            microchip.cachedParentPos = serializer.m44(microchip.cachedParentPos);
            microchip.localTransform = serializer.m44(microchip.localTransform);
        }

        if (version >= 0x283)
            microchip.hideInPlayMode = serializer.bool(microchip.hideInPlayMode);
        if (version >= 0x2b8)
            microchip.wiresVisible = serializer.bool(microchip.wiresVisible);
        if (version >= 0x2e4)
            microchip.lastTouched = serializer.s32(microchip.lastTouched);

        if (version >= 0x2e9)
            microchip.offset = serializer.v4(microchip.offset);

        if (version >= 0x34d) {
            microchip.name = serializer.wstr(microchip.name);
            microchip.components = serializer.array(microchip.components, CompactComponent.class);
            microchip.circuitBoardSizeX = serializer.f32(microchip.circuitBoardSizeX);
            microchip.circuitBoardSizeY = serializer.f32(microchip.circuitBoardSizeY);
        }

        if (subVersion >= 0x1d)
            microchip.keepVisualVertical = serializer.bool(microchip.keepVisualVertical);
        if (subVersion >= 0x2d)
            microchip.broadcastType = serializer.i8(microchip.broadcastType);
        
        return microchip;
    }

    @Override public int getAllocatedSize() { 
        int size = PMicrochip.BASE_ALLOCATION_SIZE;
        if (this.components != null) size += (this.components.length * CompactComponent.BASE_ALLOCATION_SIZE);
        if (this.name != null) size += (this.name.length() * 0x2);
        return size;
    }
}
