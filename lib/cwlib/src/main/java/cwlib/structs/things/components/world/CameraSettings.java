package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class CameraSettings implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public float defaultZoomMultiplier = 1, maximumZoomMultiplier = 1, zoomDelayMultiplier = 1,
        zoomSpeedMultiplier = 1;

    @GsonRevision(lbp3 = true, min = 0x36)
    public float defaultPitchMultiplier = 0.254098f, maximumPitchMultiplier = 0.254098f;

    @Override
    public void serialize(Serializer serializer)
    {
        defaultZoomMultiplier = serializer.f32(defaultZoomMultiplier);
        maximumZoomMultiplier = serializer.f32(maximumZoomMultiplier);
        zoomDelayMultiplier = serializer.f32(zoomDelayMultiplier);
        zoomSpeedMultiplier = serializer.f32(zoomSpeedMultiplier);

        int subVersion = serializer.getRevision().getSubVersion();
        if (subVersion > 0x36)
        {
            defaultPitchMultiplier = serializer.f32(defaultPitchMultiplier);
            maximumPitchMultiplier = serializer.f32(maximumPitchMultiplier);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return CameraSettings.BASE_ALLOCATION_SIZE;
    }
}
