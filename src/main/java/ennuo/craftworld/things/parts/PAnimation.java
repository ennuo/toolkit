package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class PAnimation implements Serializable {
    public ResourceDescriptor animation;
    public float velocity, position;
    
    public PAnimation serialize(Serializer serializer, Serializable structure) {
        PAnimation animation = (structure == null) ? new PAnimation() : (PAnimation) structure;
        
        animation.animation = serializer.resource(animation.animation, ResourceType.ANIMATION);
        animation.velocity = serializer.f32(animation.velocity);
        animation.position = serializer.f32(animation.position);
        
        return animation;
    }
    
}
