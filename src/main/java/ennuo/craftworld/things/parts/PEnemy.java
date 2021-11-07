package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PEnemy implements Serializable {

    public PEnemy serialize(Serializer serializer, Serializable structure) {
        PEnemy enemy = (structure == null) ? new PEnemy() : (PEnemy) structure;
        
        return enemy;
    }
    
}
