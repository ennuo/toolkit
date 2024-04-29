package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class GoPlayCache implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public SlotID levelID;
    public SlotID planetID;
    public byte numBubbleCollected;
    public byte awardLevelComplete;
    public byte awardCollectAll;
    public byte awardAcedLevel;

    @Override
    public void serialize(Serializer serializer)
    {
        levelID = serializer.struct(levelID, SlotID.class);
        planetID = serializer.struct(planetID, SlotID.class);

        numBubbleCollected = serializer.i8(numBubbleCollected);
        awardLevelComplete = serializer.i8(awardLevelComplete);
        awardCollectAll = serializer.i8(awardCollectAll);
        awardAcedLevel = serializer.i8(awardAcedLevel);
    }

    @Override
    public int getAllocatedSize()
    {
        return GoPlayCache.BASE_ALLOCATION_SIZE;
    }
}
