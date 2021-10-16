package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class ShapeInfo {
    public static int MAX_SIZE = 0x8;
    
    public int numVerts;
    public int isPointCloud;
    
    public ShapeInfo(Data data) {
        numVerts = data.i32();
        isPointCloud = data.i32();
    }
    
    public void serialize(Output output) {
        output.i32(numVerts);
        output.i32(isPointCloud);
    }
}
