package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PinAward implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int pinID, awardCount;

    @SuppressWarnings("unchecked")
    @Override public PinAward serialize(Serializer serializer, Serializable structure) {
        PinAward award = (structure == null) ? new PinAward() : (PinAward) structure;

        award.pinID = serializer.i32(award.pinID);
        award.awardCount = serializer.i32(award.awardCount);

        return award;
    }

    @Override public int getAllocatedSize() { return PinAward.BASE_ALLOCATION_SIZE; }
}
