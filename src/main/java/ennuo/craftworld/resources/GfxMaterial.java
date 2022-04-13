package ennuo.craftworld.resources;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.gfxmaterial.Box;
import ennuo.craftworld.resources.structs.gfxmaterial.ParameterAnimation;
import ennuo.craftworld.resources.structs.gfxmaterial.Wire;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.toolkit.utilities.Globals;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class GfxMaterial implements Serializable {
    public int flags;
    public float alphaTestLevel;
    public byte alphaLayer, alphaMode, shadowCastMode;
    public float bumpLevel, cosinePower,
    reflectionBlur, refractiveIndex,
    refractiveFresnelFalloffPower, refractiveFresnelMultiplier,
    refractiveFresnelOffset, refractiveFresnelShift;
    public byte fuzzLengthAndRefractiveFlag, translucencyDensity,
    fuzzSwirlAngle, fuzzSwirlAmplitude, fuzzLightingBias,
    fuzzLightingScale, iridescenceRoughness;
    
    int[] blobBinaryOffsets;
    byte[] ps3BinaryCode;

    public ResourceDescriptor[] textures;

    public byte[] wrapS, wrapT;

    public Box[] boxes;
    public Wire[] wires;

    public int soundEnum;

    public ParameterAnimation[] parameterAnimations;
    
    public GfxMaterial() {}
    public GfxMaterial(Resource resource) {
        Serializer serializer = new Serializer(resource.handle);
        this.serialize(serializer, this);
    }
    
    public int getBlobOffsetCount(Revision revision) {
        int sourceOffsets = 0xC;
        if ((this.flags & 0x10000) != 0)
            sourceOffsets = 0x18;
        if (revision.head < 0x3c1 || !revision.isVita() || !revision.isAfterVitaRevision(0xF))
            sourceOffsets = 0xA;
        if (revision.head < 0x393)
            sourceOffsets = 0x8;
        if (revision.head < 0x34f)
            sourceOffsets = 0x4;
        if ((revision.head < 0x2d0 && !revision.isLeerdammer()) ||
                (revision.isLeerdammer() && !revision.isAfterLeerdammerRevision(0x12)))
            sourceOffsets = 0x3;
        return sourceOffsets;
    }
    
    public GfxMaterial serialize(Serializer serializer, Serializable structure) {
        
        GfxMaterial gfxMaterial = null;
        if (structure != null) gfxMaterial = (GfxMaterial) structure;
        else gfxMaterial = new GfxMaterial();
        
        gfxMaterial.flags = serializer.i32(gfxMaterial.flags);
        gfxMaterial.alphaTestLevel = serializer.f32(gfxMaterial.alphaTestLevel);
        gfxMaterial.alphaLayer = serializer.i8(gfxMaterial.alphaLayer);
        if (serializer.revision.head > 0x2f9)
            gfxMaterial.alphaMode = serializer.i8(gfxMaterial.alphaMode);
        gfxMaterial.shadowCastMode = serializer.i8(gfxMaterial.shadowCastMode);
        gfxMaterial.bumpLevel = serializer.f32(gfxMaterial.bumpLevel);
        gfxMaterial.cosinePower = serializer.f32(gfxMaterial.cosinePower);
        gfxMaterial.reflectionBlur = serializer.f32(gfxMaterial.reflectionBlur);
        gfxMaterial.refractiveIndex = serializer.f32(gfxMaterial.refractiveIndex);
        if (serializer.revision.head > 0x13003ef) {
            gfxMaterial.refractiveFresnelFalloffPower = serializer.f32(gfxMaterial.refractiveFresnelFalloffPower);
            gfxMaterial.refractiveFresnelMultiplier = serializer.f32(gfxMaterial.refractiveFresnelMultiplier);
            gfxMaterial.refractiveFresnelOffset = serializer.f32(gfxMaterial.refractiveFresnelOffset);
            gfxMaterial.refractiveFresnelShift = serializer.f32(gfxMaterial.refractiveFresnelShift);
            gfxMaterial.fuzzLengthAndRefractiveFlag = serializer.i8(gfxMaterial.fuzzLengthAndRefractiveFlag);
            if (serializer.revision.head > 0x17703ef) {
                gfxMaterial.translucencyDensity = serializer.i8(gfxMaterial.translucencyDensity);
                gfxMaterial.fuzzSwirlAngle = serializer.i8(gfxMaterial.fuzzSwirlAngle);
                gfxMaterial.fuzzSwirlAmplitude = serializer.i8(gfxMaterial.fuzzSwirlAmplitude);
                gfxMaterial.fuzzLightingBias = serializer.i8(gfxMaterial.fuzzLightingBias);
                gfxMaterial.fuzzLightingScale = serializer.i8(gfxMaterial.fuzzLightingScale);
                gfxMaterial.iridescenceRoughness = serializer.i8(gfxMaterial.iridescenceRoughness);
            }
        }
        
        int sourceOffsets = gfxMaterial.getBlobOffsetCount(serializer.revision);
        if (serializer.isWriting) {
            for (int i = 0; i  < sourceOffsets; ++i)
                serializer.output.i32(gfxMaterial.blobBinaryOffsets[i]);
            serializer.output.i8a(gfxMaterial.ps3BinaryCode);
            for (int i = 0; i < 8; ++i)
                serializer.output.resource(gfxMaterial.textures[i]);            
        } else {
            // offsets are based on start until 0x393
            gfxMaterial.blobBinaryOffsets  = new int[sourceOffsets];
            for (int i = 0; i < sourceOffsets; ++i)
                gfxMaterial.blobBinaryOffsets[i] = serializer.input.i32();
            gfxMaterial.ps3BinaryCode = serializer.input.i8a();
            gfxMaterial.textures = new ResourceDescriptor[8];
            for (int i = 0; i < 8; ++i)
                gfxMaterial.textures[i] = serializer.input.resource(ResourceType.TEXTURE);
        }
        
        gfxMaterial.wrapS = serializer.i8a(gfxMaterial.wrapS);
        gfxMaterial.wrapT = serializer.i8a(gfxMaterial.wrapT);
        gfxMaterial.boxes = serializer.array(gfxMaterial.boxes, Box.class);
        gfxMaterial.wires = serializer.array(gfxMaterial.wires, Wire.class);
        
        if (serializer.revision.head > 0x148)
            gfxMaterial.soundEnum = serializer.i32(gfxMaterial.soundEnum);
        
        if (serializer.revision.head >= 0x2a2)
            gfxMaterial.parameterAnimations = 
                        serializer.array(gfxMaterial.parameterAnimations, ParameterAnimation.class);
        
        return gfxMaterial;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        int dataSize = 0x1000 + this.ps3BinaryCode.length;
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.GFX_MATERIAL);      
    }
    
    public Wire findWireFrom(int box) {
        for (Wire wire: this.wires)
            if (wire.boxFrom == box)
                return wire;
        return null;
    }
    
    public int getOutputBox() {
        for (int i = 0; i < this.boxes.length; ++i) {
            Box box = this.boxes[i];
            if (box.type == Box.BoxType.OUTPUT)
                return i;
        }
        return -1;
    }
    
    public Box getBoxFrom(Wire wire) {
        return this.boxes[wire.boxFrom];
    }
    
    public Box getBoxTo(Wire wire) {
        return this.boxes[wire.boxTo];
    }
    
    public byte[] extractTexture(int index) { return this.extractTexture(this.textures[index]); }
    public byte[] extractTexture(ResourceDescriptor texDescriptor) {
        byte[] data = Globals.extractFile(texDescriptor);
        if (data == null) return null;
        Texture texture = new Texture(data);
        if (texture.parsed) {
            BufferedImage image = texture.getImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            } catch (IOException ex) {
                Logger.getLogger(GfxMaterial.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }
}
