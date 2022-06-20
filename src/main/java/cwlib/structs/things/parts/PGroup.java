package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.types.data.NetworkPlayerID;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class PGroup implements Serializable {
    public boolean copyright;
    public NetworkPlayerID creator;
    public ResourceDescriptor planDescriptor;
    public boolean editable;
    public Thing emitter;
    public int lifetime, aliveFrames;
    public boolean pickupAllMembers;
    
    public PGroup serialize(Serializer serializer, Serializable structure) {
        PGroup group = (structure == null) ? new PGroup() : (PGroup) structure;
        
        group.copyright = serializer.bool(group.copyright);
        group.creator = serializer.struct(group.creator, NetworkPlayerID.class);
        group.planDescriptor = serializer.resource(group.planDescriptor, ResourceType.PLAN, true);
        group.editable = serializer.bool(group.editable);
        group.emitter = serializer.reference(group.emitter, Thing.class);
        group.lifetime = serializer.i32(group.lifetime);
        group.aliveFrames = serializer.i32(group.aliveFrames);
        group.pickupAllMembers = serializer.bool(group.pickupAllMembers);
        
        return group;
    }
    
}
