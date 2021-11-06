package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.serializer.v2.Serializable;
import ennuo.craftworld.serializer.v2.Serializer;

public class Wire implements Serializable {
    public int boxFrom, boxTo;
    public byte portFrom, portTo;
    
    public Wire serialize(Serializer serializer, Serializable structure) {
        Wire wire = null;
        if (structure != null) wire = (Wire) structure;
        else wire = new Wire();
        
        wire.boxFrom = (int) serializer.u32d(wire.boxFrom);
        wire.boxTo = (int) serializer.u32d(wire.boxTo);
        wire.portFrom = serializer.i8(wire.portFrom);
        wire.portTo = serializer.i8(wire.portTo);
        
        // NOTE(Abz): I have no idea what this is, no named fields for it,
        // it's always just null bytes, so I figure it doesn't matter anyway.
        serializer.pad(0x5);
        
        return wire;
    }
}
