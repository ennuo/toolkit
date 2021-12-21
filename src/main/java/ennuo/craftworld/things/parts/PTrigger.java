package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.things.Thing;

public class PTrigger implements Serializable {
    public int triggerType;
    public Thing[] inThings;
    public float radiusMultiplier;
    public boolean allZLayers;
    public float hysteresisMultiplier;
    public boolean enabled;
    
    public PTrigger serialize(Serializer serializer, Serializable structure) {
        PTrigger trigger = (structure == null) ? new PTrigger() : (PTrigger) structure;
        
        trigger.triggerType = serializer.i32(trigger.triggerType);
        trigger.inThings = serializer.array(trigger.inThings, Thing.class, true);
        trigger.radiusMultiplier = serializer.f32(trigger.radiusMultiplier);
        trigger.allZLayers = serializer.bool(trigger.allZLayers);
        trigger.hysteresisMultiplier = serializer.f32(trigger.hysteresisMultiplier);
        trigger.enabled = serializer.bool(trigger.enabled);
        
        return trigger;
    }
    
}
