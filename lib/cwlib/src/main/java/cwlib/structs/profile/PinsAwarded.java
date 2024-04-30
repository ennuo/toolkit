package cwlib.structs.profile;

import java.util.ArrayList;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class PinsAwarded implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ArrayList<PinAward> pinAwards = new ArrayList<>();
    public ArrayList<PinProgress> pinProgress = new ArrayList<>();
    public int[] recentlyAwardedPinIDs;
    public int[] profileDisplayPinIDs;

    @GsonRevision(min = Revisions.PIN_FLAGS)
    public byte pinsFlags;

    @GsonRevision(min = Revisions.WEEKDAYS_PLAYED_PIN)
    public byte weekdaysPlayedBits;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_MOE_PIN_PROGRESS)
    public int moreOfEverythingPinProgress;

    @GsonRevision(lbp3 = true, min = Revisions.SLAPPED_AS_PIN)
    public byte slappedAsBits;

    @GsonRevision(lbp3 = true, min = Revisions.SIMON_SAYS_PIN)
    public int simonSaysProgress;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version >= Revisions.PROFILE_PINS)
        {
            pinAwards = serializer.arraylist(pinAwards, PinAward.class);
            pinProgress = serializer.arraylist(pinProgress, PinProgress.class);
            recentlyAwardedPinIDs = serializer.intvector(recentlyAwardedPinIDs);
            profileDisplayPinIDs = serializer.intvector(profileDisplayPinIDs);
        }

        if (version >= Revisions.PIN_FLAGS)
            pinsFlags = serializer.i8(pinsFlags);

        if (version >= Revisions.WEEKDAYS_PLAYED_PIN)
            weekdaysPlayedBits = serializer.i8(weekdaysPlayedBits);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_MOE_PIN_PROGRESS))
            moreOfEverythingPinProgress = serializer.i32(moreOfEverythingPinProgress);

        if (subVersion >= Revisions.SLAPPED_AS_PIN)
            slappedAsBits = serializer.i8(slappedAsBits);

        if (subVersion >= Revisions.SIMON_SAYS_PIN)
            simonSaysProgress = serializer.i32(simonSaysProgress);
    }

    public boolean hasPin(int id)
    {
        for (int i = 0; i < pinAwards.size(); ++i)
        {
            if (pinAwards.get(i).pinID == id) return true;
        }
        return false;
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PinsAwarded.BASE_ALLOCATION_SIZE;
        if (this.pinAwards != null)
            size += (this.pinAwards.size() * PinAward.BASE_ALLOCATION_SIZE);
        if (this.pinProgress != null)
            size += (this.pinProgress.size() * PinProgress.BASE_ALLOCATION_SIZE);
        if (this.recentlyAwardedPinIDs != null) size += (this.recentlyAwardedPinIDs.length * 4);
        if (this.profileDisplayPinIDs != null) size += (this.profileDisplayPinIDs.length * 4);
        return size;
    }
}
