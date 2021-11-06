package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.v2.Serializable;
import ennuo.craftworld.serializer.v2.Serializer;

public class ShapeInfo implements Serializable {
    public static int MAX_SIZE = 0x8;
    
    public int numVerts;
    public int isPointCloud;

    public ShapeInfo serialize(Serializer serializer, Serializable structure) {
        ShapeInfo info = (structure == null) ? new ShapeInfo() : (ShapeInfo) structure;
        
        info.numVerts = serializer.i32(info.numVerts);
        info.isPointCloud = serializer.i32(info.isPointCloud);
        
        return info;
    }
}
