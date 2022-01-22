package ennuo.craftworld.types.savedata;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class LevelData implements Serializable {
    
    public LevelData serialize(Serializer serializer, Serializable structure) {
        LevelData data = (structure == null) ? new LevelData() : (LevelData) structure;
        
        
        return data;
    }
    
}
