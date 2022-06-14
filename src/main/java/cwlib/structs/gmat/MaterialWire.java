package cwlib.structs.gmat;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MaterialWire implements Serializable {
    public static final int SWIZZLE_ELEMENT_COUNT = 5;
    
    public int boxFrom, boxTo;
    public byte portFrom, portTo;
    public byte[] swizzle = new byte[SWIZZLE_ELEMENT_COUNT];
    
    public MaterialWire serialize(Serializer serializer, Serializable structure) {
        MaterialWire wire = null;
        if (structure != null) wire = (MaterialWire) structure;
        else wire = new MaterialWire();
        
        wire.boxFrom = (int) serializer.u32d(wire.boxFrom);
        wire.boxTo = (int) serializer.u32d(wire.boxTo);
        wire.portFrom = serializer.i8(wire.portFrom);
        wire.portTo = serializer.i8(wire.portTo);
        for (int i = 0; i < SWIZZLE_ELEMENT_COUNT; ++i)
                    wire.swizzle[i] = serializer.i8(wire.swizzle[i]);
        
        return wire;
    }
}
