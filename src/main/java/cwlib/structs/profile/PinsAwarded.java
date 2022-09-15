package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class PinsAwarded implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public PinAward[] pinAwards;
    public PinProgress[] pinProgress;
    public int[] recentlyAwardedPinIDs;
    public int[] profileDisplayPinIDs;

    @GsonRevision(min=Revisions.PIN_FLAGS)
    public byte pinsFlags;

    @GsonRevision(min=Revisions.WEEKDAYS_PLAYED_PIN)
    public byte weekdaysPlayedBits;

    @GsonRevision(branch=0x4431, min=Revisions.D1_MOE_PIN_PROGRESS)
    public int moreOfEverythingPinProgress;

    @GsonRevision(lbp3=true, min=Revisions.SLAPPED_AS_PIN)
    public byte slappedAsBits;

    @GsonRevision(lbp3=true, min=Revisions.SIMON_SAYS_PIN)
    public int simonSaysProgress;

    @SuppressWarnings("unchecked")
    @Override public PinsAwarded serialize(Serializer serializer, Serializable structure) {
        PinsAwarded pins = (structure == null) ? new PinsAwarded() : (PinsAwarded) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();
        
        if (version >= Revisions.PROFILE_PINS) {
            pins.pinAwards = serializer.array(pins.pinAwards, PinAward.class);
            pins.pinProgress = serializer.array(pins.pinProgress, PinProgress.class);
            pins.recentlyAwardedPinIDs = serializer.intvector(pins.recentlyAwardedPinIDs);
            pins.profileDisplayPinIDs = serializer.intvector(pins.profileDisplayPinIDs);
        }

        if (version >= Revisions.PIN_FLAGS)
            pins.pinsFlags = serializer.i8(pins.pinsFlags);
        
        if (version >= Revisions.WEEKDAYS_PLAYED_PIN)
            pins.weekdaysPlayedBits = serializer.i8(pins.weekdaysPlayedBits);
        
        if (revision.has(Branch.DOUBLE11, Revisions.D1_MOE_PIN_PROGRESS))
            pins.moreOfEverythingPinProgress = serializer.i32(pins.moreOfEverythingPinProgress);

        if (subVersion >= Revisions.SLAPPED_AS_PIN)
            pins.slappedAsBits = serializer.i8(pins.slappedAsBits);
        
        if (subVersion >= Revisions.SIMON_SAYS_PIN)
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
