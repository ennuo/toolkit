package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class GameCamera implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    @Override
    public void serialize(Serializer serializer)
    {
        serializer.v4(null); // cameraTarget
        serializer.bool(false); // validCameraTarget
    }

    @Override
    public int getAllocatedSize()
    {
        return GameCamera.BASE_ALLOCATION_SIZE;
    }
}
