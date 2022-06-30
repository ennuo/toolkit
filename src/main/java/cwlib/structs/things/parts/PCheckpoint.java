package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PCheckpoint implements Serializable {
    public boolean active;
    public int activationFrame;
    public byte spawnsLeft, maxSpawnsLeft;
    public boolean instanceInfiniteSpawns;
    public Thing[] spawningList;
    public int spawningDelay;
    public int lifeMultiplier;
    
    public PCheckpoint serialize(Serializer serializer, Serializable structure) {
        PCheckpoint checkpoint = (structure == null) ? new PCheckpoint() : (PCheckpoint) structure;
        
        checkpoint.active = serializer.bool(checkpoint.active);
        checkpoint.activationFrame = serializer.i32(checkpoint.activationFrame);
        checkpoint.spawnsLeft = serializer.i8(checkpoint.spawnsLeft);
        checkpoint.maxSpawnsLeft = serializer.i8(checkpoint.maxSpawnsLeft);
        checkpoint.instanceInfiniteSpawns = serializer.bool(checkpoint.instanceInfiniteSpawns);
        checkpoint.spawningList = serializer.array(checkpoint.spawningList, Thing.class, true);
        checkpoint.spawningDelay = serializer.i32(checkpoint.spawningDelay);
        checkpoint.lifeMultiplier = serializer.i32(checkpoint.lifeMultiplier);
        
        return checkpoint;
    }

    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
  
}
