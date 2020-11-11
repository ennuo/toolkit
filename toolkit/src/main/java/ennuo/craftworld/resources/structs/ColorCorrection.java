package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class ColorCorrection {
    public static int MAX_SIZE = 0x18;
    
    public float saturation, hueShift,
    brightness, contrast, tintHue,
    tintAmount;
    
    
    public ColorCorrection() {}
    public ColorCorrection(Data data) {
        saturation = data.float32();
        hueShift = data.float32();
        brightness = data.float32();
        contrast = data.float32();
        tintHue = data.float32();
        tintAmount = data.float32();
    }
    
    public void serialize(Output output) {
        output.float32(saturation);
        output.float32(hueShift);
        output.float32(brightness);
        output.float32(contrast);
        output.float32(tintHue);
        output.float32(tintAmount);
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
