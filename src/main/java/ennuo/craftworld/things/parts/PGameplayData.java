package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PGameplayData implements Serializable {

    public PGameplayData serialize(Serializer serializer, Serializable structure) {
        PGameplayData gameplayData = (structure == null) ? new PGameplayData() : (PGameplayData) structure;
        
        return gameplayData;
    }
    
}
