package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Vector3f;

public class Polygon {
    public Vector3f[] vertices = new Vector3f[] {
        new Vector3f(-100, 100, 0),
        new Vector3f(100, 100, 0),
        new Vector3f(100, -100, 0),
        new Vector3f(-100, -100, 0)
    };
    public int[] loops = new int[] { 4 };
    
    public Polygon(Data data, int partsRevision) {
        boolean isVector3 = true;
        int count = data.int32();
        if (partsRevision >= 0x5e) isVector3 = data.bool();
        Vector3f[] vertices = new Vector3f[count];
        for (int i = 0; i < vertices.length; ++i) {
            Vector3f vector = new Vector3f(0, 0, 0);
            vector.x = data.float32();
            vector.y = data.float32();
            if (isVector3) vector.z = data.float32();
        }
        
        int loopCount = data.int32();
        if (partsRevision >= 0x4e) data.int8();
        
        loops = new int[loopCount];
        for (int i = 0; i < loopCount; ++i)
            loops[i] = data.int32(); 
    }
}
