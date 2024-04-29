package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.CompactComponent;

public class PMicrochip implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0xC0;

    public Thing circuitBoardThing;

    @GsonRevision(max = 0x2e3)
    @Deprecated
    public boolean circuitBoardVisible;

    @GsonRevision(max = 0x2e8)
    @Deprecated
    public Thing parentThing;

    @GsonRevision(max = 0x2e8)
    @Deprecated
    public Matrix4f cachedParentPos, localTransform;

    @GsonRevision(min = 0x283)
    public boolean hideInPlayMode;

    @GsonRevision(min = 0x2b8)
    public boolean wiresVisible;

    @GsonRevision(min = 0x2e4)
    public int lastTouched;

    @GsonRevision(min = 0x2e9)
    public Vector4f offset;

    @GsonRevision(min = 0x34d)
    public String name;

    @GsonRevision(min = 0x34d)
    public CompactComponent[] components;

    @GsonRevision(min = 0x34d)
    public float circuitBoardSizeX, circuitBoardSizeY;

    @GsonRevision(lbp3 = true, min = 0x1d)
    public boolean keepVisualVertical;

    @GsonRevision(lbp3 = true, min = 0x2d)
    public byte broadcastType;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        circuitBoardThing = serializer.thing(circuitBoardThing);
        if (version < 0x2e4)
            circuitBoardVisible = serializer.bool(circuitBoardVisible);
        if (version < 0x2e9)
        {
            parentThing = serializer.thing(parentThing);
            cachedParentPos = serializer.m44(cachedParentPos);
            localTransform = serializer.m44(localTransform);
        }

        if (version >= 0x283)
            hideInPlayMode = serializer.bool(hideInPlayMode);
        if (version >= 0x2b8)
            wiresVisible = serializer.bool(wiresVisible);
        if (version >= 0x2e4)
            lastTouched = serializer.s32(lastTouched);

        if (version >= 0x2e9)
            offset = serializer.v4(offset);

        if (version >= 0x34d)
        {
            name = serializer.wstr(name);
            components = serializer.array(components, CompactComponent.class);
            circuitBoardSizeX = serializer.f32(circuitBoardSizeX);
            circuitBoardSizeY = serializer.f32(circuitBoardSizeY);
        }

        if (subVersion >= 0x1d)
            keepVisualVertical = serializer.bool(keepVisualVertical);
        if (subVersion >= 0x2d)
            broadcastType = serializer.i8(broadcastType);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PMicrochip.BASE_ALLOCATION_SIZE;
        if (this.components != null)
            size += (this.components.length * CompactComponent.BASE_ALLOCATION_SIZE);
        if (this.name != null) size += (this.name.length() * 0x2);
        return size;
    }
}
