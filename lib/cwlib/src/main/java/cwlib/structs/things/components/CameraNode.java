package cwlib.structs.things.components;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class CameraNode implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Vector4f targetBox;
    public Vector3f pitchAngle;
    public float zoomDistance;
    public boolean localSpaceRoll;

    @Override
    public void serialize(Serializer serializer)
    {
        targetBox = serializer.v4(targetBox);
        pitchAngle = serializer.v3(pitchAngle);
        zoomDistance = serializer.f32(zoomDistance);
        localSpaceRoll = serializer.bool(localSpaceRoll);
    }

    @Override
    public int getAllocatedSize()
    {
        return CameraNode.BASE_ALLOCATION_SIZE;
    }
}
