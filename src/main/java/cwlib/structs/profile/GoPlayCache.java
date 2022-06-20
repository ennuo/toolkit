package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class GoPlayCache implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public SlotID levelID;
    public SlotID planetID;
    public byte numBubbleCollected;
    public byte awardLevelComplete;
    public byte awardCollectAll;
    public byte awardAcedLevel;

    @SuppressWarnings("unchecked")
    @Override public GoPlayCache serialize(Serializer serializer, Serializable structure) {
        GoPlayCache cache = (structure == null) ? new GoPlayCache() : (GoPlayCache) structure;

        cache.levelID = serializer.struct(cache.levelID, SlotID.class);
        cache.planetID = serializer.struct(cache.planetID, SlotID.class);

        cache.numBubbleCollected = serializer.i8(cache.numBubbleCollected);
        cache.awardLevelComplete = serializer.i8(cache.awardLevelComplete);
        cache.awardCollectAll = serializer.i8(cache.awardCollectAll);
        cache.awardAcedLevel = serializer.i8(cache.awardAcedLevel);
        
        return cache;
    }

    @Override public int getAllocatedSize() { return GoPlayCache.BASE_ALLOCATION_SIZE; }
}
