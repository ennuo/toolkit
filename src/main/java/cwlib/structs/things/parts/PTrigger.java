package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PTrigger implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public byte triggerType;
    public Thing[] inThings;
    public float radiusMultiplier;
    public byte zRangeHundreds;
    public boolean allZLayers;
    public float hysteresisMultiplier;
    public boolean enabled;
    public float zOffset;
    public int scoreValue;
    
    @SuppressWarnings("unchecked")
    @Override public PTrigger serialize(Serializer serializer, Serializable structure) {
        PTrigger trigger = (structure == null) ? new PTrigger() : (PTrigger) structure;
        
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        trigger.triggerType = serializer.i8(trigger.triggerType);
        trigger.inThings = serializer.array(trigger.inThings, Thing.class, true);
        trigger.radiusMultiplier = serializer.f32(trigger.radiusMultiplier);
        
        if (subVersion >= 0x2a)
            trigger.zRangeHundreds = serializer.i8(trigger.zRangeHundreds);
        
        if (version >= 0x1d5)
            trigger.allZLayers = serializer.bool(trigger.allZLayers);

        if (version >= 0x19b) {
            trigger.hysteresisMultiplier = serializer.f32(trigger.hysteresisMultiplier);
            trigger.enabled = serializer.bool(trigger.enabled);
        }

        if (version >= 0x322)
            trigger.zOffset = serializer.f32(trigger.zOffset);

        if (subVersion >= 0x90)
            trigger.scoreValue = serializer.s32(trigger.scoreValue);
        
        return trigger;
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        // We'll actually calculate the size of these Thing's
        // in the Thing class to avoid circular dependencies.
        if (this.inThings != null)
            size += (this.inThings.length) * Thing.BASE_ALLOCATION_SIZE;
        return size;
    }
}
