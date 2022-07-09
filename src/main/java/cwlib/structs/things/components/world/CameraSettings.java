package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class CameraSettings implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public float defaultZoomMultiplier = 1, maximumZoomMultiplier = 1, zoomDelayMultiplier = 1, zoomSpeedMultiplier = 1;
    
    @SuppressWarnings("unchecked")
    @Override public CameraSettings serialize(Serializer serializer, Serializable structure) {
        CameraSettings settings = (structure == null) ? new CameraSettings() : (CameraSettings) structure;

        settings.defaultZoomMultiplier = serializer.f32(settings.defaultZoomMultiplier);
        settings.maximumZoomMultiplier = serializer.f32(settings.maximumZoomMultiplier);
        settings.zoomDelayMultiplier = serializer.f32(settings.zoomDelayMultiplier);
        settings.zoomSpeedMultiplier = serializer.f32(settings.zoomSpeedMultiplier);

        return settings;
    }

    @Override public int getAllocatedSize() { return CameraSettings.BASE_ALLOCATION_SIZE; }
}
