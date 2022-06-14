package cwlib.structs.gmat;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Vector4f;

public class MaterialParameterAnimation implements Serializable {
    public Vector4f baseValue;
    public float[] keys;
    public byte[] name;
    public byte componentsAnimated;
    
    public MaterialParameterAnimation serialize(Serializer serializer, Serializable structure) {
        MaterialParameterAnimation parameterAnimation  = null;
        if (structure != null) parameterAnimation = (MaterialParameterAnimation) structure;
        else parameterAnimation = new MaterialParameterAnimation();
        
        parameterAnimation.baseValue = serializer.v4(parameterAnimation.baseValue);
        parameterAnimation.keys = serializer.f32a(parameterAnimation.keys);
        parameterAnimation.name = serializer.i8a(parameterAnimation.name);
        parameterAnimation.componentsAnimated = serializer.i8(parameterAnimation.componentsAnimated);
        
        return parameterAnimation;
    }
}
