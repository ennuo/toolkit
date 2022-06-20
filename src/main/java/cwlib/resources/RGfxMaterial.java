package cwlib.resources;

import cwlib.enums.BoxType;
import cwlib.enums.GfxMaterialFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.enums.ShadowCastMode;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialParameterAnimation;
import cwlib.structs.gmat.MaterialWire;
import cwlib.types.data.ResourceReference;
import cwlib.types.data.Revision;

/**
 * Resource that controls how meshes get rendered,
 * stores a collection of CG/GXP/OrbShdr's as well
 * as material animation data.
 */
public class RGfxMaterial implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x250;
    public static final int MAX_TEXTURES = 8;
    public static final int MAX_WRAPS = 8;
    public static final int UV_OFFSETS = 0x10;
    public static final int UV_SCALES = 0x8;
    public static final int PERF_DATA = 0x2;

    public int flags = GfxMaterialFlags.DEFAULT;
    public float alphaTestLevel = 0.5f;
    public byte alphaLayer, alphaMode;
    public ShadowCastMode shadowCastMode = ShadowCastMode.ON;
    public float bumpLevel = 1.0f, cosinePower = 1.0f;
    public float reflectionBlur = 1.0f, refractiveIndex = 0.01f;

    public float refractiveFresnelFalloffPower = 1.0f;
    public float refractiveFresnelMultiplier = 1.0f;
    public float refractiveFresnelOffset;
    public float refractiveFresnelShift;

    public byte fuzzLengthAndRefractiveFlag;
    public byte translucencyDensity;
    public byte fuzzSwirlAngle = -1;
    public byte fuzzSwirlAmplitude;
    public byte fuzzLightingBias = 127;
    public byte fuzzLightingScale = 127;
    public byte iridesenceRoughness;

    public transient int[] blobOffsets; 
    public transient byte[] code;

    public ResourceReference[] textures = new ResourceReference[MAX_TEXTURES];

    public byte[] wrapS = { 1, 1, 1, 1, 1, 1, 1, 1 };
    public byte[] wrapT = { 1, 1, 1, 1, 1, 1, 1, 1 };

    public MaterialBox[] boxes;
    public MaterialWire[] wires;

    public int soundEnum;

    public MaterialParameterAnimation[] parameterAnimations;

    /* PS Vita specific fields */

    public short[] uvOffsets = new short[UV_OFFSETS];
    public short[] uvScales = new short[UV_SCALES];

    public byte[] cycleCount = new byte[PERF_DATA];
    public byte[] conditionalTexLookups = new byte[PERF_DATA];
    public byte[] unconditionalTexLookups = new byte[PERF_DATA];
    public byte[] nonDependentTexLookups = new byte[PERF_DATA];

    @SuppressWarnings("unchecked")
    @Override public RGfxMaterial serialize(Serializer serializer, Serializable structure) {
        RGfxMaterial gmat = (structure == null) ? new RGfxMaterial() : (RGfxMaterial) structure;

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        gmat.flags = serializer.i32(gmat.flags);
        gmat.alphaTestLevel = serializer.f32(gmat.alphaTestLevel);
        gmat.alphaLayer = serializer.i8(gmat.alphaLayer);
        if (head > 0x2f9)
            gmat.alphaMode = serializer.i8(gmat.alphaMode);
        gmat.shadowCastMode = serializer.enum8(gmat.shadowCastMode);
        gmat.bumpLevel = serializer.f32(gmat.bumpLevel);
        gmat.cosinePower = serializer.f32(gmat.cosinePower);
        gmat.reflectionBlur = serializer.f32(gmat.reflectionBlur);
        gmat.refractiveIndex = serializer.f32(gmat.refractiveIndex);

        if (revision.isAfterLBP3Revision(0x139)) {
            gmat.refractiveFresnelFalloffPower = serializer.f32(gmat.refractiveFresnelFalloffPower);
            gmat.refractiveFresnelMultiplier = serializer.f32(gmat.refractiveFresnelMultiplier);
            gmat.refractiveFresnelOffset = serializer.f32(gmat.refractiveFresnelOffset);
            gmat.refractiveFresnelShift = serializer.f32(gmat.refractiveFresnelShift);
            if (revision.isAfterLBP3Revision(0x16a)) {
                gmat.fuzzLengthAndRefractiveFlag = serializer.i8(gmat.fuzzLengthAndRefractiveFlag);
                if (revision.isAfterLBP3Revision(0x17b)) {
                    gmat.translucencyDensity = serializer.i8(gmat.translucencyDensity);
                    gmat.fuzzSwirlAngle = serializer.i8(gmat.fuzzSwirlAngle);
                    gmat.fuzzSwirlAmplitude = serializer.i8(gmat.fuzzSwirlAmplitude);
                    gmat.fuzzLightingBias = serializer.i8(gmat.fuzzLightingBias);
                    gmat.fuzzLightingScale = serializer.i8(gmat.fuzzLightingScale);
                    gmat.iridesenceRoughness = serializer.i8(gmat.iridesenceRoughness);
                }
            }
        }

        int sourceOffsets = gmat.getBlobOffsetCount(revision);
        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            for (int i = 0; i  < sourceOffsets; ++i)
                stream.i32(gmat.blobOffsets[i]);
            stream.bytearray(gmat.code);
            for (int i = 0; i < MAX_TEXTURES; ++i)
                serializer.resource(gmat.textures[i], ResourceType.TEXTURE);
        } else {
            MemoryInputStream stream = serializer.getInput();
            gmat.blobOffsets  = new int[sourceOffsets];
            for (int i = 0; i < sourceOffsets; ++i)
                gmat.blobOffsets[i] = stream.i32();
            gmat.code = stream.bytearray();
            gmat.textures = new ResourceReference[MAX_TEXTURES];
            for (int i = 0; i < MAX_TEXTURES; ++i)
                gmat.textures[i] = serializer.resource(null, ResourceType.TEXTURE);
        }

        gmat.wrapS = serializer.bytearray(gmat.wrapS);
        gmat.wrapT = serializer.bytearray(gmat.wrapT);
        gmat.boxes = serializer.array(gmat.boxes, MaterialBox.class);
        gmat.wires = serializer.array(gmat.wires, MaterialWire.class);

        if (head > 0x15a)
            gmat.soundEnum = serializer.i32(gmat.soundEnum);

        if (head > 0x2a1)
            gmat.parameterAnimations = serializer.array(gmat.parameterAnimations, MaterialParameterAnimation.class);

        if (revision.isAfterVitaRevision(0x18)) {
            for (int i = 0; i < UV_OFFSETS; ++i)
                gmat.uvOffsets[i] = serializer.i16(gmat.uvOffsets[i]);
            for (int i = 0; i < UV_SCALES; ++i)
                gmat.uvScales[i] = serializer.i16(gmat.uvScales[i]);
        }
        
        if (revision.isAfterVitaRevision(0x3)) {
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

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.code != null) size += this.code.length;
        if (this.boxes != null)
            for (MaterialBox box : this.boxes)
                size += box.getAllocatedSize();
        if (this.wires != null)
            for (MaterialWire wire : this.wires)
                size += wire.getAllocatedSize();
        if (this.parameterAnimations != null)
            for (MaterialParameterAnimation animation : parameterAnimations)
                size += animation.getAllocatedSize();
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RGfxMaterial.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.GFX_MATERIAL,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    /**
     * Gets the number of shader blob offsets in this material.
     * @param revision Revision of the resource
     * @return Number of blob offsets
     */
    public int getBlobOffsetCount(Revision revision) {
        int head = revision.getVersion();
        int sourceOffsets = 0xC;
        if ((this.flags & 0x10000) != 0)
            sourceOffsets = 0x18;
        if (head < 0x3c1 || !revision.isVita() || !revision.isAfterVitaRevision(0xF))
            sourceOffsets = 0xA;
        if (head < 0x393)
            sourceOffsets = 0x8;
        if (head < 0x34f)
            sourceOffsets = 0x4;
        if ((head < 0x2d0 && !revision.isLeerdammer()) ||
                (revision.isLeerdammer() && !revision.isAfterLeerdamerRevision(0x12)))
            sourceOffsets = 0x3;
        return sourceOffsets;
    }

    /**
     * Finds the output wire of a specified box
     * @param box Index of box
     * @return Output wire if it exists
     */
    public MaterialWire findWireFrom(int box) {
        for (MaterialWire wire : this.wires)
            if (wire.boxFrom == box)
                return wire;
        return null;
    }

    /**
     * Gets the index of the shader output box
     * @return Index of output box
     */
    public int getOutputBox() {
        for (int i = 0; i < this.boxes.length; ++i) {
            MaterialBox box = this.boxes[i];
            if (box.type == BoxType.OUTPUT)
                return i;
        }
        return -1;
    }

    public MaterialBox getBoxFrom(MaterialWire wire) { return this.boxes[wire.boxFrom]; }
    public MaterialBox getBoxTo(MaterialWire wire) { return this.boxes[wire.boxTo]; }
}
