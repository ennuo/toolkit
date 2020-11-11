package ennuo.craftworld.things.parts;

import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.Copyright;
import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;
import ennuo.craftworld.things.ThingPtr;

public class PGroup implements Part {
    public Copyright copyright;
    public ResourcePtr planDescriptor = new ResourcePtr(null, RType.PLAN);
    public boolean editable = false;
    public ThingPtr emitter = null;
    public int lifetime = 0;
    public int aliveFrames = 0;
    public boolean pickupAllMembers = false;
    public int flags = 0;
    
    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        copyright = new Copyright(serializer.input);
        planDescriptor = serializer.input.resource(RType.PLAN, true);
        if (serializer.partsRevision < 0x5e) editable = serializer.input.bool();
        emitter = serializer.deserializeThing();
        lifetime = serializer.input.int32();
        aliveFrames = serializer.input.int32();
        if (serializer.partsRevision < 0x5e) pickupAllMembers = serializer.input.bool();
        flags = serializer.input.int32();
    }
    
}
