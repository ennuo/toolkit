package cwlib.structs.profile;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class Challenge implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int challengeID;
    public int challengeStatus;
    public int levelID;
    public int scoreToBeat;

    @GsonRevision(branch=0x4431, min=Revisions.D1_CHALLENGE_LEVEL_TYPE)
    public int levelType;

    @GsonRevision(branch=0x4431, min=Revisions.D1_CHALLENGE_SCORE)
    public int myScore;

    @GsonRevision(branch=0x4431, min=Revisions.D1_CHALLENGE_SCORE)
    public String networkOnlineID;

    @SuppressWarnings("unchecked")
    @Override public Challenge serialize(Serializer serializer, Serializable structure) {
        Challenge challenge = (structure == null) ? new Challenge() : (Challenge) structure;

        Revision revision = serializer.getRevision();

        challenge.challengeID = serializer.i32(challenge.challengeID);
        challenge.challengeStatus = serializer.i32(challenge.challengeStatus);
        challenge.levelID = serializer.i32(challenge.levelID);
        challenge.scoreToBeat = serializer.i32(challenge.scoreToBeat);
        
        if (revision.has(Branch.DOUBLE11, Revisions.D1_CHALLENGE_LEVEL_TYPE))
            challenge.levelType = serializer.i32(challenge.levelType);
        
        if (revision.has(Branch.DOUBLE11, Revisions.D1_CHALLENGE_SCORE)) {
            challenge.myScore = serializer.i32(challenge.myScore);
            challenge.networkOnlineID = serializer.str(challenge.networkOnlineID);
        }

        return challenge;
    }

    @Override public int getAllocatedSize() {
        int size = Challenge.BASE_ALLOCATION_SIZE;
        if (this.networkOnlineID != null)
            size += (this.networkOnlineID.length());
        return size;
    }
}
