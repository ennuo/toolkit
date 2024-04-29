package cwlib.structs.inventory;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class ColorCorrection implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x18;

    public float saturation = 1.0f;
    public float hueShift;
    public float brightness = 0.5f;
    public float contrast = 0.5f;
    public float tintHue, tintAmount;

    @Override
    public void serialize(Serializer serializer)
    {
        saturation = serializer.f32(saturation);
        hueShift = serializer.f32(hueShift);
        brightness = serializer.f32(brightness);
        contrast = serializer.f32(contrast);
        tintHue = serializer.f32(tintHue);
        tintAmount = serializer.f32(tintAmount);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
