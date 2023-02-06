package cwlib.structs.things.parts;

import cwlib.enums.Branch;
import cwlib.enums.TriggerType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class PTrigger implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public TriggerType triggerType = TriggerType.RADIUS;
    public Thing[] inThings;
    public float radiusMultiplier;

    @GsonRevision(min=0x2a, lbp3=true)
    public byte zRangeHundreds;

    @GsonRevision(min=0x1d5)
    public boolean allZLayers;


    @GsonRevision(min=0x19b)
    public float hysteresisMultiplier;
    @GsonRevision(min=0x19b)
    public boolean enabled;

    @GsonRevision(min=0x322)
    public float zOffset;

    @GsonRevision(min=0x30, branch=0x4431)
    @GsonRevision(min=0x90, lbp3=true)
    public int scoreValue;
    
    @SuppressWarnings("unchecked")
    @Override public PTrigger serialize(Serializer serializer, Serializable structure) {
        PTrigger trigger = (structure == null) ? new PTrigger() : (PTrigger) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        trigger.triggerType = serializer.enum8(trigger.triggerType);
        trigger.inThings = serializer.thingarray(trigger.inThings);
        trigger.radiusMultiplier = serializer.f32(trigger.radiusMultiplier);

        if (version < 0x1d5)
            serializer.i32(0); // zLayers?
        
        if (subVersion >= 0x2a)
            trigger.zRangeHundreds = serializer.i8(trigger.zRangeHundreds);
        
        trigger.allZLayers = serializer.bool(trigger.allZLayers);

        if (version >= 0x19b) {
            trigger.hysteresisMultiplier = serializer.f32(trigger.hysteresisMultiplier);
            trigger.enabled = serializer.bool(trigger.enabled);
        }

        if (version >= 0x322)
            trigger.zOffset = serializer.f32(trigger.zOffset);

        if (subVersion >= 0x90 || revision.has(Branch.DOUBLE11, 0x30))
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
