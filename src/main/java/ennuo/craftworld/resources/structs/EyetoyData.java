package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import java.util.Arrays;
import org.joml.Matrix4f;

public class EyetoyData {
    public static int MAX_SIZE = 0x8B + ColorCorrection.MAX_SIZE;
    
    public ResourcePtr frame;
    public ResourcePtr alphaMask;
    
    public Matrix4f colorCorrection = new Matrix4f().identity();
    ColorCorrection colorCorrectionSrc = new ColorCorrection();
    
    public ResourcePtr outline;
    
    public EyetoyData() {}
    public EyetoyData(Data data) {
        frame = data.resource(RType.TEXTURE);
        alphaMask = data.resource(RType.TEXTURE);
        colorCorrection = data.matrix();
        colorCorrectionSrc = new ColorCorrection(data);
        if (data.revision > 0x2c3)
            outline = data.resource(RType.TEXTURE);
    
    }
    
    public void serialize(Output output) {
        output.resource(frame);
        output.resource(alphaMask);
        output.matrix(colorCorrection);
        colorCorrectionSrc.serialize(output);
        if (output.revision > 0x2c3)
            output.resource(outline);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof EyetoyData)) return false;
        EyetoyData d = (EyetoyData)o;
        return (
                d.frame.equals(frame) &&
                d.alphaMask.equals(alphaMask) &&
                d.colorCorrection.equals(colorCorrection) &&
                d.colorCorrectionSrc.equals(colorCorrectionSrc) &&
                d.outline.equals(outline)
        );
    }
}
