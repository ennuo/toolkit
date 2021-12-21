package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Matrix4f;

public class ImplicitEllipsoid implements Serializable {
    public Matrix4f transform;
    public int parentBone, affectWorldOnly;

    public ImplicitEllipsoid serialize(Serializer serializer, Serializable structure) {
        ImplicitEllipsoid ellipsoid = 
                (structure == null) ? new ImplicitEllipsoid() : (ImplicitEllipsoid) structure;
        
        ellipsoid.transform = serializer.matrix(ellipsoid.transform);
        ellipsoid.parentBone = serializer.i32(ellipsoid.parentBone);
        ellipsoid.affectWorldOnly = serializer.i32(ellipsoid.affectWorldOnly);
        
        return ellipsoid;
    }
}
