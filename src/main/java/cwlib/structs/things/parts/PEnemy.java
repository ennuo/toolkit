package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import org.joml.Vector3f;

public class PEnemy implements Serializable {
    public int partType;
    public float radius;
    public int snapVetex;
    public Vector3f centerOffset;
    public Thing animThing;
    public float animSpeed;
    public int sourcePlayerNumber;
    public boolean newWalkConstraintMass;
    
    
    public PEnemy serialize(Serializer serializer, Serializable structure) {
        PEnemy enemy = (structure == null) ? new PEnemy() : (PEnemy) structure;
        
        enemy.partType = serializer.i32(enemy.partType);
        enemy.radius = serializer.f32(enemy.radius);
        
        
        return enemy;
    }
    
}
