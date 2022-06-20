package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Challenge implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int challengeID;
    public int challengeStatus;
    public int levelID;
    public int scoreToBeat;
    public int levelType;
    public int myScore;
    public String networkOnlineID;

    @SuppressWarnings("unchecked")
    @Override public Challenge serialize(Serializer serializer, Serializable structure) {
        Challenge challenge = (structure == null) ? new Challenge() : (Challenge) structure;

        challenge.challengeID = serializer.i32(challenge.challengeID);
        challenge.challengeStatus = serializer.i32(challenge.challengeStatus);
        challenge.levelID = serializer.i32(challenge.levelID);
        challenge.scoreToBeat = serializer.i32(challenge.scoreToBeat);
        
        if (serializer.getRevision().isAfterVitaRevision(0x5a))
            challenge.levelType = serializer.i32(challenge.levelType);
        
        if (serializer.getRevision().isAfterVitaRevision(0x67)) {
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
