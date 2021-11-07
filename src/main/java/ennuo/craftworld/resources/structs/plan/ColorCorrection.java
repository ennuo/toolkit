package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.serializer.Serializable;

public class ColorCorrection implements Serializable {
    public static int MAX_SIZE = 0x18;
    
    public float saturation = 1;
    public float hueShift = 0;
    public float brightness = 0.5f;
    public float contrast = 0.5f;
    public float tintHue = 0;
    public float tintAmount = 0;
    
    public ColorCorrection serialize(Serializer serializer, Serializable structure) {
        ColorCorrection cc = (structure == null) ? new ColorCorrection() : (ColorCorrection) structure;
        
        cc.saturation = serializer.f32(cc.saturation);
        cc.hueShift = serializer.f32(cc.hueShift);
        cc.brightness = serializer.f32(cc.brightness);
        cc.contrast = serializer.f32(cc.contrast);
        cc.tintHue = serializer.f32(cc.tintHue);
        cc.tintAmount = serializer.f32(cc.tintAmount);
        
        return cc;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ColorCorrection)) return false;
        ColorCorrection d = (ColorCorrection)o;
        return (
                saturation == d.saturation &&
                hueShift == d.hueShift &&
                brightness == d.brightness &&
                contrast == d.contrast &&
                tintHue == d.tintHue &&
                tintAmount == d.tintAmount
        );
    }
}
