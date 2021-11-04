package ennuo.craftworld.resources.things.parts;

import ennuo.craftworld.resources.structs.Decal;
import ennuo.craftworld.serializer.Serializer;

public class PStickers implements Part {
    public Decal[] decals;
    public Decal[][] costumeDecals;
    public Decal[] paintControl;
    public Decal[] eyetoyData;
    
    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        int count = serializer.input.i32();
        if (count != 0) {
            decals = new Decal[count];
            for (int i = 0; i < count; ++i)
                decals[i] = new Decal(serializer.input);
        }
        count = serializer.input.i32();
        if (count != 0) {
            costumeDecals = new Decal[count][];
            for (int i = 0; i < count; ++i) {
                int subcount = serializer.input.i32();
                if (subcount != 0) {
                    costumeDecals[i] = new Decal[subcount];
                    for (int j = 0; j < subcount; ++i)
                        costumeDecals[i][j] = new Decal(serializer.input);
                }
            }
        }
        
        if (serializer.partsRevision <= 0x4e) serializer.input.i32();
        
        count = serializer.input.i32();
        if (count != 0) {
            eyetoyData = new Decal[count];
            for (int i = 0; i < count; ++i)
                eyetoyData[i] = new Decal(serializer.input);
        }
    }
    
    
    
}
