package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class PRef implements Serializable {
    ResourceDescriptor plan;
    int oldLifetime;
    int oldAliveFrames;
    boolean childrenSelectable;
    boolean stripChildren;
    
    
    public PRef serialize(Serializer serializer, Serializable structure) {
        PRef ref = (structure == null) ? new PRef() : (PRef) structure;
        
        ref.plan = serializer.resource(ref.plan, ResourceType.PLAN, true);
        ref.oldLifetime = serializer.i32(ref.oldLifetime);
        ref.oldAliveFrames = serializer.i32(ref.oldAliveFrames);
        ref.childrenSelectable = serializer.bool(ref.childrenSelectable);
        ref.stripChildren = serializer.bool(ref.stripChildren);
        
        return ref;
    }
    
}
