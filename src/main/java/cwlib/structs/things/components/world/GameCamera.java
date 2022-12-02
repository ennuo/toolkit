package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class GameCamera implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    @SuppressWarnings("unchecked")
    @Override public GameCamera serialize(Serializer serializer, Serializable structure) {
        GameCamera camera = (structure == null) ? new GameCamera() : (GameCamera) structure;

        serializer.v4(null); // cameraTarget
        serializer.bool(false); // validCameraTarget
        

        
        return camera;
    }

    @Override public int getAllocatedSize() { return GameCamera.BASE_ALLOCATION_SIZE; }
}
