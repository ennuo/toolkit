package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class PinsAwarded implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public PinAward[] pinAwards;
    public PinProgress[] pinProgress;
    public int[] recentlyAwardedPinIDs;
    public int[] profileDisplayPinIDs;
    public byte pinsFlags;
    public byte weekdaysPlayedBits;
    public int moreOfEverythingPinProgress;
    public byte slappedAsBits;
    public int simonSaysProgress;

    @SuppressWarnings("unchecked")
    @Override public PinsAwarded serialize(Serializer serializer, Serializable structure) {
        PinsAwarded pins = (structure == null) ? new PinsAwarded() : (PinsAwarded) structure;

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();
        
        if (head > 0x384) {
            pins.pinAwards = serializer.array(pins.pinAwards, PinAward.class);
            pins.pinProgress = serializer.array(pins.pinProgress, PinProgress.class);
            pins.recentlyAwardedPinIDs = serializer.intvector(pins.recentlyAwardedPinIDs);
            pins.profileDisplayPinIDs = serializer.intvector(pins.profileDisplayPinIDs);
        }

        if (head > 0x33a)
            pins.pinsFlags = serializer.i8(pins.pinsFlags);
        
        if (head > 0x34a)
            pins.weekdaysPlayedBits = serializer.i8(pins.weekdaysPlayedBits);
        
        if (revision.isAfterVitaRevision(0x63))
            pins.moreOfEverythingPinProgress = serializer.i32(pins.moreOfEverythingPinProgress);

        if (revision.isAfterLBP3Revision(0x176))
            pins.slappedAsBits = serializer.i8(pins.slappedAsBits);
        
        if (revision.isAfterLBP3Revision(0x208))
            pins.simonSaysProgress = serializer.i32(pins.simonSaysProgress);

        return pins;
    }

    @Override public int getAllocatedSize() {
        int size = PinsAwarded.BASE_ALLOCATION_SIZE;
        if (this.pinAwards != null) size += (this.pinAwards.length * PinAward.BASE_ALLOCATION_SIZE);
        if (this.pinProgress != null) size += (this.pinProgress.length * PinProgress.BASE_ALLOCATION_SIZE);
        if (this.recentlyAwardedPinIDs != null) size += (this.recentlyAwardedPinIDs.length * 4);
        if (this.profileDisplayPinIDs != null) size += (this.profileDisplayPinIDs.length * 4);
        return size;
    }
}
