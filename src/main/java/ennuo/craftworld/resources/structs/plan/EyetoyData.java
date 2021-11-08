package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Matrix4f;

public class EyetoyData implements Serializable {
    public static int MAX_SIZE = 0x8B + ColorCorrection.MAX_SIZE;
    
    public ResourceDescriptor frame;
    public ResourceDescriptor alphaMask;
    
    public Matrix4f colorCorrection = new Matrix4f().identity();
    ColorCorrection colorCorrectionSrc = new ColorCorrection();
    
    public ResourceDescriptor outline;
    
    public EyetoyData serialize(Serializer serializer, Serializable structure) {
        EyetoyData eyetoy = (structure == null) ? new EyetoyData() : (EyetoyData) structure;
        
        eyetoy.frame = serializer.resource(eyetoy.frame, ResourceType.TEXTURE);
        eyetoy.alphaMask = serializer.resource(eyetoy.alphaMask, ResourceType.TEXTURE);
        eyetoy.colorCorrection = serializer.matrix(eyetoy.colorCorrection);
        eyetoy.colorCorrectionSrc = serializer.struct(eyetoy.colorCorrectionSrc, ColorCorrection.class);
        if (serializer.revision > 0x2c3)
            eyetoy.outline = serializer.resource(eyetoy.outline, ResourceType.TEXTURE);
        
        return eyetoy;
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
