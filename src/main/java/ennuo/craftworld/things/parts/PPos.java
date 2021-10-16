package ennuo.craftworld.things.parts;

import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;
import ennuo.craftworld.things.ThingPtr;

public class PPos implements Part {
    public static long PART_FLAG = 16;
   
    public ThingPtr thingOfWhichIAmABone = null;
    public long animHash = 0;
    public float[] localPosition;
    public float[] worldPosition;
    

    @Override
    public void Serialize(Serializer serializer) {
        serializer.serializeThing(thingOfWhichIAmABone);
        serializer.output.i32((int) animHash);
        serializer.output.matrix(localPosition);
        if (serializer.partsRevision < 0x5e)
            serializer.output.matrix(worldPosition);
    }

    @Override
    public void Deserialize(Serializer serializer) {
        thingOfWhichIAmABone = serializer.deserializeThing();
        animHash = serializer.input.u32();
        localPosition = serializer.input.matrix();
        if (serializer.partsRevision < 0x5e)
            worldPosition = serializer.input.matrix();
    }
}