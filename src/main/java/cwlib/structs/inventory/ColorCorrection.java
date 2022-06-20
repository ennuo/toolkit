package cwlib.structs.inventory;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class ColorCorrection implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x18;

    public float saturation = 1.0f;
    public float hueShift;
    public float brightness = 0.5f;
    public float contrast = 0.5f;
    public float tintHue, tintAmount;

    @SuppressWarnings("unchecked")
    @Override public ColorCorrection serialize(Serializer serializer, Serializable structure) {
        ColorCorrection cc = 
            (structure == null) ? new ColorCorrection() : (ColorCorrection) structure;
        
        cc.saturation = serializer.f32(cc.saturation);
        cc.hueShift = serializer.f32(cc.hueShift);
        cc.brightness = serializer.f32(cc.brightness);
        cc.contrast = serializer.f32(cc.contrast);
        cc.tintHue = serializer.f32(cc.tintHue);
        cc.tintAmount = serializer.f32(cc.tintAmount);

        return cc;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
