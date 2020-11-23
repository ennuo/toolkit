package ennuo.craftworld.things.parts;

import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;
import ennuo.craftworld.things.ThingPtr;

public class PTrigger implements Part {
    public int triggerType = 0;
    public ThingPtr[] inThings = null;
    public float radiusMultiplier = 600;
    public int zRangeHundreds = 5;
    public boolean allZLayers = false;
    public float hysteresisMultiplier = 1;
    public boolean enabled = true;
    public float zOffset = 0;
    public int scoreValue = 10;
    
    
    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        triggerType = serializer.input.int32();
        int thingCount = serializer.input.int32();
        if (thingCount != 0) {
            inThings = new ThingPtr[thingCount];
            for (int i = 0; i < thingCount; ++i)
                inThings[i] = serializer.deserializeThing();
        }
        radiusMultiplier = serializer.input.float32();
        if (serializer.partsRevision >= 0x7e)
            zRangeHundreds = serializer.input.int32();
        allZLayers = serializer.input.bool();
        hysteresisMultiplier = serializer.input.float32();
        enabled = serializer.input.bool();
        if (serializer.partsRevision >= 0x7e) {
            zOffset = serializer.input.float32();
            scoreValue = serializer.input.int32();
        }
    }
    
}
