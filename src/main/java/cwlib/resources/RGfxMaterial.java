package cwlib.resources;

import java.util.ArrayList;
import java.util.Arrays;

import org.joml.Vector2f;
import org.joml.Vector4f;

import cwlib.enums.AudioMaterial;
import cwlib.enums.BoxType;
import cwlib.enums.Branch;
import cwlib.enums.GfxMaterialFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.enums.ShadowCastMode;
import cwlib.enums.TextureWrap;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialParameterAnimation;
import cwlib.structs.gmat.MaterialWire;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import executables.gfx.GfxAssembler.BrdfPort;

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

    public static final Vector4f SPECULAR_COLOR = new Vector4f(0.09f, 0.09f, 0.09f, 1.0f);
    // public static final Vector4f SPECULAR_COLOR = new Vector4f(126.43f, 932.4f, 6421.2f, 632.156f);

    public int flags = GfxMaterialFlags.DEFAULT;
    public float alphaTestLevel = 0.5f;
    public byte alphaLayer, alphaMode;
    public ShadowCastMode shadowCastMode = ShadowCastMode.ON;
    public float bumpLevel = 0.2f, cosinePower = 1.0f;
    public float reflectionBlur = 1.0f, refractiveIndex = 0.01f;

    public float refractiveFresnelFalloffPower = 1.0f;
    public float refractiveFresnelMultiplier = 1.0f;
    public float refractiveFresnelOffset;
    public float refractiveFresnelShift;

    public byte fuzzLengthAndRefractiveFlag;
    public byte translucencyDensity = -1;
    public byte fuzzSwirlAngle = -1;
    public byte fuzzSwirlAmplitude;
    public byte fuzzLightingBias = 127;
    public byte fuzzLightingScale = 127;
    public byte iridesenceRoughness;

    public byte[][] shaders;
    public byte[] code;

    public ResourceDescriptor[] textures = new ResourceDescriptor[MAX_TEXTURES];

    public TextureWrap[] wrapS;
    public TextureWrap[] wrapT;

    public ArrayList<MaterialBox> boxes = new ArrayList<>();
    public ArrayList<MaterialWire> wires = new ArrayList<>();

    public AudioMaterial soundEnum = AudioMaterial.NONE;

    public MaterialParameterAnimation[] parameterAnimations;

    /* PS Vita specific fields */

    public short[] uvOffsets = new short[UV_OFFSETS];
    public short[] uvScales = new short[UV_SCALES];

    public byte[] cycleCount = new byte[PERF_DATA];
    public byte[] conditionalTexLookups = new byte[PERF_DATA];
    public byte[] unconditionalTexLookups = new byte[PERF_DATA];
    public byte[] nonDependentTexLookups = new byte[PERF_DATA];

    public RGfxMaterial() {
        this.wrapS = new TextureWrap[MAX_TEXTURES];
        this.wrapT = new TextureWrap[MAX_TEXTURES];
        for (int i = 0; i < 8; ++i) {
            this.wrapS[i] = TextureWrap.WRAP;
            this.wrapT[i] = TextureWrap.WRAP;
        }
    }

    @SuppressWarnings("unchecked")
    @Override public RGfxMaterial serialize(Serializer serializer, Serializable structure) {
        RGfxMaterial gmat = (structure == null) ? new RGfxMaterial() : (RGfxMaterial) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        gmat.flags = serializer.i32(gmat.flags);
        gmat.alphaTestLevel = serializer.f32(gmat.alphaTestLevel);
        gmat.alphaLayer = serializer.i8(gmat.alphaLayer);
        if (version >= Revisions.GFXMATERIAL_ALPHA_MODE)
            gmat.alphaMode = serializer.i8(gmat.alphaMode);
        gmat.shadowCastMode = serializer.enum8(gmat.shadowCastMode);
        gmat.bumpLevel = serializer.f32(gmat.bumpLevel);
        gmat.cosinePower = serializer.f32(gmat.cosinePower);
        gmat.reflectionBlur = serializer.f32(gmat.reflectionBlur);
        gmat.refractiveIndex = serializer.f32(gmat.refractiveIndex);

        if (subVersion >= Revisions.FRESNEL) {
            gmat.refractiveFresnelFalloffPower = serializer.f32(gmat.refractiveFresnelFalloffPower);
            gmat.refractiveFresnelMultiplier = serializer.f32(gmat.refractiveFresnelMultiplier);
            gmat.refractiveFresnelOffset = serializer.f32(gmat.refractiveFresnelOffset);
            gmat.refractiveFresnelShift = serializer.f32(gmat.refractiveFresnelShift);
            if (subVersion >= Revisions.FUZZ) {
                gmat.fuzzLengthAndRefractiveFlag = serializer.i8(gmat.fuzzLengthAndRefractiveFlag);
                if (subVersion >= Revisions.FUZZ_LIGHTING) {
                    gmat.translucencyDensity = serializer.i8(gmat.translucencyDensity);
                    gmat.fuzzSwirlAngle = serializer.i8(gmat.fuzzSwirlAngle);
                    gmat.fuzzSwirlAmplitude = serializer.i8(gmat.fuzzSwirlAmplitude);
                    gmat.fuzzLightingBias = serializer.i8(gmat.fuzzLightingBias);
                    gmat.fuzzLightingScale = serializer.i8(gmat.fuzzLightingScale);
                    gmat.iridesenceRoughness = serializer.i8(gmat.iridesenceRoughness);
                }
            }
        }

        boolean serializeCode = !revision.isToolkit() || revision.before(Branch.MIZUKI, Revisions.MZ_REMOVE_GFX_CODE);
        int sourceOffsets = gmat.getBlobOffsetCount(revision);
        if (serializer.isWriting()) {
            if (serializeCode) {
                MemoryOutputStream stream = serializer.getOutput();
                int offset = 0;
                for (int i = 0; i < sourceOffsets; ++i) {
                    byte[] shader = gmat.shaders[i];
                    if (version >= 0x34f)
                        offset += shader.length;
                    stream.i32(offset);
                    if (version < 0x34f)
                        offset += shader.length;
                }
                if (this.code != null) offset += this.code.length;
                stream.i32(offset);
                for (byte[] shader : gmat.shaders) stream.bytes(shader);
                if (this.code != null)
                    stream.bytes(this.code);
            }
            for (int i = 0; i < MAX_TEXTURES; ++i)
                serializer.resource(gmat.textures[i], ResourceType.TEXTURE);
        } else {
            if (serializeCode) {
                MemoryInputStream stream = serializer.getInput();
                int[] blobOffsets  = new int[sourceOffsets];
                for (int i = 0; i < sourceOffsets; ++i)
                    blobOffsets[i] = stream.i32();
                byte[] code = stream.bytearray();
    
                gmat.shaders = new byte[sourceOffsets][];
                if (version < 0x34f) {
                    for (int i = 1; i < sourceOffsets; ++i)
                        gmat.shaders[i - 1] = Arrays.copyOfRange(code, blobOffsets[i - 1], blobOffsets[i]);
                    gmat.shaders[sourceOffsets - 1] = Arrays.copyOfRange(code, blobOffsets[sourceOffsets - 1], code.length);
                } else {
                    int offset = 0;
                    for (int i = 0; i < sourceOffsets; ++i) {
                        gmat.shaders[i] = Arrays.copyOfRange(code, offset, blobOffsets[i]);
                        offset += gmat.shaders[i].length;
                    }
                    if (offset != code.length)
                        gmat.code = Arrays.copyOfRange(code, offset, code.length);
                }
            }
            
            gmat.textures = new ResourceDescriptor[MAX_TEXTURES];
            for (int i = 0; i < MAX_TEXTURES; ++i)
                gmat.textures[i] = serializer.resource(null, ResourceType.TEXTURE);
        }

        gmat.wrapS = serializer.enumarray(gmat.wrapS, TextureWrap.class);
        gmat.wrapT = serializer.enumarray(gmat.wrapT, TextureWrap.class);
        gmat.boxes = serializer.arraylist(gmat.boxes, MaterialBox.class);
        gmat.wires = serializer.arraylist(gmat.wires, MaterialWire.class);

        if (version >= Revisions.GFXMATERIAL_SOUND_ENUM)
            gmat.soundEnum = serializer.enum32(gmat.soundEnum);

        if (version >= Revisions.PARAMETER_ANIMATIONS)
            gmat.parameterAnimations = serializer.array(gmat.parameterAnimations, MaterialParameterAnimation.class);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_UV_OFFSCALE)) {
            for (int i = 0; i < UV_OFFSETS; ++i)
                gmat.uvOffsets[i] = serializer.i16(gmat.uvOffsets[i]);
            for (int i = 0; i < UV_SCALES; ++i)
                gmat.uvScales[i] = serializer.i16(gmat.uvScales[i]);
        }
        
        if (revision.has(Branch.DOUBLE11, Revisions.D1_PERFDATA)) {
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
        if (this.shaders != null) 
            for (byte[] shader : this.shaders)
                size += shader.length;
        if (this.code != null)
            size += this.code.length;
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
        if (head < 0x3c1 || !revision.isVita() || revision.before(Branch.DOUBLE11, Revisions.D1_SHADER))
            sourceOffsets = 0xA;
        if (head < 0x393)
            sourceOffsets = 0x8;
        if (head < 0x34f)
            sourceOffsets = 0x4;
        if ((head < 0x2d0 && !revision.isLeerdammer()) || revision.before(Branch.LEERDAMMER, Revisions.LD_SHADER))
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
        for (int i = 0; i < this.boxes.size(); ++i) {
            MaterialBox box = this.boxes.get(i);
            if (box.type == BoxType.OUTPUT)
                return i;
        }
        return -1;
    }

    public int getBoxIndex(MaterialBox box) {
        for (int i = 0; i < this.boxes.size(); ++i)
            if (box == this.boxes.get(i)) return i;
        return -1;
    }

    public MaterialWire getWireConnectedToPort(MaterialBox box, int port) { 
        return this.getWireConnectedToPort(this.getBoxIndex(box), port); 
    }

    public MaterialWire getWireConnectedToPort(int box, int port) { 
        for (MaterialWire wire : this.wires) {
            if (wire.boxTo == box && (wire.portTo & 0xff) == port)
                return wire;
        }
        return null;
    }

    public MaterialBox[] getBoxesConnected(MaterialBox box) { return this.getBoxesConnected(this.getBoxIndex(box)); }
    public MaterialBox[] getBoxesConnected(int box) {
        ArrayList<MaterialBox> boxes = new ArrayList<>();
        for (MaterialWire wire : this.wires) {
            if (wire.boxTo == box)
                boxes.add(this.boxes.get(wire.boxFrom));
        }
        return boxes.toArray(MaterialBox[]::new);
    }
    
    public MaterialBox getBoxConnectedToPort(MaterialBox box, int port) {
        return this.getBoxConnectedToPort(this.getBoxIndex(box), port);
    }
    
    public MaterialBox getBoxConnectedToPort(int box, int port) {
        for (MaterialWire wire : this.wires) {
            if (wire.boxTo == box && (wire.portTo & 0xff) == port)
                return this.boxes.get(wire.boxFrom);
        }
        return null;
    }

    public MaterialBox getBoxFrom(MaterialWire wire) { return this.boxes.get(wire.boxFrom); }
    public MaterialBox getBoxTo(MaterialWire wire) { return this.boxes.get(wire.boxTo); }

    public static RGfxMaterial getBumpLayout(
        Vector4f diffuseTransform,
        Vector4f bumpTransform,
        ResourceDescriptor diffuse,
        ResourceDescriptor bump,
        boolean doubleSided,
        boolean alphaClip
    ) {
        RGfxMaterial gfx = new RGfxMaterial();
        gfx.textures[0] = diffuse;
        gfx.textures[1] = bump;
        gfx.boxes.add(new MaterialBox());
        gfx.boxes.add(new MaterialBox(diffuseTransform, 0, 0));
        gfx.boxes.add(new MaterialBox(SPECULAR_COLOR));
        gfx.boxes.add(new MaterialBox(bumpTransform, 0, 1));

        gfx.wires.add(new MaterialWire(1, 0, 0, BrdfPort.DIFFUSE));
        gfx.wires.add(new MaterialWire(2, 0, 0, BrdfPort.SPECULAR));
        gfx.wires.add(new MaterialWire(3, 0, 0, BrdfPort.BUMP));
        
        gfx.flags = GfxMaterialFlags.DEFAULT;
        if (doubleSided)
            gfx.flags |= GfxMaterialFlags.TWO_SIDED;

        if (alphaClip)
            gfx.wires.add(new MaterialWire(1, 0, 0, BrdfPort.ALPHA_CLIP));
        
        return gfx;
    }

    public static RGfxMaterial getDiffuseLayout(Vector4f transform, ResourceDescriptor texture, boolean doubleSided, boolean alphaClip) {
        RGfxMaterial gfx = new RGfxMaterial();
        gfx.textures[0] = texture;
        gfx.boxes.add(new MaterialBox());
        gfx.boxes.add(new MaterialBox(transform, 0, 0));
        gfx.boxes.add(new MaterialBox(SPECULAR_COLOR));

        gfx.wires.add(new MaterialWire(1, 0, 0, BrdfPort.DIFFUSE));
        gfx.wires.add(new MaterialWire(2, 0, 0, BrdfPort.SPECULAR));
        
        gfx.flags = GfxMaterialFlags.DEFAULT;
        if (doubleSided)
            gfx.flags |= GfxMaterialFlags.TWO_SIDED;

        if (alphaClip)
            gfx.wires.add(new MaterialWire(1, 0, 0, BrdfPort.ALPHA_CLIP));
        
        return gfx;
    }
}
