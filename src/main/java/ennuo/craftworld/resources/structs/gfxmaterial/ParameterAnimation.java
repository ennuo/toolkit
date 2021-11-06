package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.serializer.v2.Serializable;
import ennuo.craftworld.serializer.v2.Serializer;
import org.joml.Vector4f;

public class ParameterAnimation implements Serializable {
    public Vector4f baseValue;
    public float[] keys;
    public byte[] name;
    public byte componentsAnimated;
    
    public ParameterAnimation serialize(Serializer serializer, Serializable structure) {
        ParameterAnimation parameterAnimation  = null;
        if (structure != null) parameterAnimation = (ParameterAnimation) structure;
        else parameterAnimation = new ParameterAnimation();
        
        parameterAnimation.baseValue = serializer.v4(parameterAnimation.baseValue);
        parameterAnimation.keys = serializer.f32a(parameterAnimation.keys);
        parameterAnimation.name = serializer.i8a(parameterAnimation.name);
        parameterAnimation.componentsAnimated = serializer.i8(parameterAnimation.componentsAnimated);
        
        return parameterAnimation;
    }
}
