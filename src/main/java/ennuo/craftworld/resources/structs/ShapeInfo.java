package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class ShapeInfo {
    public static int MAX_SIZE = 0x8;
    
    public int numVerts;
    public int isPointCloud;
    
    public ShapeInfo(Data data) {
        numVerts = data.int32();
        isPointCloud = data.int32();
    }
    
    public void serialize(Output output) {
        output.int32(numVerts);
        output.int32(isPointCloud);
    }
}
