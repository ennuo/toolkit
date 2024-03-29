package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Vector4f;

public class ImplicitPlane implements Serializable {
    public Vector4f planeNormal, pointInPlane;
    public int parentBone;
    
    public ImplicitPlane serialize(Serializer serializer, Serializable structure) {
        ImplicitPlane plane = 
                (structure == null) ? new ImplicitPlane() : (ImplicitPlane) structure;
        
        plane.planeNormal = serializer.v4(plane.planeNormal);
        plane.pointInPlane = serializer.v4(plane.pointInPlane);
        plane.parentBone = serializer.i32(plane.parentBone);
        
        return plane;
    }
}
