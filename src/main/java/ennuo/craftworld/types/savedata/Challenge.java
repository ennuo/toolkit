package ennuo.craftworld.types.savedata;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class Challenge implements Serializable {
    public int challengeID;
    public int challengeStatus;
    public int levelID;
    public int scoreToBeat;
    public int levelType;
    public int myScore;
    public String networkOnlineID;
    
    public Challenge serialize(Serializer serializer, Serializable structure) {
        Challenge challenge = 
                (structure == null) ? new Challenge() : (Challenge) structure;
        
        challenge.challengeID = serializer.i32(challenge.challengeID);
        challenge.challengeStatus = serializer.i32(challenge.challengeStatus);
        challenge.levelID = serializer.i32(challenge.levelID);
        challenge.scoreToBeat = serializer.i32(challenge.scoreToBeat);
        
        if (serializer.revision.isAfterVitaRevision(0x5a))
            challenge.levelType = serializer.i32(challenge.levelType);
        
        if (serializer.revision.isAfterVitaRevision(0x67)) {
            challenge.myScore = serializer.i32(challenge.myScore);
            challenge.networkOnlineID = serializer.str8(challenge.networkOnlineID);
        }
    
        return challenge;
    }
    
    
    
}
