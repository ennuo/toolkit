package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class RPins implements Serializable {
    public Pin[] pins;

    public static class Pin implements Serializable {
        
        public long id, progressType, category;
        public long titleLamsKey, descriptionLamsKey;
        public String translatedName, translatedDescription;
        public ResourceDescriptor icon;
        public long initialProgressValue, targetValue;
        public byte trophyToUnlock;
        public short behaviourFlags;
        public byte trophyToUnlockLBP1;

        @SuppressWarnings("unchecked")
        @Override public Pin serialize(Serializer serializer, Serializable structure) {
            Pin pin = (structure == null) ? new Pin() : (Pin) structure;

            pin.id = serializer.u32(pin.id);
            pin.progressType = serializer.u32(pin.progressType);
            pin.category = serializer.u32(pin.category);
            pin.titleLamsKey = serializer.u32(pin.titleLamsKey);
            pin.descriptionLamsKey = serializer.u32(pin.descriptionLamsKey);
            pin.icon = serializer.resource(pin.icon, ResourceType.TEXTURE, true);
            pin.initialProgressValue = serializer.u32(pin.initialProgressValue);
            pin.targetValue = serializer.u32(pin.targetValue);
            pin.trophyToUnlock = serializer.i8(pin.trophyToUnlock);
            pin.behaviourFlags = serializer.i16(pin.behaviourFlags);
            if (!serializer.getRevision().isVita())
                pin.trophyToUnlockLBP1 = serializer.i8(pin.trophyToUnlockLBP1);
            
            return pin;

        }

        @Override public int getAllocatedSize() { return -1; }

        
    }

    @SuppressWarnings("unchecked")
    @Override public RPins serialize(Serializer serializer, Serializable structure) {
        RPins pins = (structure == null) ? new RPins() : (RPins) structure;

        pins.pins = serializer.array(pins.pins, Pin.class);

        return pins;
    }

    @Override public int getAllocatedSize() { return -1; }
}
