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
    public static final int MAX_TEXTURES = 8;
    public static final int MAX_WRAPS = 8;
    public static final int UV_OFFSETS = 0x10;
    public static final int UV_SCALES = 0x8;
    
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

    public ResourceDescriptor[] textures = new ResourceDescriptor[MAX_TEXTURES];

    public byte[] wrapS = new byte[MAX_WRAPS], wrapT = new byte[MAX_WRAPS];

    public Box[] boxes;
    public Wire[] wires;

    public int soundEnum;

    public ParameterAnimation[] parameterAnimations;
    
    public short[] uvOffsets = new short[UV_OFFSETS];
    public short[] uvScales = new short[UV_SCALES];

    public byte[] cycleCount = new byte[2];
    public byte[] conditionalTexLookups = new byte[2];
    public byte[] unconditionalTexLookups = new byte[2];
    public byte[] nonDependentTexLookups = new byte[2];
    
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
        GfxMaterial gmat = (structure == null) ? new GfxMaterial() : (GfxMaterial) structure;
        
        gmat.flags = serializer.i32(gmat.flags);
        gmat.alphaTestLevel = serializer.f32(gmat.alphaTestLevel);
        gmat.alphaLayer = serializer.i8(gmat.alphaLayer);
        if (serializer.revision.head > 0x2f9)
            gmat.alphaMode = serializer.i8(gmat.alphaMode);
        gmat.shadowCastMode = serializer.i8(gmat.shadowCastMode);
        gmat.bumpLevel = serializer.f32(gmat.bumpLevel);
        gmat.cosinePower = serializer.f32(gmat.cosinePower);
        gmat.reflectionBlur = serializer.f32(gmat.reflectionBlur);
        gmat.refractiveIndex = serializer.f32(gmat.refractiveIndex);
        if (serializer.revision.isAfterLBP3Revision(0x139)) {
            gmat.refractiveFresnelFalloffPower = serializer.f32(gmat.refractiveFresnelFalloffPower);
            gmat.refractiveFresnelMultiplier = serializer.f32(gmat.refractiveFresnelMultiplier);
            gmat.refractiveFresnelOffset = serializer.f32(gmat.refractiveFresnelOffset);
            gmat.refractiveFresnelShift = serializer.f32(gmat.refractiveFresnelShift);
            if (serializer.revision.isAfterLBP3Revision(0x16a)) {
                gmat.fuzzLengthAndRefractiveFlag = serializer.i8(gmat.fuzzLengthAndRefractiveFlag);
                if (serializer.revision.isAfterLBP3Revision(0x17b)) {
                    gmat.translucencyDensity = serializer.i8(gmat.translucencyDensity);
                    gmat.fuzzSwirlAngle = serializer.i8(gmat.fuzzSwirlAngle);
                    gmat.fuzzSwirlAmplitude = serializer.i8(gmat.fuzzSwirlAmplitude);
                    gmat.fuzzLightingBias = serializer.i8(gmat.fuzzLightingBias);
                    gmat.fuzzLightingScale = serializer.i8(gmat.fuzzLightingScale);
                    gmat.iridescenceRoughness = serializer.i8(gmat.iridescenceRoughness);
                }
            }
        }
        
        int sourceOffsets = gmat.getBlobOffsetCount(serializer.revision);
        if (serializer.isWriting) {
            for (int i = 0; i  < sourceOffsets; ++i)
                serializer.output.i32(gmat.blobBinaryOffsets[i]);
            serializer.output.i8a(gmat.ps3BinaryCode);
            for (int i = 0; i < 8; ++i)
                serializer.output.resource(gmat.textures[i]);            
        } else {
            // offsets are based on start until 0x393
            gmat.blobBinaryOffsets  = new int[sourceOffsets];
            for (int i = 0; i < sourceOffsets; ++i)
                gmat.blobBinaryOffsets[i] = serializer.input.i32();
            gmat.ps3BinaryCode = serializer.input.i8a();
            gmat.textures = new ResourceDescriptor[8];
            for (int i = 0; i < 8; ++i)
                gmat.textures[i] = serializer.input.resource(ResourceType.TEXTURE);
        }
        
        gmat.wrapS = serializer.i8a(gmat.wrapS);
        gmat.wrapT = serializer.i8a(gmat.wrapT);
        gmat.boxes = serializer.array(gmat.boxes, Box.class);
        gmat.wires = serializer.array(gmat.wires, Wire.class);
        
        if (serializer.revision.head > 0x15a)
            gmat.soundEnum = serializer.i32(gmat.soundEnum);
        
        if (serializer.revision.head > 0x2a1)
            gmat.parameterAnimations = 
                        serializer.array(gmat.parameterAnimations, ParameterAnimation.class);
        
        if (serializer.revision.isAfterVitaRevision(0x18)) {
            for (int i = 0; i < UV_OFFSETS; ++i)
                gmat.uvOffsets[i] = serializer.i16(gmat.uvOffsets[i]);
            for (int i = 0; i < UV_SCALES; ++i)
                gmat.uvScales[i] = serializer.i16(gmat.uvScales[i]);
        }
        
        if (serializer.revision.isAfterVitaRevision(0x3)) {
            gmat.cycleCount[0] = serializer.i8(gmat.cycleCount[0]);
            gmat.cycleCount[1] = serializer.i8(gmat.cycleCount[1]);

            gmat.conditionalTexLookups[0] = serializer.i8(gmat.conditionalTexLookups[0]);
            gmat.conditionalTexLookups[1] = serializer.i8(gmat.conditionalTexLookups[1]);

            gmat.unconditionalTexLookups[0] = serializer.i8(gmat.unconditionalTexLookups[0]);
            gmat.unconditionalTexLookups[1] = serializer.i8(gmat.unconditionalTexLookups[1]);

            gmat.nonDependentTexLookups[0] = serializer.i8(gmat.nonDependentTexLookups[0]);
            gmat.nonDependentTexLookups[1] = serializer.i8(gmat.nonDependentTexLookups[1]);
        }
        
        
        return gmat;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        int dataSize = 0x2000 + this.ps3BinaryCode.length;
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
