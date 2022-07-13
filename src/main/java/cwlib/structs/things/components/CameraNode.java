package cwlib.structs.things.components;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class CameraNode implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Vector4f targetBox;
    public Vector3f pitchAngle;
    public float zoomDistance;
    public boolean localSpaceRoll;

    @SuppressWarnings("unchecked")
    @Override public CameraNode serialize(Serializer serializer, Serializable structure) {
        CameraNode node = (structure == null) ? new CameraNode() : (CameraNode) structure;

        node.targetBox = serializer.v4(node.targetBox);
        node.pitchAngle = serializer.v3(node.pitchAngle);
        node.zoomDistance = serializer.f32(node.zoomDistance);
        node.localSpaceRoll = serializer.bool(node.localSpaceRoll);

        return node;
    }

    @Override public int getAllocatedSize() { return CameraNode.BASE_ALLOCATION_SIZE; }
}
