package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class ColorCorrection {
    public static int MAX_SIZE = 0x18;
    
    public float saturation = 1;
    public float hueShift = 0;
    public float brightness = 0.5f;
    public float contrast = 0.5f;
    public float tintHue = 0;
    public float tintAmount = 0;
    
    public ColorCorrection() {}
    public ColorCorrection(Data data) {
        saturation = data.f32();
        hueShift = data.f32();
        brightness = data.f32();
        contrast = data.f32();
        tintHue = data.f32();
        tintAmount = data.f32();
    }
    
    public void serialize(Output output) {
        output.f32(saturation);
        output.f32(hueShift);
        output.f32(brightness);
        output.f32(contrast);
        output.f32(tintHue);
        output.f32(tintAmount);
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
