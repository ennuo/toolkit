package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.structs.EggLink;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PGameplayData implements Serializable {
    public int fluffCost;
    public SlotID keyLink;
    public EggLink eggLink;
    
    public PGameplayData serialize(Serializer serializer, Serializable structure) {
        PGameplayData gameplayData = (structure == null) ? new PGameplayData() : (PGameplayData) structure;
        
        gameplayData.fluffCost = serializer.i32(gameplayData.fluffCost);
        gameplayData.eggLink = serializer.reference(gameplayData.eggLink, EggLink.class);
        gameplayData.keyLink = serializer.reference(gameplayData.keyLink, SlotID.class);
        
        return gameplayData;
    }
    
}
