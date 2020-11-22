package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.gfxmaterial.Box;
import ennuo.craftworld.resources.structs.gfxmaterial.ParameterAnimation;
import ennuo.craftworld.resources.structs.gfxmaterial.Wire;

public class GfxMaterial {
    int flags;
    float alphaTestLevel;
    byte alphaLayer, alphaMode, shadowCastMode;
    float bumpLevel, cosinePower,
          reflectionBlur, refractiveIndex,
          refractiveFresnelFalloffPower, refractiveFresnelMultiplier,
          refractiveFresnelOffset, refractiveFresnelShift;
    byte fuzzLengthAndRefractiveFlag, translucencyDensity,
         fuzzSwirlAngle, fuzzSwirlAmplitude, fuzzLightingBias,
         fuzzLightingScale, iridescenceRoughness;
    
    byte[][] shaders;
    ResourcePtr[] textures;
         
    byte[] wrapS, wrapT;
    
    Box[] boxes;
    Wire[] wires;
    
    int soundEnum;
    
    ParameterAnimation[] parameterAnimations;
    
    public GfxMaterial(Data data) {
        flags = data.int32();
        
        alphaTestLevel = data.float32();
        alphaLayer = data.int8();
        if (data.revision > 0x272)
            alphaMode = data.int8();
        shadowCastMode = data.int8();
        
        bumpLevel = data.float32();
        cosinePower = data.float32();
        reflectionBlur = data.float32();
        refractiveIndex = data.float32();
        
        if (data.revision > 0x3f8) {
            refractiveFresnelFalloffPower = data.float32();
            refractiveFresnelMultiplier = data.float32();
            refractiveFresnelOffset = data.float32();
            refractiveFresnelShift = data.float32();
        
            fuzzLengthAndRefractiveFlag = data.int8();
            translucencyDensity = data.int8();
            fuzzSwirlAngle = data.int8();
            fuzzSwirlAmplitude = data.int8();
            fuzzLightingBias = data.int8();
            fuzzLightingScale = data.int8();
            iridescenceRoughness = data.int8();
        }
        
        int[] offsets = new int[12];
        offsets[0] = 0;
        for (int i = 1; i < 12; ++i)
            offsets[i] = data.int32();
       
        System.out.println("Shaders offset = 0x" + Bytes.toHex(data.offset));
        
        shaders = new byte[11][];
        for (int i = 1; i < 12; ++i)
            shaders[i - 1] = data.bytes(offsets[i] - offsets[i - 1]);
        
        textures = new ResourcePtr[8];
        for (int i = 0; i < 8; ++i) {
            textures[i] = data.resource(RType.TEXTURE);
            String str;
            if (textures[i] != null) str = textures[i].toString();
            else str = "null";
            System.out.println("Texture [" + i + "]  offset = 0x" + Bytes.toHex(data.offset) + ", value = " + str);
        }
        
        System.out.println("WrapS offset = 0x" + Bytes.toHex(data.offset));
        wrapS = new byte[data.int32()];
        for (int i = 0; i < wrapS.length; ++i)
            wrapS[i] = data.int8();
        System.out.println("WrapT offset = 0x" + Bytes.toHex(data.offset));
        wrapT = new byte[data.int32()];
        for (int i = 0; i < wrapT.length; ++i)
            wrapT[i] = data.int8();
        
        System.out.println("Boxes offset = 0x" + Bytes.toHex(data.offset));
        boxes = Box.array(data);
        System.out.println("Wires offset = 0x" + Bytes.toHex(data.offset));
        wires = Wire.array(data);
        
        // FINISH LATER //
        
        System.out.println("0x" + Bytes.toHex(data.offset));
    }
    
}
