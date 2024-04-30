package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class Challenge implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int challengeID;
    public int challengeStatus;
    public int levelID;
    public int scoreToBeat;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_CHALLENGE_LEVEL_TYPE)
    public int levelType;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_CHALLENGE_SCORE)
    public int myScore;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_CHALLENGE_SCORE)
    public String networkOnlineID;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();

        challengeID = serializer.i32(challengeID);
        challengeStatus = serializer.i32(challengeStatus);
        levelID = serializer.i32(levelID);
        scoreToBeat = serializer.i32(scoreToBeat);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_CHALLENGE_LEVEL_TYPE))
            levelType = serializer.i32(levelType);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_CHALLENGE_SCORE))
        {
            myScore = serializer.i32(myScore);
            networkOnlineID = serializer.str(networkOnlineID);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = Challenge.BASE_ALLOCATION_SIZE;
        if (this.networkOnlineID != null)
            size += (this.networkOnlineID.length());
        return size;
    }
}
