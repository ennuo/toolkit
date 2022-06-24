package cwlib.structs.mesh;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class ImplicitPlane implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Vector4f planeNormal, pointInPlane;
    public int parentBone;

    @SuppressWarnings("unchecked")
    @Override public ImplicitPlane serialize(Serializer serializer, Serializable structure) {
        ImplicitPlane plane = 
            (structure == null) ? new ImplicitPlane() : (ImplicitPlane) structure;

        plane.planeNormal = serializer.v4(plane.planeNormal);
        plane.pointInPlane = serializer.v4(plane.pointInPlane);
        plane.parentBone = serializer.i32(plane.parentBone);

        return plane;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
