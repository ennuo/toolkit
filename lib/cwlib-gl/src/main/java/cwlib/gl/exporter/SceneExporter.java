package cwlib.gl.exporter;

import cwlib.enums.*;
import cwlib.gl.jobs.DecalBaker;
import cwlib.gl.jobs.MaterialBaker;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.*;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Morph;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.mesh.Submesh;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.CostumePiece;
import cwlib.structs.things.components.Decoration;
import cwlib.structs.things.components.RegionOverride;
import cwlib.structs.things.components.decals.Decal;
import cwlib.structs.things.parts.*;
import cwlib.types.archives.FileArchive;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileEntry;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.impl.v2.*;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import org.joml.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

// This class is stil a work in progress, so it's kind of messy,
// the idea is to re-implement the model exporter to support full scenes,
// as well as baking materials and decals for more accurate rendering.
public class SceneExporter
{
    private final GlTF gltf = new GlTF();
    private final Scene scene;
    private final HashMap<ResourceDescriptor, int[]> modelCache = new HashMap<>();
    private final HashMap<ResourceDescriptor, Integer> nonBakedMaterialCache = new HashMap<>();
    private final HashMap<ResourceDescriptor, Integer> imageCache = new HashMap<>();
    private final ArrayList<BufferDataSegment> dataBufferSegments = new ArrayList<>();
    private int dataBufferSize;

    public SceneExporter()
    {
        setAsset("IKAROS//CWLIB", "2.0");


        // Initialize default scene
        scene = new Scene();
        gltf.setScene(0);
        gltf.addScenes(scene);

        // Create the binary buffer node
        this.gltf.addBuffers(new Buffer());
    }

    // public static void ExportModelToFile(String path, ResourceDescriptor descriptor)
    // {
    //     SceneExporter exporter = new SceneExporter();
    //     FileEntry row = ResourceSystem.get(descriptor);
    //     if (row != null)
    //     {
    //         String name = row.getName().replace(".mol", "");
    //         exporter.registerModel(name, descriptor, null, new MeshConfig());
    //     }

    //     exporter.export(path);
    // }

    public void registerLevel(RLevel level)
    {
        Thing worldThing = level.worldThing;

        // Can't exactly parse the level if there's no
        // existent world.
        if (worldThing == null) return;

        PWorld world = worldThing.getPart(Part.WORLD);

        // This shouldn't even be possible in normal circumstances.
        if (world == null) return;


        for (Thing thing : world.things)
        {
            // Sometimes deleted things or non-serialized bones
            // just have null entries in the world.
            if (thing == null) continue;
            registerThing(thing);
        }
    }

    public void registerPlan(RPlan plan)
    {
        for (Thing thing : plan.getThings())
        {
            if (thing == null) continue;
            registerThing(thing);
        }
    }

    public void registerThing(Thing thing)
    {
        PStickers stickers = thing.getPart(Part.STICKERS);
        PCostume costume = thing.getPart(Part.COSTUME);


        MeshConfig globalEntityConfig = new MeshConfig();
        if (costume != null)
        {
            globalEntityConfig.addHiddenRegions(costume.meshPartsHidden);
            globalEntityConfig.overrides.put(new ResourceDescriptor(9698,
                ResourceType.GFX_MATERIAL), costume.material);
        }

        if (thing.hasPart(Part.MATERIAL_OVERRIDE))
        {
            PMaterialOverride part = thing.getPart(Part.MATERIAL_OVERRIDE);
            globalEntityConfig.addRegionOverrides(part.overrides);
        }

        EntityInfo root = null;
        if (thing.hasPart(Part.RENDER_MESH))
        {
            PRenderMesh renderMesh = thing.getPart(Part.RENDER_MESH);
            FileEntry entry = ResourceSystem.get(renderMesh.mesh);
            globalEntityConfig.decals = stickers != null ? stickers.decals : null;
            if (entry != null)
            {
                root = registerModel(entry.getName().replace(".mol", ""), renderMesh.mesh
                    , null,
                    globalEntityConfig);

                for (int i = 0; i < renderMesh.boneThings.length; ++i)
                {
                    Thing boneThing = renderMesh.boneThings[i];
                    if (boneThing == null || !boneThing.hasPart(Part.POS)) continue;
                    Matrix4f transform = boneThing.<PPos>getPart(Part.POS).localPosition;
                    transform = transform.mul(root.skeleton.bones[i].invSkinPoseMatrix,
                        new Matrix4f());

                    // If we're a locator node, rotate into +Z for Blender
                    // if (root.skeleton.bones[i].parent == -1)
                    //     transform = transform.rotate((float) Math.toRadians(-90.0f),
                    //     new
                    //     Vector3f(1.0f, 0.0f, 0.0f));

                    Node node = root.skeleton.joints[i];
                    node.setTranslation(null);
                    node.setRotation(null);
                    node.setScale(null);
                    node.setMatrix(transform.get(new float[16]));
                }

                // Depending on the type of entity serialized,
                // the bone things might just not exist, in this case,
                // just transform the root node
                if (renderMesh.boneThings.length == 0 && thing.hasPart(Part.POS))
                {
                    Matrix4f transform = thing.<PPos>getPart(Part.POS).localPosition;
                    transform = transform.mul(root.skeleton.bones[0].invSkinPoseMatrix,
                        new Matrix4f());
                    // transform = transform.rotate((float) Math.toRadians(-90.0f), new
                    // Vector3f
                    // (1.0f, 0.0f, 0.0f));

                    Node node = root.skeleton.joints[0];
                    node.setTranslation(null);
                    node.setRotation(null);
                    node.setScale(null);
                    node.setMatrix(transform.get(new float[16]));
                }
            }
        }

        if (root == null) return;

        if (thing.hasPart(Part.DECORATIONS))
        {
            PDecorations decorations = thing.getPart(Part.DECORATIONS);
            for (Decoration decoration : decorations.decorations)
            {
                FileEntry entry = ResourceSystem.get(decoration.renderMesh.mesh);
                if (entry == null) continue;
                String name = entry.getName().replace(".mol", "");
                EntityInfo info = registerModel(name, decoration.renderMesh.mesh, null,
                    new MeshConfig());


                Node parent = root.skeleton.joints[decoration.parentBone];
                for (LocatorInfo locator : info.skeleton.locators)
                {
                    int locatorIndex = gltf.getNodes().indexOf(locator.node);
                    gltf.getScenes().get(0).removeNodes(locatorIndex);
                    parent.addChildren(locatorIndex);

                    Matrix4f offset = locator.transform.mul(decoration.offset,
                        new Matrix4f());
                    locator.node.setTranslation(null);
                    locator.node.setScale(null);
                    locator.node.setRotation(null);
                    locator.node.setMatrix(offset.get(new float[16]));
                }
            }
        }

        if (costume != null)
        {
            for (int i = 0; i < costume.costumePieces.length; ++i)
            {
                CostumePiece piece = costume.costumePieces[i];
                if (piece.mesh == null) continue;
                long guid = piece.mesh.getGUID().getValue();
                if (guid == 9876 || guid == 9877) continue;

                FileEntry entry = ResourceSystem.get(piece.mesh);
                if (entry == null) continue;
                String name = entry.getName().replace(".mol", "");

                globalEntityConfig.decals = stickers != null ? stickers.costumeDecals[i]
                    : null;
                registerModel(name, piece.mesh, root.skeleton, globalEntityConfig);
            }
        }

    }

