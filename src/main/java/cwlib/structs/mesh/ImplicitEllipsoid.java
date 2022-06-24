package cwlib.structs.mesh;

import org.joml.Matrix4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class ImplicitEllipsoid implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public Matrix4f transform;
    public int parentBone, affectWorldOnly;

    @SuppressWarnings("unchecked")
    @Override public ImplicitEllipsoid serialize(Serializer serializer, Serializable structure) {
        ImplicitEllipsoid ellipsoid = 
            (structure == null) ? new ImplicitEllipsoid() : (ImplicitEllipsoid) structure;
        
        ellipsoid.transform = serializer.m44(ellipsoid.transform);
        ellipsoid.parentBone = serializer.i32(ellipsoid.parentBone);
        ellipsoid.affectWorldOnly = serializer.i32(ellipsoid.affectWorldOnly);

        return ellipsoid;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
