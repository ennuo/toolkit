package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class Wire implements Serializable {
    public static final int SWIZZLE_ELEMENT_COUNT = 5;
    
    public int boxFrom, boxTo;
    public byte portFrom, portTo;
    public byte[] swizzle = new byte[SWIZZLE_ELEMENT_COUNT];
    
    public Wire serialize(Serializer serializer, Serializable structure) {
        Wire wire = null;
        if (structure != null) wire = (Wire) structure;
        else wire = new Wire();
        
        wire.boxFrom = (int) serializer.u32d(wire.boxFrom);
        wire.boxTo = (int) serializer.u32d(wire.boxTo);
        wire.portFrom = serializer.i8(wire.portFrom);
        wire.portTo = serializer.i8(wire.portTo);
        for (int i = 0; i < SWIZZLE_ELEMENT_COUNT; ++i)
                    wire.swizzle[i] = serializer.i8(wire.swizzle[i]);
        
        return wire;
    }
}
