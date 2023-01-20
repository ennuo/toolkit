package cwlib.resources.custom;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.ByteArrayInputStream;

import org.joml.Vector3f;
import org.joml.Vector4f;

import gr.zdimensions.jsquish.Squish.CompressionType;

import cwlib.enums.Branch;
import cwlib.enums.CacheFlags;
import cwlib.enums.GfxMaterialFlags;
import cwlib.enums.ParameterType;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RGfxMaterial;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.custom.CachedShader;
import cwlib.structs.custom.ParameterOffset;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialWire;
import cwlib.types.Resource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.GatherData;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import cwlib.util.Images;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import executables.gfx.GfxAssembler.BrdfPort;

public class RShaderCache implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public static RShaderCache LBP1 = 
        new Resource(FileIO.getResourceFile("/binary/caches/lbp1.shadercache")).loadResource(RShaderCache.class);
    public static RShaderCache LBP2 = 
        new Resource(FileIO.getResourceFile("/binary/caches/lbp2.shadercache")).loadResource(RShaderCache.class);

    @GsonRevision(branch=0x4d5a, min=Revisions.MZ_CGC_ORBIS)
    private boolean orbis;
    
    private ArrayList<CachedShader> shaders = new ArrayList<>();
    private HashMap<Integer, CachedShader> lookup = new HashMap<>();

    public RShaderCache() { this.orbis = false; };
    public RShaderCache(boolean orbis) { this.orbis = orbis; }

    @SuppressWarnings("unchecked")
    @Override public RShaderCache serialize(Serializer serializer, Serializable structure) {
        RShaderCache cache = (structure == null) ? new RShaderCache() : (RShaderCache) structure;

        if (serializer.getRevision().has(Branch.MIZUKI, Revisions.MZ_CGC_ORBIS))
            cache.orbis = serializer.bool(cache.orbis);

        cache.shaders = serializer.arraylist(cache.shaders, CachedShader.class);
        if (!serializer.isWriting()) {
            for (CachedShader shader : this.shaders)
                this.lookup.put(shader.flags & 0xffff, shader);
        }

        return cache;
    }

    @Override public int getAllocatedSize() {
        int size = RShaderCache.BASE_ALLOCATION_SIZE;
        for (CachedShader shader : shaders)
            size += shader.getAllocatedSize();
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RShaderCache.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags, 
            ResourceType.SHADER_CACHE,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    public boolean isOrbis() { return this.orbis; }
    public ArrayList<CachedShader> getShaders() { return this.shaders; }

    private void write(byte[] cache, int offset, float value, boolean isSwizzled) {
        int bits = Float.floatToIntBits(value);
        if (isSwizzled) {
            cache[offset + 2] = (byte) (bits >>> 24);
            cache[offset + 3] = (byte) (bits >>> 16);
            cache[offset] = (byte) (bits >>> 8);
            cache[offset + 1] = (byte) (bits & 0xff);
        } else {
            cache[offset] = (byte) (bits >>> 24);
            cache[offset + 1] = (byte) (bits >>> 16);
            cache[offset + 2] = (byte) (bits >>> 8);
            cache[offset + 3] = (byte) (bits & 0xff);
        }
    }

    private void write(byte[] cache, ParameterOffset parameter, float value) {
        for (int offset : parameter.offsets) {
            boolean isSwizzled = (offset & ParameterOffset.UNSWIZZLED_FLAG) == 0;
            offset = offset & 0xffffff;
            this.write(cache, offset, value, isSwizzled);
        }
    }

    private void write(byte[] cache, ParameterOffset parameter, Vector4f value) {
        for (int offset : parameter.offsets) {
            boolean isSwizzled = (offset & ParameterOffset.UNSWIZZLED_FLAG) == 0;
            offset = offset & 0xffffff;
            switch (parameter.subType) {
                case NONE: {
                    this.write(cache, offset, value.x, isSwizzled);
                    this.write(cache, offset + 4, value.y, isSwizzled);
                    this.write(cache, offset + 8, value.z, isSwizzled);
                    this.write(cache, offset + 12, value.w, isSwizzled);
                    break;
                }
                case XY: {
                    this.write(cache, offset, value.x, isSwizzled);
                    this.write(cache, offset + 4, value.y, isSwizzled);
                    break;
                }
                case ZW: {
                    this.write(cache, offset, value.z, isSwizzled);
                    this.write(cache, offset + 4, value.w, isSwizzled);
                    break;
                }
                case Z: {
                    this.write(cache, offset, value.z, isSwizzled);
                    break;
                }
            }
        }
    }

    private void write(byte[] cache, ParameterOffset[] parameters, Vector4f value) {
        for (ParameterOffset parameter : parameters)
            this.write(cache, parameter, value);
    }

    private void write(byte[] cache, ParameterOffset[] parameters, float value) {
        for (ParameterOffset parameter : parameters)
            this.write(cache, parameter, value);
    }

    private GatherData setTexture(byte[] cache, ParameterOffset[] parameters, TextureModel texture, boolean noSRGB) {
        if (texture == null) return null;

        this.write(cache, parameters, new Vector4f(1.0f, 1.0f, 0.0f, 0.0f));
        ByteBuffer buffer = texture.getImageModel().getImageData();

        GatherData data = null;
        try { 
            byte[] b = new byte[buffer.remaining()];
            buffer.get(b);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(b));
            if (noSRGB) image = Images.toBump(image);

            byte[] texData = Images.toTEX(image, CompressionType.DXT5, noSRGB, false);
            
            String name = texture.getName();
            if (name == null) 
                name = texture.getImageModel().getName();
            if (name == null) 
                name = SHA1.fromBuffer(texData).toString();

            data = new GatherData(name + ".tex", texData);
        } catch (Exception ex) { return null; }

        return data;
    }

    public byte[] compile(Vector4f transform, ResourceDescriptor diffuse, boolean isDoubleSided, boolean alpha) {
        int flags = CacheFlags.DIFFUSE | CacheFlags.SPECULAR_COLOR;
        if (alpha) flags |= CacheFlags.ALPHA_CLIP;
        CachedShader shader = this.lookup.get(flags);
        Resource resource = new Resource(shader.shader);
        byte[] shaderData = resource.getStream().getBuffer();
        if (shader.has(ParameterType.SPECULAR_COLOR))
            this.write(shaderData, shader.get(ParameterType.SPECULAR_COLOR), new Vector4f(0.09f, 0.09f, 0.09f, 1.0f));
        if (shader.has(ParameterType.TEXTURE0))
            this.write(shaderData, shader.get(ParameterType.TEXTURE0), transform);
        RGfxMaterial gfx = new RGfxMaterial();
        gfx.serialize(new Serializer(shaderData, resource.getRevision(), resource.getCompressionFlags()), gfx);
        gfx.textures[0] = diffuse;
        if (isDoubleSided)
            gfx.flags |= GfxMaterialFlags.TWO_SIDED;
        return Resource.compress(gfx.build(resource.getRevision(), resource.getCompressionFlags()));
    }

    public GatherData[] compile(MaterialModelV2 material) {
        if (material == null || material.getBaseColorTexture() == null) return null;
        int flags = CacheFlags.DIFFUSE | CacheFlags.SPECULAR_COLOR;
        if (material.getNormalTexture() != null)
            flags |= CacheFlags.BUMP;
        CachedShader shader = this.lookup.get(flags);
        if (shader == null) return null;
        Resource resource = new Resource(shader.shader);
        byte[] shaderData = resource.getStream().getBuffer();
        ArrayList<GatherData> data = new ArrayList<>();

        if (shader.has(ParameterType.SPECULAR_COLOR))
            this.write(shaderData, shader.get(ParameterType.SPECULAR_COLOR), new Vector4f(0.09f, 0.09f, 0.09f, 1.0f));
        if (shader.has(ParameterType.TEXTURE0))
            data.add(this.setTexture(shaderData, shader.get(ParameterType.TEXTURE0), material.getBaseColorTexture(), false));
        if (shader.has(ParameterType.TEXTURE1)) {
            data.add(this.setTexture(shaderData, shader.get(ParameterType.TEXTURE1), material.getNormalTexture(), true));
            if (shader.has(ParameterType.BUMP_LEVEL))
                this.write(shaderData, shader.get(ParameterType.BUMP_LEVEL), material.getNormalScale());
        }

        // Greatest hack known to man
        RGfxMaterial gfx = new RGfxMaterial();
        gfx.serialize(new Serializer(shaderData, resource.getRevision(), resource.getCompressionFlags()), gfx);
        for (int i = 0; i < data.size(); ++i) {
            if (data.get(i) == null) continue;
            gfx.textures[i] = new ResourceDescriptor(data.get(i).getSHA1(), ResourceType.TEXTURE);
        }
        
        data.add(new GatherData(
            material.getName() + ".gmat", 
            Resource.compress(gfx.build(resource.getRevision(), resource.getCompressionFlags()))
        ));

        return data.toArray(GatherData[]::new);
    }




    public static void main(String[] args) {
        FileArchive archive = new FileArchive("E:/zeon/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/boot/base.farc");
        FileDB database = new FileDB("E:/zeon/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/boot/db/base.map");
        ResourceSystem.DISABLE_LOGS = true;

        for (FileDBRow row : database) {
            if (!row.getPath().endsWith(".gmat")) continue;
            RGfxMaterial material = archive.loadResource(row.getSHA1(), RGfxMaterial.class);
            if (material == null) continue;

            int flags = 0;
            int output = material.getOutputBox();
            MaterialBox[] boxes = new MaterialBox[8];
            for (MaterialWire wire : material.wires) {
                if (wire.boxTo != output) continue;
                flags |= (1 << (wire.portTo & 0xff));
                boxes[wire.portTo] = material.boxes.get(wire.boxFrom);
            }

            // if (flags == ((1 << BrdfPort.DIFFUSE) | (1 << BrdfPort.SPECULAR)) && material.boxes.size() == 3) {
            //     if (!boxes[BrdfPort.SPECULAR].isColor()) continue;
            //     if (!boxes[BrdfPort.DIFFUSE].isTexture()) continue;
            //     MaterialBox diffuse = boxes[BrdfPort.DIFFUSE];
            //     float scaleX = Float.intBitsToFloat(diffuse.getParameters()[0]);
            //     float scaleY = Float.intBitsToFloat(diffuse.getParameters()[1]);
            //     float offsetX = Float.intBitsToFloat(diffuse.getParameters()[2]);
            //     float offsetY = Float.intBitsToFloat(diffuse.getParameters()[3]);
            //     if ((scaleX == 1.0f && scaleY == 1.0f)) continue;
            //     FileIO.write(material.shaders[1], "C:/Users/Aidan/Desktop/samples/" + row.getName() + ".code");
            //     // FileIO.write(GsonUtils.toJSON(material.boxes).getBytes(), "C:/Users/Aidan/Desktop/samples/" + row.getName() + ".json");
            //     System.out.println(row.getPath());
            // }

            // if (flags == ((1 << BrdfPort.DIFFUSE) | (1 << BrdfPort.SPECULAR) | (1 << BrdfPort.BUMP)) && material.boxes.size() == 4) {
            //     if (!boxes[BrdfPort.SPECULAR].isColor()) continue;
            //     if (!boxes[BrdfPort.DIFFUSE].isTexture()) continue;
            //     if (!boxes[BrdfPort.BUMP].isTexture()) continue;

            //     MaterialBox box = boxes[BrdfPort.DIFFUSE];
            //     float scaleX = Float.intBitsToFloat(box.getParameters()[0]);
            //     float scaleY = Float.intBitsToFloat(box.getParameters()[1]);
            //     if ((scaleX == 1.0f && scaleY == 1.0f)) continue;

            //     box = boxes[BrdfPort.BUMP];
            //     scaleX = Float.intBitsToFloat(box.getParameters()[0]);
            //     scaleY = Float.intBitsToFloat(box.getParameters()[1]);
            //     if ((scaleX == 1.0f && scaleY == 1.0f)) continue;


            //     FileIO.write(material.shaders[0], "C:/Users/Aidan/Desktop/samples/" + row.getName() + ".code");
            //     // FileIO.write(GsonUtils.toJSON(material.boxes).getBytes(), "C:/Users/Aidan/Desktop/samples/" + row.getName() + ".json");
            //     System.out.println(row.getPath());
            // }

            if (flags == ((1 << BrdfPort.DIFFUSE) | (1 << BrdfPort.SPECULAR) | (1 << BrdfPort.BUMP)) && material.boxes.size() == 4) {
                if (!boxes[BrdfPort.SPECULAR].isTexture()) continue;
                if (!boxes[BrdfPort.DIFFUSE].isTexture()) continue;
                if (!boxes[BrdfPort.BUMP].isTexture()) continue;

                MaterialBox box = boxes[BrdfPort.DIFFUSE];
                float scaleX = Float.intBitsToFloat(box.getParameters()[0]);
                float scaleY = Float.intBitsToFloat(box.getParameters()[1]);
                if ((scaleX == 1.0f && scaleY == 1.0f)) continue;

                box = boxes[BrdfPort.BUMP];
                scaleX = Float.intBitsToFloat(box.getParameters()[0]);
                scaleY = Float.intBitsToFloat(box.getParameters()[1]);
                if ((scaleX == 1.0f && scaleY == 1.0f)) continue;

                box = boxes[BrdfPort.SPECULAR];
                scaleX = Float.intBitsToFloat(box.getParameters()[0]);
                scaleY = Float.intBitsToFloat(box.getParameters()[1]);
                if ((scaleX == 1.0f && scaleY == 1.0f)) continue;


                // FileIO.write(material.shaders[0], "C:/Users/Aidan/Desktop/samples/" + row.getName() + ".code");
                // FileIO.write(GsonUtils.toJSON(material.boxes).getBytes(), "C:/Users/Aidan/Desktop/samples/" + row.getName() + ".json");
                System.out.println(row.getPath());
            }
        }
    }
}
