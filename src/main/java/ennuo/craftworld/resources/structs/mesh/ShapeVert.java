package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Vector4f;

public class ShapeVert implements Serializable {
    public static int MAX_SIZE = 0x25;
    
    public Vector4f localPos, localNormal;
    public int boneIndex;
    
    public ShapeVert serialize(Serializer serializer, Serializable structure) {
        ShapeVert vert = (structure == null) ? new ShapeVert() : (ShapeVert) structure;
        
        vert.localPos = serializer.v4(vert.localPos);
        vert.localNormal = serializer.v4(vert.localNormal);
        vert.boneIndex = serializer.i32(vert.boneIndex);
        
        return vert;
    }
}
