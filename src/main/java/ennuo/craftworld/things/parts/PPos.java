package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.things.Thing;
import org.joml.Matrix4f;

public class PPos implements Serializable {
    public Thing thingOfWhichIAmABone;
    public long animHash;
    public Matrix4f localPosition;
    public Matrix4f worldPosition;

    public PPos serialize(Serializer serializer, Serializable structure) {
        PPos pos = (structure == null) ? new PPos() : (PPos) structure;
        
        pos.thingOfWhichIAmABone = serializer.struct(pos.thingOfWhichIAmABone, Thing.class);
        pos.animHash = serializer.u32(pos.animHash);
        pos.localPosition = serializer.matrix(pos.localPosition);
        pos.worldPosition = serializer.matrix(pos.worldPosition);
        
        return pos;
    }
    
}
