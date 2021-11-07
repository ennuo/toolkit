package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Vector4f;

public class ShapeVert implements Serializable {
    public static int MAX_SIZE = 0x25;
    
    public Vector4f localPos, localNormal;
    public int boneIndex;
    
    public ShapeVert serialize(Serializer serializer, Serializable structure) {
        ShapeVert vert = (structure == null) ? new ShapeVert() : (ShapeVert) structure;
        
        serializer.v4(vert.localPos);
        serializer.v4(vert.localNormal);
        serializer.i32(vert.boneIndex);
        
        return vert;
    }
}
