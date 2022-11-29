package cwlib.structs.gmat;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * A wire that connects two boxes on the shader graph,
 * used by Media Molecule in their shader editor.
 */
public class MaterialWire implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;
    public static final int SWIZZLE_ELEMENT_COUNT = 5;

    public static int lastUID = 0;
    public int id = ++lastUID;

    public int boxFrom, boxTo;
    public byte portFrom, portTo;
    public byte[] swizzle = new byte[SWIZZLE_ELEMENT_COUNT];

    @SuppressWarnings("unchecked")
    @Override public MaterialWire serialize(Serializer serializer, Serializable structure) {
        MaterialWire wire = (structure == null) ? new MaterialWire() : (MaterialWire) structure;

        wire.boxFrom = serializer.s32(wire.boxFrom);
        wire.boxTo = serializer.s32(wire.boxTo);
        wire.portFrom = serializer.i8(wire.portFrom);
        wire.portTo = serializer.i8(wire.portTo);
        for (int i = 0; i < SWIZZLE_ELEMENT_COUNT; ++i)
            wire.swizzle[i] = serializer.i8(wire.swizzle[i]);

        return wire;
    }

    @Override public int getAllocatedSize() { return MaterialWire.BASE_ALLOCATION_SIZE; }
}