    static int x = 0;

    public int registerDecalMaterial(RMesh mesh, ResourceDescriptor gfxMaterialDescriptor,
                                     Decal[] decals)
    {
        Vector4f materialTransform = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        RGfxMaterial gmat = ResourceSystem.load(gfxMaterialDescriptor, RGfxMaterial.class);
        int output = gmat.getOutputBox();
        String name = ResourceSystem.get(gfxMaterialDescriptor).getName().replace(".gmat",
            ".decalsbaked_" + (x++));

        Function<MaterialBox, TextureInfo> getTextureInfo = (box) ->
        {
            int[] params = box.getParameters();
            TextureInfo info = new TextureInfo();

            Vector4f transform = box.getTextureTransform().mul(materialTransform);
            float rotation = box.getTextureRotation();

            // do this later
            if (rotation != 0.0f || !transform.equals(new Vector4f(1.0f, 1.0f, 0.0f, 0.0f),
                0.01f))
                info.addExtensions("KHR_texture_transform",
                    new KHRTextureTransform(transform,
                        rotation));

            info.setIndex(registerTexture(gmat, params[5]));
            info.setTexCoord(params[4]);

            return info;
        };

        Function<MaterialBox, float[]> getColorInfo = (box) ->
        {
            int[] params = box.getParameters();
            return new float[]
                {
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2]),
                    Float.intBitsToFloat(params[3])
                };
        };

        BiFunction<Integer, String, TextureInfo> getBakedTextureInfo = (port, extension) ->
        {
            TextureInfo info = new TextureInfo();
            byte[] png = new MaterialBaker(mesh, gmat, gfxMaterialDescriptor,
                materialTransform,
                port, 0).BakeToPNG();
            info.setIndex(registerTexture(name + "_baked." + extension, png));
            return info;
        };

        int attributeIndex = mesh.getAttributeCount() - 1;

        // Initialize a default alpha material
        MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
        pbr.setMetallicFactor(0.0f);
        Material material = new Material();
        material.setName(name);
        material.setPbrMetallicRoughness(pbr);

        material.setDoubleSided((gmat.flags & GfxMaterialFlags.TWO_SIDED) != 0);
        material.setAlphaMode(AlphaMode.MASK);
        material.setAlphaCutoff(gmat.alphaTestLevel);

        {
            TextureInfo info = new TextureInfo();
            System.out.println("Baking texture decals");
            byte[] png = new DecalBaker(decals).BakeToPNG();
            info.setIndex(registerTexture(name + ".diff", png));
            info.setTexCoord(attributeIndex);
            pbr.setBaseColorTexture(info);
            pbr.setBaseColorFactor(new float[] { 2.0f / 3.0f, 2.0f / 3.0f, 2.0f / 3.0f,
                1.0f });
        }

        MaterialBox normal = gmat.getBoxConnectedToPort(output, BrdfPort.BUMP);
        if (normal != null)
        {
            MaterialNormalTextureInfo info = new MaterialNormalTextureInfo();
            info.setScale(gmat.bumpLevel);
            if (normal.isTexture())
            {
                int[] params = normal.getParameters();
                Vector4f transform = normal.getTextureTransform().mul(materialTransform);
                float rotation = normal.getTextureRotation();
                if (rotation != 0.0f || !transform.equals(new Vector4f(1.0f, 1.0f, 0.0f,
                        0.0f),
                    0.01f))
                    info.addExtensions("KHR_texture_transform",
                        new KHRTextureTransform(transform
                            , rotation));

                info.setIndex(registerTexture(gmat, params[5], true));
                info.setTexCoord(params[4]);
            }
            else
            {
                System.out.println("Baking " + name + " normal texture");
                byte[] png = new MaterialBaker(mesh, gmat, gfxMaterialDescriptor,
                    materialTransform, BrdfPort.BUMP, 0).BakeToPNG();
                info.setIndex(registerTexture(name + "_baked.norm", png));
            }

            material.setNormalTexture(info);
        }

        MaterialBox specular = gmat.getBoxConnectedToPort(output, BrdfPort.SPECULAR);
        if (specular != null)
        {
            KHRMaterialsSpecular spec = new KHRMaterialsSpecular();
            if (specular.isColor())
            {
                float[] factor = getColorInfo.apply(specular);
                spec.specularColorFactor = new float[] { factor[0], factor[1], factor[2] };
            }
            else if (specular.isTexture())
                spec.specularColorTexture = getTextureInfo.apply(specular);
            else if (specular.isSimpleMultiply(gmat))
            {
                MaterialBox[] inputs = gmat.getBoxesConnected(specular);

                MaterialBox colorBox, textureBox;
                if (inputs[0].isColor())
                {
                    colorBox = inputs[0];
                    textureBox = inputs[1];
                }
                else
                {
                    colorBox = inputs[1];
                    textureBox = inputs[0];
                }

                float[] factor = getColorInfo.apply(colorBox);
                spec.specularColorFactor = new float[] { factor[0], factor[1], factor[2] };

                spec.specularColorTexture = getTextureInfo.apply(textureBox);
            }
            else
                spec.specularColorTexture = getBakedTextureInfo.apply(BrdfPort.SPECULAR,
                    "spec");

            material.addExtensions("KHR_materials_specular", spec);
        }

        MaterialBox glow = gmat.getBoxConnectedToPort(output, BrdfPort.GLOW);
        if (glow != null)
        {
            material.setEmissiveFactor(new float[] { 1.0f, 1.0f, 1.0f });
            if (glow.isColor())
            {
                int[] params = glow.getParameters();
                material.setEmissiveFactor(new float[] {
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2])
                });
            }
            else if (glow.isTexture())
            {
                int[] params = glow.getParameters();
                TextureInfo info = new TextureInfo();

                Vector4f transform = glow.getTextureTransform();
                float rotation = glow.getTextureRotation();

                // do this later
                if (rotation != 0.0f || !transform.equals(new Vector4f(1.0f, 1.0f, 0.0f,
                        0.0f),
                    0.01f))
                    info.addExtensions("KHR_texture_transform",
                        new KHRTextureTransform(transform
                            , rotation));

                info.setIndex(registerTexture(gmat, params[5]));
                info.setTexCoord(params[4]);
                material.setEmissiveTexture(info);
            }
            else
            {
                TextureInfo info = new TextureInfo();
                System.out.println("Baking " + name + " glow texture");
                byte[] png = new MaterialBaker(mesh, gmat, gfxMaterialDescriptor,
                    materialTransform, BrdfPort.GLOW, 0).BakeToPNG();
                info.setIndex(registerTexture(name + "_baked.glow", png));
                material.setEmissiveTexture(info);
            }

            material.addExtensions("KHR_materials_emissive_strength",
                new KHREmissiveStrength());
        }


        gltf.addMaterials(material);
        int index = gltf.getMaterials().size() - 1;

        return index;
    }

    public int registerMaterial(RMesh mesh, Vector4f materialTransform,
                                ResourceDescriptor descriptor)
    {
        if (nonBakedMaterialCache.containsKey(descriptor))
            return nonBakedMaterialCache.get(descriptor);

        boolean isBakedMaterial = false;
        RGfxMaterial gmat = ResourceSystem.load(descriptor, RGfxMaterial.class);
        if (gmat == null) return 0;
        String name = ResourceSystem.get(descriptor).getName().replace(".gmat", "");

        // Hard-coded alpha layer nonsense from LBP1
        int output = gmat.getOutputBox();
        if (descriptor.isGUID())
        {
            long guid = descriptor.getGUID().getValue();
            if (guid == 0x5407 || guid == 0x2a32 || guid == 0x436f || guid == 0x665f || guid == 0x2a35 || guid == 0x10775 || guid == 0x10c2c)
            {
                gmat.alphaLayer = (byte) 0xc0;
                gmat.alphaMode = 4;
            }
        }

        MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
        pbr.setMetallicFactor(0.0f);
        Material material = new Material();
        material.setName(name);
        material.setPbrMetallicRoughness(pbr);
        material.setDoubleSided((gmat.flags & GfxMaterialFlags.TWO_SIDED) != 0);


        boolean isAlphaClip = ((gmat.flags & GfxMaterialFlags.ALPHA_CLIP) != 0);
        boolean hasAlphaNode = gmat.getBoxConnectedToPort(output, BrdfPort.ALPHA_CLIP) != null;
        boolean isAlphaBlend = gmat.alphaMode != 0;

        if (isAlphaBlend && !isAlphaClip) material.setAlphaMode(AlphaMode.BLEND);
        else if (isAlphaClip || hasAlphaNode)
        {
            material.setAlphaMode(AlphaMode.MASK);
            material.setAlphaCutoff(gmat.alphaTestLevel);
        }


        Function<MaterialBox, TextureInfo> getTextureInfo = (box) ->
        {
            int[] params = box.getParameters();
            TextureInfo info = new TextureInfo();

            Vector4f transform = box.getTextureTransform().mul(materialTransform);
            float rotation = box.getTextureRotation();

            // do this later
            if (rotation != 0.0f || !transform.equals(new Vector4f(1.0f, 1.0f, 0.0f, 0.0f),
                0.01f))
                info.addExtensions("KHR_texture_transform",
                    new KHRTextureTransform(transform,
                        rotation));

            info.setIndex(registerTexture(gmat, params[5]));
            info.setTexCoord(params[4]);

            return info;
        };

        Function<MaterialBox, float[]> getColorInfo = (box) ->
        {
            int[] params = box.getParameters();
            return new float[]
                {
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2]),
                    Float.intBitsToFloat(params[3])
                };
        };

        BiFunction<Integer, String, TextureInfo> getBakedTextureInfo = (port, extension) ->
        {
            TextureInfo info = new TextureInfo();
            System.out.println("Baking " + name + " " + extension + " texture");
            byte[] png =
                new MaterialBaker(mesh, gmat, descriptor, materialTransform, port, 0).BakeToPNG();
            info.setIndex(registerTexture(name + "_baked." + extension, png));
            return info;
        };

        // For exporting materials, we'll check for three things
        // Channel has a single texture -> Safe to just export
        // Channel has a single color -> Safe to just export
        // Channels has multiple textures/colors -> Bake texture map to UV0 and hope for the
        // best

        MaterialBox diffuse = gmat.getBoxConnectedToPort(output, BrdfPort.DIFFUSE);
        if (diffuse.isColor()) pbr.setBaseColorFactor(getColorInfo.apply(diffuse));
        else if (diffuse.isTexture()) pbr.setBaseColorTexture(getTextureInfo.apply(diffuse));
        else if (diffuse.isSimpleMultiply(gmat))
        {
            MaterialBox[] inputs = gmat.getBoxesConnected(diffuse);

            MaterialBox colorBox, textureBox;
            if (inputs[0].isColor())
            {
                colorBox = inputs[0];
                textureBox = inputs[1];
            }
            else
            {
                colorBox = inputs[1];
                textureBox = inputs[0];
            }

            pbr.setBaseColorFactor(getColorInfo.apply(colorBox));
            pbr.setBaseColorTexture(getTextureInfo.apply(textureBox));
        }
        else
        {
            pbr.setBaseColorTexture(getBakedTextureInfo.apply(BrdfPort.DIFFUSE, "diff"));
            isBakedMaterial = true;
        }


        MaterialBox normal = gmat.getBoxConnectedToPort(output, BrdfPort.BUMP);
        if (normal != null)
        {
            MaterialNormalTextureInfo info = new MaterialNormalTextureInfo();
            info.setScale(gmat.bumpLevel);
            if (normal.isTexture())
            {
                int[] params = normal.getParameters();
                Vector4f transform = normal.getTextureTransform().mul(materialTransform);
                float rotation = normal.getTextureRotation();
                if (rotation != 0.0f || !transform.equals(new Vector4f(1.0f, 1.0f, 0.0f,
                        0.0f),
                    0.01f))
                    info.addExtensions("KHR_texture_transform",
                        new KHRTextureTransform(transform
                            , rotation));


                info.setIndex(registerTexture(gmat, params[5], true));
                info.setTexCoord(params[4]);
            }
            else
            {
                isBakedMaterial = true;
                System.out.println("Baking " + name + " normal texture");
                byte[] png = new MaterialBaker(mesh, gmat, descriptor, materialTransform,
                    BrdfPort.BUMP, 0).BakeToPNG();
                info.setIndex(registerTexture(name + "_baked.norm", png));
            }

            material.setNormalTexture(info);
        }

        MaterialBox specular = gmat.getBoxConnectedToPort(output, BrdfPort.SPECULAR);
        if (specular != null)
        {
            KHRMaterialsSpecular spec = new KHRMaterialsSpecular();
            if (specular.isColor())
            {
                float[] factor = getColorInfo.apply(specular);
                spec.specularColorFactor = new float[] { factor[0], factor[1], factor[2] };
            }
            else if (specular.isTexture())
                spec.specularColorTexture = getTextureInfo.apply(specular);
            else if (specular.isSimpleMultiply(gmat))
            {
                MaterialBox[] inputs = gmat.getBoxesConnected(specular);

                MaterialBox colorBox, textureBox;
                if (inputs[0].isColor())
                {
                    colorBox = inputs[0];
                    textureBox = inputs[1];
                }
                else
                {
                    colorBox = inputs[1];
                    textureBox = inputs[0];
                }

                float[] factor = getColorInfo.apply(colorBox);
                spec.specularColorFactor = new float[] { factor[0], factor[1], factor[2] };

                spec.specularColorTexture = getTextureInfo.apply(textureBox);
            }
            else
                spec.specularColorTexture = getBakedTextureInfo.apply(BrdfPort.SPECULAR,
                    "spec");

            material.addExtensions("KHR_materials_specular", spec);
        }

        MaterialBox glow = gmat.getBoxConnectedToPort(output, BrdfPort.GLOW);
        if (glow != null)
        {
            material.setEmissiveFactor(new float[] { 1.0f, 1.0f, 1.0f });
            if (glow.isColor())
            {
                int[] params = glow.getParameters();
                material.setEmissiveFactor(new float[] {
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2])
                });
            }
            else if (glow.isTexture())
            {
                int[] params = glow.getParameters();
                TextureInfo info = new TextureInfo();

                Vector4f transform = glow.getTextureTransform();
                float rotation = glow.getTextureRotation();

                // do this later
                if (rotation != 0.0f || !transform.equals(new Vector4f(1.0f, 1.0f, 0.0f,
                        0.0f),
                    0.01f))
                    info.addExtensions("KHR_texture_transform",
                        new KHRTextureTransform(transform
                            , rotation));

                info.setIndex(registerTexture(gmat, params[5]));
                info.setTexCoord(params[4]);
                material.setEmissiveTexture(info);
            }
            else
            {
                isBakedMaterial = true;
                TextureInfo info = new TextureInfo();
                System.out.println("Baking " + name + " glow texture");
                byte[] png = new MaterialBaker(mesh, gmat, descriptor, materialTransform,
                    BrdfPort.GLOW, 0).BakeToPNG();
                info.setIndex(registerTexture(name + "_baked.glow", png));
                material.setEmissiveTexture(info);
            }

            material.addExtensions("KHR_materials_emissive_strength",
                new KHREmissiveStrength());
        }


        gltf.addMaterials(material);
        int index = gltf.getMaterials().size() - 1;
        if (!isBakedMaterial) nonBakedMaterialCache.put(descriptor, index);

        return index;
    }

    public int registerImage(String name, byte[] buffer)
    {
        Image image = new Image();
        image.setBufferView(getBufferView(buffer, BufferViewTarget.NONE));
        image.setMimeType("image/png");
        image.setName(name);
        gltf.addImages(image);

        return gltf.getImages().size() - 1;
    }

    public int registerImage(ResourceDescriptor descriptor)
    {
        return registerImage(descriptor, false);
    }

    public int registerImage(ResourceDescriptor descriptor, boolean normal)
    {
        if (imageCache.containsKey(descriptor))
            return imageCache.get(descriptor);

        byte[] textureData = ResourceSystem.extract(descriptor);
        if (textureData == null) return -1;
        RTexture texture = new RTexture(textureData);

        BufferedImage image = texture.getImage();
        if (normal)
        {
            for (int x = 0; x < image.getWidth(); ++x)
            {
                for (int y = 0; y < image.getHeight(); ++y)
                {
                    Color c = new Color(image.getRGB(x, y), true);

                    Color output = new Color(255 - c.getAlpha(), c.getGreen(), 255, 255);

                    image.setRGB(x, y, output.getRGB());
                }
            }
        }

        byte[] png;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            ImageIO.write(image, "png", stream);
            png = stream.toByteArray();
        }
        catch (Exception ex) { return -1; }

        String name = ResourceSystem.get(descriptor).getName().replace(".tex", "");
        int index = registerImage(name, png);
        imageCache.put(descriptor, index);
        return index;
    }

    public int registerTexture(String name, byte[] data)
    {
        int index = registerImage(name, data);
        Texture texture = new Texture();
        texture.setSource(index);
        gltf.addTextures(texture);
        return gltf.getTextures().size() - 1;
    }

    public int registerTexture(RGfxMaterial material, int index)
    {
        return registerTexture(material, index, false);
    }

    public int registerTexture(RGfxMaterial material, int index, boolean normal)
    {
        ResourceDescriptor textureDescriptor = material.textures[index];

        Texture texture = new Texture();
        texture.setSource(registerImage(textureDescriptor, normal));

        // Check if the texture needs to use a sampler
        int glWrapS = TextureWrapMode.REPEAT, glWrapT = TextureWrapMode.REPEAT;

        TextureWrap gmatWrapS = material.wrapS[index], gmatWrapT = material.wrapT[index];

        if (gmatWrapS == TextureWrap.CLAMP || gmatWrapS == TextureWrap.CLAMP_TO_EDGE)
            glWrapS = TextureWrapMode.CLAMP_TO_EDGE;
        if (gmatWrapT == TextureWrap.CLAMP || gmatWrapT == TextureWrap.CLAMP_TO_EDGE)
            glWrapT = TextureWrapMode.CLAMP_TO_EDGE;

        // The default sampler state is REPEAT, so we only need to create another one if it's
        // clamp.
        if (glWrapS != TextureWrapMode.REPEAT || glWrapT != TextureWrapMode.REPEAT)
        {
            // See if a sampler already exists
            List<Sampler> samplers = gltf.getSamplers();
            Sampler sampler = null;
            if (samplers != null)
            {
                for (int i = 0; i < samplers.size(); ++i)
                {
                    Sampler candidate = samplers.get(i);
                    if (candidate.getWrapS() == glWrapS && candidate.getWrapT() == glWrapT)
                    {
                        sampler = candidate;
                        break;
                    }
                }
            }

            if (sampler == null)
            {
                sampler = new Sampler();
                sampler.setWrapS(glWrapS);
                sampler.setWrapT(glWrapT);
                gltf.addSamplers(sampler);
            }

            texture.setSampler(gltf.getSamplers().indexOf(sampler));
        }

        // Next check if a texture instance already exists with these settings
        List<Texture> textures = gltf.getTextures();
        if (textures != null)
        {
            for (int i = 0; i < textures.size(); ++i)
            {
                Texture candidate = textures.get(i);
                if (candidate.getSampler() == texture.getSampler() && candidate.getSource() == texture.getSource())
                    return i;
            }
        }

        gltf.addTextures(texture);
        return gltf.getTextures().size() - 1;
    }

    public EntityInfo registerModel(String name, ResourceDescriptor descriptor,
                                    SkeletonInfo skeleton, MeshConfig config)
    {
        RMesh model = ResourceSystem.load(descriptor, RMesh.class);
        if (model == null) return null;

        model.fixupSkinForExport();
        Submesh[] meshes = model.findAllMeshes();
        if (meshes.length == 0)
        {
            System.out.println("WARNING! " + descriptor + " has no meshes!");
            return null;
        }
        else
        {
            for (Submesh mesh : meshes)
            {
                if (mesh.primitives.length == 0)
                {
                    System.out.println("WARNING! " + descriptor + " has missing " +
                                       "primitives in a " +
                                       "submesh!");
                    return null;
                }
            }
        }

        if (skeleton == null) skeleton = registerSkeleton(model.getBones(), meshes);


        int[] meshIndices = modelCache.getOrDefault(descriptor, null);
        if (meshIndices == null)
        {
            meshIndices = new int[meshes.length];

            Vector3f[] normals = model.getNormals();
            int numAttributes = model.getAttributeCount();
            int numMorphs = model.getMorphCount();

            byte[] vertexStream = createStream(model.getVertices());
            byte[] normalStream = createStream(normals);
            byte[] tangentStream = createStream(model.getTangents());
            byte[] weightStream = createStream(model.getWeights());
            byte[] jointStream = createJointStream(model.getJoints(), skeleton.remap);
            byte[][] textureCoordinateStreams = new byte[numAttributes][];
            for (int i = 0; i < numAttributes; ++i)
                textureCoordinateStreams[i] = createStream(model.getUVs(i));

            byte[][] morphStreams = new byte[numMorphs][];
            byte[][] morphNormalStreams = new byte[numMorphs][];
            HashMap<String, Object> extras = null;

            if (numMorphs != 0)
            {
                // Blender supports target names in the extras key
                extras = new HashMap<>();
                extras.put("targetNames", model.getMorphNames());


                Morph[] morphs = model.getMorphs();
                for (int i = 0; i < numMorphs; ++i)
                {
                    Morph morph = morphs[i];
                    morphStreams[i] = createStream(morph.offsets);
                    for (int j = 0; j < model.getNumVerts(); ++j)
                        morph.normals[i].sub(normals[i]);
                    morphNormalStreams[i] = createStream(morph.normals);
                }
            }

            // register all materials
            for (Primitive segment : model.getPrimitives())
            {
                if (config.isRegionHidden(segment.region)) continue;
                registerMaterial(model, config.getMaterialTransform(segment.region),
                    config.getMaterialOverride(segment));
            }

            HashMap<ResourceDescriptor, Integer> decalMaterialLookup = new HashMap<>();


            for (int submeshIndex = 0; submeshIndex < meshes.length; ++submeshIndex)
            {

                Submesh submesh = meshes[submeshIndex];
                ArrayList<Primitive> primitives =
                    new ArrayList<>(Arrays.asList(submesh.primitives));
                Mesh mesh = new Mesh();
                mesh.setExtras(extras);
                for (int i = 0; i < numMorphs; ++i)
                    mesh.addWeights(0.0f);

                if (config.decals != null && config.decals.length != 0)
                {
                    for (Primitive segment : submesh.primitives)
                    {
                        if (config.isRegionHidden(segment.region)) continue;

                        Primitive primitive = new Primitive(segment.minVert,
                            segment.maxVert,
                            segment.firstIndex, segment.numIndices);
                        primitive.material = segment.material;
                        primitive.region = 0xDEADBEEF;
                        if (!decalMaterialLookup.containsKey(segment.material))
                            decalMaterialLookup.put(segment.material,
                                registerDecalMaterial(model
                                    , primitive.material, config.decals));

                        primitives.add(primitive);
                    }
                }


                for (Primitive segment : primitives)
                {
                    if (config.isRegionHidden(segment.region)) continue;

                    MeshPrimitive primitive = new MeshPrimitive();
                    primitive.setMode(PrimitiveType.TRIANGLES);
                    int indexBufferView = getBufferView(createIndexStream(model, segment),
                        BufferViewTarget.ELEMENT_ARRAY_BUFFER);

                    int vertexCount = (segment.maxVert - segment.minVert) + 1;

                    int vertexStart2 = (segment.minVert * 8), vertexStart3 =
                        (segment.minVert * 12), vertexStart4 = (segment.minVert * 16);
                    int vertexSize2 = (vertexCount * 8), vertexSize3 = (vertexCount * 12),
                        vertexSize4 = (vertexCount * 16);


                    int vertexBufferView = getBufferView(vertexStream, vertexStart3,
                        vertexSize3,
                        BufferViewTarget.ARRAY_BUFFER);
                    int normalBufferView = getBufferView(normalStream, vertexStart3,
                        vertexSize3,
                        BufferViewTarget.ARRAY_BUFFER);
                    int tangentBufferView = getBufferView(tangentStream, vertexStart4,
                        vertexSize4, BufferViewTarget.ARRAY_BUFFER);
                    int jointBufferView = getBufferView(jointStream, segment.minVert * 4,
                        vertexCount * 4, BufferViewTarget.ARRAY_BUFFER);
                    int weightBufferView = getBufferView(weightStream, vertexStart4,
                        vertexSize4,
                        BufferViewTarget.ARRAY_BUFFER);

                    primitive.addAttributes("POSITION", createAccessor(vertexBufferView,
                        vertexCount, DimensionType.VEC3, EncodingType.FLOAT, false));
                    primitive.addAttributes("NORMAL", createAccessor(normalBufferView,
                        vertexCount, DimensionType.VEC3, EncodingType.FLOAT, false));
                    primitive.addAttributes("TANGENT", createAccessor(tangentBufferView,
                        vertexCount, DimensionType.VEC4, EncodingType.FLOAT, false));
                    primitive.addAttributes("WEIGHTS_0", createAccessor(weightBufferView,
                        vertexCount, DimensionType.VEC4, EncodingType.FLOAT, false));
                    primitive.addAttributes("JOINTS_0", createAccessor(jointBufferView,
                        vertexCount, DimensionType.VEC4, EncodingType.UNSIGNED_BYTE
                        , false));

                    for (int i = 0; i < numAttributes; ++i)
                    {
                        int texCoordBufferView =
                            getBufferView(textureCoordinateStreams[i],
                                vertexStart2, vertexSize2,
                                BufferViewTarget.ARRAY_BUFFER);
                        primitive.addAttributes("TEXCOORD_" + i,
                            createAccessor(texCoordBufferView, vertexCount,
                                DimensionType.VEC2, EncodingType.FLOAT, false));
                    }
                    primitive.setIndices(createAccessor(indexBufferView,
                        segment.numIndices,
                        DimensionType.SCALAR, EncodingType.UNSIGNED_SHORT, false));
                    for (int i = 0; i < numMorphs; ++i)
                    {
                        HashMap<String, Integer> target = new HashMap<>();
                        int morphPositionBufferView = getBufferView(morphStreams[i],
                            vertexStart3
                            , vertexSize3, BufferViewTarget.ARRAY_BUFFER);
                        int morphNormalBufferView = getBufferView(morphNormalStreams[i],
                            vertexStart3, vertexSize3,
                            BufferViewTarget.ARRAY_BUFFER);

                        target.put("POSITION", createAccessor(morphPositionBufferView,
                            vertexCount, DimensionType.VEC3, EncodingType.FLOAT,
                            false));
                        target.put("NORMAL", createAccessor(morphNormalBufferView,
                            vertexCount,
                            DimensionType.VEC3, EncodingType.FLOAT, false));

                        primitive.addTargets(target);
                    }

                    ResourceDescriptor material = config.getMaterialOverride(segment);
                    if (segment.region == 0xDEADBEEF)
                        primitive.setMaterial(decalMaterialLookup.get(segment.material));
                    else
                        primitive.setMaterial(registerMaterial(model,
                            config.getMaterialTransform(segment.region), material));
                    mesh.addPrimitives(primitive);
                }

                gltf.addMeshes(mesh);
                meshIndices[submeshIndex] = gltf.getMeshes().size() - 1;
            }

            modelCache.put(descriptor, meshIndices);
            System.out.println("adding " + descriptor + " to cache...");
        }

        EntityInfo entityInfo = new EntityInfo();
        entityInfo.skeleton = skeleton;
        entityInfo.locators = new Node[meshes.length];
        for (int i = 0; i < meshIndices.length; ++i)
        {
            int meshIndex = meshIndices[i];
            Submesh submesh = meshes[i];
            if (!submesh.skinned)
            {
                skeleton.locators[i].node.setMesh(meshIndex);
            }
            else
            {
                Node meshNode = createChildNode(name + "_Mesh", skeleton.locators[i].node);
                meshNode.setSkin(skeleton.skins[i].skin);
                meshNode.setMesh(meshIndex);
            }

            entityInfo.locators[i] = skeleton.locators[i].node;
        }

        return entityInfo;
    }

    private SkeletonInfo registerSkeleton(Bone[] bones, Submesh[] meshes)
    {
        LocatorInfo[] locators = new LocatorInfo[meshes.length];
        Node[] nodes = new Node[bones.length];
        SkinInfo[] skins = new SkinInfo[meshes.length];
        int[] remap = new int[bones.length];

        for (int i = 0; i < meshes.length; ++i)
        {
            Submesh mesh = meshes[i];
            SkinInfo info = new SkinInfo();
            registerSkeletonChildrenInternal(info, nodes, bones, remap, mesh.locator);

            LocatorInfo locator = new LocatorInfo();
            locator.transform = bones[mesh.locator].skinPoseMatrix;
            locator.invTransform = bones[mesh.locator].invSkinPoseMatrix;
            locator.index = mesh.locator;
            locator.node = nodes[mesh.locator];
            locator.skinned = mesh.skinned;
            locators[i] = locator;

            if (!mesh.skinned)
            {
                skins[i] = null;
                continue;
            }

            Skin skin = new Skin();
            skin.setJoints(info.joints.stream().map(node -> gltf.getNodes().indexOf(node)).toList());
            gltf.addSkins(skin);
            info.skin = gltf.getSkins().size() - 1;

            // Create the inverse bind matrices
            MemoryOutputStream stream = new MemoryOutputStream(info.joints.size() * 64);
            stream.setLittleEndian(true);
            for (Matrix4f inverse : info.inverses) stream.m44(inverse);
            int view = getBufferView(stream.getBuffer(), BufferViewTarget.NONE);
            skin.setInverseBindMatrices(createAccessor(view, info.joints.size(),
                DimensionType.MAT4, EncodingType.FLOAT, false));

            skins[i] = info;
        }

        SkeletonInfo info = new SkeletonInfo();
        info.locators = locators;
        info.bones = bones;
        info.joints = nodes;
        info.skins = skins;
        info.remap = remap;

        return info;
    }

    private void registerSkeletonChildrenInternal(SkinInfo skin, Node[] nodes, Bone[] bones,
                                                  int[] remap, int index)
    {
        Bone bone = bones[index];

        Node node;
        Matrix4f transform;
        if (bone.parent == -1)
        {
            node = createSceneNode(bone.getName());
            transform = bone.skinPoseMatrix;
        }
        else
        {
            node = createChildNode(bone.getName(), nodes[bone.parent]);
            skin.joints.add(node);
            skin.inverses.add(bone.invSkinPoseMatrix);

            // If we're a direct descendant of the locator, use our global transform instead
            if (bones[bone.parent].parent == -1)
                transform = bone.skinPoseMatrix;
            else
                transform = bone.getLocalTransform(bones);
        }

        nodes[index] = node;

        // LBP doesn't store the local translations for bones,
        // so we have to decompose the local transform matrix.
        Vector3f translation = transform.getTranslation(new Vector3f());
        Quaternionf rotation = new Quaternionf().setFromUnnormalized(transform);
        Vector3f scale = transform.getScale(new Vector3f());
        node.setTranslation(new float[] { translation.x, translation.y, translation.z });
        node.setRotation(new float[] { rotation.x, rotation.y, rotation.z, rotation.w });
        node.setScale(new float[] { scale.x, scale.y, scale.z });

        for (int i = 0; i < bones.length; ++i)
        {
            if (bones[i].parent == index)
                registerSkeletonChildrenInternal(skin, nodes, bones, remap, i);
        }
    }

    private int getBufferView(byte[] buffer, int target)
    {
        return getBufferView(buffer, 0, buffer.length, target);
    }

    private int getBufferView(byte[] buffer, int offset, int size, int target)
    {
        BufferDataSegment segment = null;
        for (int i = 0; i < dataBufferSegments.size(); ++i)
        {
            BufferDataSegment element = dataBufferSegments.get(i);

            // Check if data contents is the same, so we're not wasting space
            if (Arrays.equals(element.byteBuffer, buffer))
            {
                segment = element;
                break;
            }
        }

        // If the buffer hasn't already been registered, create a new segment
        if (segment == null)
        {
            // Just going to align all buffers to a matrix boundary.
            if ((dataBufferSize % 64) != 0)
                dataBufferSize += (64 - (dataBufferSize % 64));

            segment = new BufferDataSegment(buffer, dataBufferSize);
            dataBufferSize += buffer.length;
            dataBufferSegments.add(segment);
        }

        offset += segment.byteOffset;


        // Check if a buffer view with the same offset and size already exists.
        List<BufferView> views = gltf.getBufferViews();
        if (views != null)
        {
            for (int i = 0; i < views.size(); ++i)
            {
                BufferView view = views.get(i);
                if (view.getByteOffset() == offset && view.getByteLength() == size)
                {
                    return i;
                }
            }
        }

        // Really missing C#'s initializer lists right now
        BufferView view = new BufferView();
        view.setBuffer(0);
        view.setByteOffset(offset);
        view.setByteLength(size);
        if (target != 0) view.setTarget(target);

        gltf.addBufferViews(view);
        return gltf.getBufferViews().size() - 1;
    }

    private int createAccessor(int view, int count, String dimensions, int encoding,
                               boolean normalized)
    {
        // Check if an accessor that matches the conditions already exists
        List<Accessor> accessors = gltf.getAccessors();
        if (accessors != null)
        {
            for (int i = 0; i < accessors.size(); ++i)
            {
                Accessor accessor = accessors.get(i);
                if (accessor.getBufferView() == view &&
                    accessor.getCount() == count &&
                    accessor.getType().equals(dimensions) &&
                    accessor.getComponentType() == encoding &&
                    accessor.isNormalized() == normalized)
                    return i;
            }
        }

        Accessor accessor = new Accessor();
        accessor.setBufferView(view);
        accessor.setComponentType(encoding);
        accessor.setType(dimensions);
        accessor.setCount(count);
        accessor.setNormalized(normalized);

        gltf.addAccessors(accessor);
        int index = gltf.getAccessors().size() - 1;
        return index;
    }

    private byte[] createIndexStream(RMesh mesh, Primitive primitive)
    {
        int[] triangles = mesh.getTriangles(primitive);

        // Convert the primitive to reference the triangle data,
        // this would normally be bad, but the mesh in this context,
        // is always a temporary instance, so it'll be discarded after this.
        primitive.firstIndex = 0;
        primitive.numIndices = triangles.length;

        // There's something weird about primitive values on PS4, that I still
        // haven't bothered to check, so recompute the min and max vertex.
        int min = triangles[0];
        int max = triangles[0];
        for (int i = 1; i < triangles.length; ++i)
        {
            int v = triangles[i];
            if (v > max) max = v;
            if (v < min) min = v;
        }

        primitive.minVert = min;
        primitive.maxVert = max;

        // Now create the actual index stream
        MemoryOutputStream stream = new MemoryOutputStream(triangles.length * 0x2);
        stream.setLittleEndian(true);
        for (int triangle : triangles)
            stream.u16(triangle - min); // Indices need to start at 0
        return stream.getBuffer();
    }

    private byte[] createJointStream(byte[][] values, int[] remap)
    {
        byte[] stream = new byte[values.length * 4];
        for (int i = 0; i < values.length; ++i)
        {
            byte[] value = values[i];
            int offset = (i * 4);
            stream[offset] = (byte) remap[value[0]];
            stream[offset + 1] = (byte) remap[value[1]];
            stream[offset + 2] = (byte) remap[value[2]];
            stream[offset + 3] = (byte) remap[value[3]];
        }
        return stream;
    }

    private byte[] createStream(Vector2f[] values)
    {
        MemoryOutputStream stream = new MemoryOutputStream(values.length * 8);
        stream.setLittleEndian(true);
        for (Vector2f value : values) stream.v2(value);
        return stream.getBuffer();
    }

    private byte[] createStream(Vector3f[] values)
    {
        MemoryOutputStream stream = new MemoryOutputStream(values.length * 12);
        stream.setLittleEndian(true);
        for (Vector3f value : values) stream.v3(value);
        return stream.getBuffer();
    }

    private byte[] createStream(Vector4f[] values)
    {
        MemoryOutputStream stream = new MemoryOutputStream(values.length * 16);
        stream.setLittleEndian(true);
        for (Vector4f value : values) stream.v4(value);
        return stream.getBuffer();
    }

    private Node createChildNode(String name, Node parent)
    {
        Node node = new Node();
        node.setName(name);
        int index = gltf.getNodes().size();
        parent.addChildren(index);
        gltf.addNodes(node);
        return node;
    }

    private Node createSceneNode(String name)
    {
        Node node = new Node();
        node.setName(name);

        gltf.addNodes(node);
        int index = gltf.getNodes().size() - 1;
        scene.addNodes(index);

        return node;
    }

    private void setAsset(String generator, String version)
    {
        Asset asset = new Asset();
        asset.setGenerator(generator);
        asset.setVersion(version);
        gltf.setAsset(asset);
    }

    public void export(String path)
    {
        HashSet<String> usedExtensions = new HashSet<>();
        List<Material> materials = gltf.getMaterials();
        if (materials != null)
        {
            for (Material material : gltf.getMaterials())
            {
                Map<String, Object> extensions = material.getExtensions();
                if (extensions != null) usedExtensions.addAll(extensions.keySet());
                usedExtensions.add("KHR_materials_specular");
                usedExtensions.add("KHR_texture_transform");
                usedExtensions.add("KHR_materials_emissive_strength");
            }
        }

        gltf.setExtensionsUsed(usedExtensions.stream().toList());

        gltf.getBuffers().get(0).setByteLength(dataBufferSize);
        byte[] dataBuffer = new byte[dataBufferSize];
        for (BufferDataSegment segment : dataBufferSegments)
            System.arraycopy(segment.byteBuffer, 0, dataBuffer, segment.byteOffset,
                segment.byteSize);

        GltfAssetV2 asset = new GltfAssetV2(gltf, ByteBuffer.wrap(dataBuffer));
        GltfAssetWriterV2 writer = new GltfAssetWriterV2();
        try
        {
            FileOutputStream stream = new FileOutputStream(path);
            writer.writeBinary(asset, stream);
            stream.close();
        }
        catch (Exception ex)
        {
            Logger.getLogger(SceneExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class MeshConfig
    {
        public Decal[] decals;
        public HashSet<Integer> hidden = new HashSet<>();
        public HashMap<ResourceDescriptor, ResourceDescriptor> overrides = new HashMap<>();
        public HashMap<Integer, RegionOverride> regionOverrides = new HashMap<>();

        public void addHiddenRegions(ArrayList<Integer> hidden)
        {
            for (int region : hidden)
                this.hidden.add(region);
        }

        public void addRegionOverrides(RegionOverride[] overrides)
        {
            for (RegionOverride override : overrides)
                regionOverrides.put(override.region, override);
        }

        public Vector4f getMaterialTransform(int region)
        {
            RegionOverride override = regionOverrides.getOrDefault(region, null);
            if (override != null)
                return new Vector4f(override.uvScale.x, override.uvScale.y, 1.0f, 1.0f);
            return new Vector4f(1.0f);
        }

        public ResourceDescriptor getMaterialOverride(Primitive primitive)
        {
            RegionOverride override = regionOverrides.getOrDefault(primitive.region, null);
            if (override != null)
                return override.material;
            if (overrides.containsKey(primitive.material))
                return overrides.get(primitive.material);
            return primitive.material;
        }

        public boolean hasHiddenRegions()
        {
            return hidden.size() != 0;
        }

        public boolean isRegionHidden(int region)
        {
            return hidden.contains(region);
        }
    }

    static class EntityInfo
    {
        public Node[] locators;
        public SkeletonInfo skeleton;
    }


    static class SkinInfo
    {
        public int skin;
        public List<Node> joints = new ArrayList<>();
        public List<Matrix4f> inverses = new ArrayList<>();
    }

    static class LocatorInfo
    {
        public int index;
        public Matrix4f transform;
        public Matrix4f invTransform;
        public Node node;
        public boolean skinned;
    }

    static class SkeletonInfo
    {
        public LocatorInfo[] locators;
        public Bone[] bones;
        public Node[] joints;
        public SkinInfo[] skins;
        public int[] remap;
    }

    static class BufferDataSegment
    {
        public final byte[] byteBuffer;
        public final int byteOffset;
        public final int byteSize;

        public BufferDataSegment(byte[] buffer, int offset)
        {
            byteBuffer = buffer;
            byteOffset = offset;
            byteSize = buffer.length;
        }
    }

    static class EncodingType
    {
        static final int BYTE = 5120;
        static final int UNSIGNED_BYTE = 5121;
        static final int SHORT = 5122;
        static final int UNSIGNED_SHORT = 5123;
        static final int UNSIGNED_INT = 5125;
        static final int FLOAT = 5126;
    }

    static class DimensionType
    {
        static final String SCALAR = "SCALAR";
        static final String VEC2 = "VEC2";
        static final String VEC3 = "VEC3";
        static final String VEC4 = "VEC4";
        static final String MAT4 = "MAT4";
    }

    static class AlphaMode
    {
        static final String OPAQUE = "OPAQUE";
        static final String MASK = "MASK";
        static final String BLEND = "BLEND";
    }

    static class BufferViewTarget
    {
        static final int NONE = 0;
        static final int ARRAY_BUFFER = 34962;
        static final int ELEMENT_ARRAY_BUFFER = 34963;
    }

    static class PrimitiveType
    {
        static final int POINTS = 0;
        static final int LINES = 1;
        static final int LINE_LOOP = 2;
        static final int LINE_STRIP = 3;
        static final int TRIANGLES = 4;
        static final int TRIANGLE_STRIP = 5;
        static final int TRIANGLE_FAN = 6;
    }

    static class TextureWrapMode
    {
        static final int CLAMP_TO_EDGE = 33071;
        static final int MIRRORED_REPEAT = 33648;
        static final int REPEAT = 10497;
    }

    static class KHRTextureTransform
    {
        public float[] offset;
        public float rotation;
        public float[] scale;

        public KHRTextureTransform(Vector4f transform, float rotation)
        {
            this.rotation = rotation;
            offset = new float[] { transform.z, transform.w };
            scale = new float[] { transform.x, transform.y };
        }
    }

    static class KHREmissiveStrength
    {
        public float emissiveStrength = 1.0f;
    }

    static class KHRMaterialsSpecular
    {
        public float[] specularColorFactor;
        public TextureInfo specularColorTexture;
    }
}
