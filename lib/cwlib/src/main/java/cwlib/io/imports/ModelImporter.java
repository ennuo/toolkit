package cwlib.io.imports;

import cwlib.enums.CostumePieceCategory;
import cwlib.enums.HairMorph;
import cwlib.enums.ResourceType;
import cwlib.enums.SkeletonType;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RMesh;
import cwlib.resources.custom.RBoneSet;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.custom.Skeleton;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Primitive;
import cwlib.types.SerializedResource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.*;

public class ModelImporter
{
    private static final RBoneSet SKELETONS =
        new SerializedResource(FileIO.getResourceFile("/binary/characters.boneset")).loadResource(RBoneSet.class);

    public static class ModelImportConfig
    {
        public String glbSourcePath;
        public byte[] glbSourceData;

        public float vertexScale = 1.0f;
        public Vector3f vertexOffset = new Vector3f(0.0f, 0.0f, 0.0f);

        public HashMap<String, String> boneRemap = new HashMap<>();
        public HashMap<String, ResourceDescriptor> materialOverrides = new HashMap<>();

        public EnumSet<CostumePieceCategory> categories =
            EnumSet.noneOf(CostumePieceCategory.class);
        public HashSet<Integer> regionsIDsToHide = new HashSet<>();
        public HairMorph hairMorph = HairMorph.HAT;
        public SkeletonType skeleton = SkeletonType.SACKBOY;
    }

    private final ModelImportConfig config;
    private final GltfModel gltf;

    private final ArrayList<String> morphs = new ArrayList<>();
    private Bone[] bones;
    private final ArrayList<Primitive> primitives = new ArrayList<>();

    private final HashMap<String, Integer> targetLookup = new HashMap<>();
    private final HashMap<String, Integer> jointLookup = new HashMap<>();

    private int attributeCount, morphCount;
    private int vertexOffset, indexOffset;
    private final MemoryOutputStream[] vertexStreams = new MemoryOutputStream[35];
    private MemoryOutputStream attributeStream = null;
    private MemoryOutputStream indexStream = null;

    private final Vector2f minUV = new Vector2f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    private final Vector2f maxUV = new Vector2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

    private final HashMap<Bone, Vector3f> minVert = new HashMap<>();
    private final HashMap<Bone, Vector3f> maxVert = new HashMap<>();

    private final HashMap<MaterialModel, Primitive> gltfMaterials = new HashMap<>();

    public ModelImporter(ModelImportConfig config)
    throws IOException
    {
        this.config = config;
        if (config.glbSourceData != null)
        {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(config.glbSourceData))
            {
                this.gltf = new GltfModelReader().readWithoutReferences(stream);
            }
        }
        else
            this.gltf = new GltfModelReader().read(Path.of(config.glbSourcePath).toUri());
    }

    private int getTargetIndex(String name)
    {
        if (this.targetLookup.containsKey(name))
            return this.targetLookup.get(name);
        int index = this.morphs.size();
        this.morphs.add(name);
        this.targetLookup.put(name, index);
        return index;
    }

    private int getJointIndex(NodeModel node)
    {
        String name = node.getName();
        name = this.config.boneRemap.getOrDefault(name, name);
        if (this.jointLookup.containsKey(name))
            return this.jointLookup.get(name);
        return 0;
    }

    private int getVertexCount(MeshModel mesh)
    {
        return mesh.getMeshPrimitiveModels()
            .stream()
            .mapToInt(model -> model.getAttributes().get("POSITION").getCount())
            .reduce(0, (total, current) -> total + current);
    }

    private int getIndexCount(MeshModel mesh)
    {
        return mesh.getMeshPrimitiveModels()
            .stream()
            .mapToInt(model -> model.getIndices().getCount())
            .reduce(0, (total, current) -> total + current);
    }

    private int getTotalVertexCount()
    {
        int count = 0;
        for (NodeModel node : gltf.getNodeModels())
        {
            if (node.getMeshModels().size() != 0)
            {
                MeshModel mesh = node.getMeshModels().get(0);
                count += this.getVertexCount(mesh);
            }
        }
        return count;
    }

    private int getTotalIndexCount()
    {
        int count = 0;
        for (NodeModel node : gltf.getNodeModels())
        {
            if (node.getMeshModels().size() != 0)
            {
                MeshModel mesh = node.getMeshModels().get(0);
                count += this.getIndexCount(mesh);
            }
        }
        return count;
    }

    private int getMaxMorphCount()
    {
        int count = 0;
        for (NodeModel node : gltf.getNodeModels())
        {
            if (node.getMeshModels().size() != 0)
            {
                MeshModel mesh = node.getMeshModels().get(0);
                for (MeshPrimitiveModel primitive : mesh.getMeshPrimitiveModels())
                {
                    int localCount = primitive.getTargets().size();
                    if (localCount > count)
                        count = localCount;
                }
            }
        }
        return count;
    }

    private int getMaxAttributeCount()
    {
        int count = 0;
        for (NodeModel node : gltf.getNodeModels())
        {
            if (node.getMeshModels().size() != 0)
            {
                MeshModel mesh = node.getMeshModels().get(0);
                for (MeshPrimitiveModel primitive : mesh.getMeshPrimitiveModels())
                {
                    for (int i = 0; i < 3; ++i)
                    {
                        if (!primitive.getAttributes().containsKey("TEXCOORD_" + i))
                            break;

                        if (i + 1 > count)
                            count = i + 1;
                    }
                }
            }
        }
        return count;
    }

    private boolean hasTangents()
    {
        for (NodeModel node : gltf.getNodeModels())
        {
            if (node.getMeshModels().size() != 0)
            {
                MeshModel mesh = node.getMeshModels().get(0);
                for (MeshPrimitiveModel primitive : mesh.getMeshPrimitiveModels())
                {
                    if (!primitive.getAttributes().containsKey("TANGENT"))
                        return false;
                }
            }
        }
        return true;
    }

    private ByteBuffer getAttributeBuffer(MeshPrimitiveModel primitive, String attribute)
    {
        AccessorModel accessor = primitive.getAttributes().get(attribute);
        if (accessor == null) return null;
        return accessor.getAccessorData().createByteBuffer();
    }

    private void addMesh(MeshModel mesh, SkinModel skin, ArrayList<String> targetNames)
    {
        for (MeshPrimitiveModel meshPrimitive : mesh.getMeshPrimitiveModels())
        {
            int numVertices = meshPrimitive.getAttributes().get("POSITION").getCount();

            int minVert = this.vertexOffset;
            int maxVert = minVert + numVertices;
            int firstIndex = this.indexOffset;
            int numIndices = meshPrimitive.getIndices().getCount();

            this.indexOffset += numIndices;
            this.vertexOffset += numVertices;

            ByteBuffer vertices = this.getAttributeBuffer(meshPrimitive, "POSITION");
            ByteBuffer color = this.getAttributeBuffer(meshPrimitive, "COLOR_0");
            MemoryOutputStream vertexStream =
                this.vertexStreams[RMesh.STREAM_POS_BONEINDICES];
            Vector3f[] vertexCache = new Vector3f[numVertices];
            for (int i = 0; i < numVertices; ++i)
            {
                Vector3f vertex = new Vector3f(
                    vertices.getFloat(),
                    vertices.getFloat(),
                    vertices.getFloat()
                );

                vertex.mul(this.config.vertexScale);
                vertex.add(this.config.vertexOffset);

                vertexCache[i] = vertex;

                vertexStream.v3(vertex);

                int c = 0xff;
                if (color != null)
                {
                    c = Math.round(((float) (color.getShort() & 0xffff) / (float) 0xffff) * 0xFF);
                    color.getShort();
                    color.getShort();
                    color.getShort();
                }
                vertexStream.i32(c, true);
            }

            MemoryOutputStream skinningStream =
                this.vertexStreams[RMesh.STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM];

            ByteBuffer weights = this.getAttributeBuffer(meshPrimitive, "WEIGHTS_0");
            ByteBuffer joints = this.getAttributeBuffer(meshPrimitive, "JOINTS_0");
            ByteBuffer normals = this.getAttributeBuffer(meshPrimitive, "NORMAL");
            ByteBuffer tangents = this.getAttributeBuffer(meshPrimitive, "TANGENT");

            boolean isShortBuffer = false;
            AccessorModel jointAccessorModel = meshPrimitive.getAttributes().get("JOINTS_0");
            isShortBuffer =
                jointAccessorModel != null &&
                jointAccessorModel.getComponentDataType().toString().equals("short");

            ByteBuffer[] attributes = new ByteBuffer[] {
                this.getAttributeBuffer(meshPrimitive, "TEXCOORD_0"),
                this.getAttributeBuffer(meshPrimitive, "TEXCOORD_1"),
                this.getAttributeBuffer(meshPrimitive, "TEXCOORD_2")
            };

            List<Map<String, AccessorModel>> targets = meshPrimitive.getTargets();
            ByteBuffer[] targetPositions = new ByteBuffer[this.morphCount];
            ByteBuffer[] targetNormals = new ByteBuffer[this.morphCount];
            for (int i = 0; i < targets.size(); ++i)
            {
                Map<String, AccessorModel> target = targets.get(i);
                if (target.containsKey("POSITION"))
                    targetPositions[i] =
                        target.get("POSITION").getAccessorData().createByteBuffer();
                if (target.containsKey("NORMAL"))
                    targetNormals[i] =
                        target.get("NORMAL").getAccessorData().createByteBuffer();
            }

            int[] jointCache = new int[4];
            float[] weightCache = new float[4];
            for (int i = 0; i < numVertices; ++i)
            {
                weightCache[0] = 1.0f;
                weightCache[1] = 0.0f;
                weightCache[2] = 0.0f;
                weightCache[3] = 0.0f;

                if (weights != null)
                {
                    for (int j = 0; j < 4; ++j)
                        weightCache[j] = weights.getFloat();
                }


                if (skin == null)
                {
                    for (int j = 0; j < 4; ++j)
                        jointCache[j] = 0;
                }
                else
                {
                    for (int j = 0; j < 4; ++j)
                    {
                        int joint =
                            (isShortBuffer ? joints.getShort() : joints.get()) & 0xff;
                        jointCache[j] = this.getJointIndex(skin.getJoints().get(joint));
                    }
                }

                // Update min/max for bones
                for (int j = 0; j < 4; ++j)
                {
                    if (weightCache[j] == 0.0f) continue;
                    Vector3f max = this.maxVert.get(this.bones[jointCache[j]]);
                    Vector3f min = this.minVert.get(this.bones[jointCache[j]]);
                    Vector3f v = vertexCache[i];

                    if (v.x > max.x) max.x = v.x;
                    if (v.y > max.y) max.y = v.y;
                    if (v.z > max.z) max.z = v.z;

                    if (v.x < min.x) min.x = v.x;
                    if (v.y < min.y) min.y = v.y;
                    if (v.z < min.z) min.z = v.z;
                }

                int scale = (weightCache[1] != 0.0f) ? 0xFE : 0xFF;
                skinningStream.u8(Math.round(weightCache[2] * scale));
                skinningStream.u8(Math.round(weightCache[1] * scale));
                skinningStream.u8(Math.round(weightCache[0] * scale));

                skinningStream.u8(jointCache[0]);

                Vector3f normal = new Vector3f(
                    normals.getFloat(),
                    normals.getFloat(),
                    normals.getFloat()
                );

                skinningStream.u24(Bytes.packNormal24(normal));

                skinningStream.u8(jointCache[1]);

                if (tangents != null)
                {
                    skinningStream.u24(Bytes.packNormal24(new Vector3f(
                        tangents.getFloat(),
                        tangents.getFloat(),
                        tangents.getFloat()
                    )));
                    tangents.getFloat();
                }
                else skinningStream.u24(0);

                skinningStream.u8(jointCache[2]);

                skinningStream.u24(0); // Don't know what a smooth normal is

                skinningStream.u8(jointCache[3]);

                // Attributes

                for (int j = 0; j < this.attributeCount; ++j)
                {
                    ByteBuffer buffer = attributes[j];
                    if (buffer == null)
                        this.attributeStream.pad(0x8);
                    Vector2f uv = new Vector2f(buffer.getFloat(), buffer.getFloat());

                    if (uv.x > this.maxUV.x) this.maxUV.x = uv.x;
                    if (uv.y > this.maxUV.y) this.maxUV.y = uv.y;

                    if (uv.x < this.minUV.x) this.minUV.x = uv.x;
                    if (uv.y < this.minUV.y) this.minUV.y = uv.y;

                    this.attributeStream.v2(uv);
                }

                // Targets
                // This one might not necessarily be in order
                // so we'll have to seek
                for (int j = 0; j < this.morphCount; ++j)
                {
                    ByteBuffer targetPosition = targetPositions[j];
                    if (targetPosition == null) continue;
                    ByteBuffer targetNormal = targetNormals[j];
                    MemoryOutputStream targetStream =
                        this.vertexStreams[RMesh.STREAM_MORPHS0 + this.getTargetIndex(targetNames.get(j))];
                    targetStream.seek((i + minVert) * 0x10, SeekMode.Begin);
                    Vector3f vertex = new Vector3f(
                        targetPosition.getFloat(),
                        targetPosition.getFloat(),
                        targetPosition.getFloat()
                    );

                    vertex.mul(this.config.vertexScale);
                    vertex.add(this.config.vertexOffset);

                    targetStream.v3(vertex);

                    if (targetNormal != null)
                    {
                        normal.add(targetNormal.getFloat(), targetNormal.getFloat(),
                            targetNormal.getFloat());
                        targetStream.i32(Bytes.packNormal32(normal));
                    }
                }
            }

            ShortBuffer indexAccessor =
                meshPrimitive.getIndices().getAccessorData().createByteBuffer().asShortBuffer();
            int[] triCache = new int[numIndices];
            for (int i = 0; i < numIndices; ++i)
            {
                int index = (indexAccessor.get() & 0xffff) + minVert;
                triCache[i] = index;
                this.indexStream.u16(index);
            }

            String materialName = meshPrimitive.getMaterialModel().getName();
            System.out.println(materialName);
            ResourceDescriptor descriptor = null;
            if (this.config.materialOverrides != null && this.config.materialOverrides.containsKey(materialName))
                descriptor = this.config.materialOverrides.get(materialName);
            else
                descriptor = new ResourceDescriptor(10803, ResourceType.GFX_MATERIAL);

            Primitive primitive = new Primitive(descriptor, minVert, maxVert, firstIndex,
                numIndices);
            this.primitives.add(primitive);
            this.gltfMaterials.put(meshPrimitive.getMaterialModel(), primitive);

        }
    }

    private void convertToGlobalSkinPose(Bone bone, Matrix4f parent)
    {
        bone.skinPoseMatrix = parent.mul(bone.skinPoseMatrix, new Matrix4f());
        for (Bone child : bone.getChildren(this.bones))
            this.convertToGlobalSkinPose(child, bone.skinPoseMatrix);
    }

    private void getCustomSkeleton()
    {
        if (this.gltf.getSkinModels().size() == 0)
        {
            this.bones = new Bone[] { new Bone("BocchiTheRock!") };
            return;
        }

        ArrayList<Bone> bones = new ArrayList<>();
        HashMap<NodeModel, Bone> lookup = new HashMap<>();
        for (SkinModel skin : this.gltf.getSkinModels())
        {
            int index = 0;
            for (NodeModel node : skin.getJoints())
            {
                Bone bone = new Bone(node.getName());

                Vector3f translation = new Vector3f();
                Quaternionf quaternion = new Quaternionf();
                Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);

                if (node.getTranslation() != null)
                    translation.set(node.getTranslation());
                float[] rotation = node.getRotation();
                if (rotation != null)
                    quaternion.set(rotation[0], rotation[1], rotation[2], rotation[3]);
                if (node.getScale() != null)
                    scale.set(node.getScale());

                bone.skinPoseMatrix = new Matrix4f().identity().translationRotateScale(
                    translation,
                    quaternion,
                    scale
                );

                bone.invSkinPoseMatrix = new Matrix4f().set(skin.getInverseBindMatrix(index,
                    new float[16]));

                bones.add(bone);
                lookup.put(node, bone);

                index++;
            }

            // Second pass to set hierarchy
            for (NodeModel model : skin.getJoints())
            {
                List<NodeModel> children = model.getChildren();
                if (children == null || children.size() == 0) continue;

                {
                    ArrayList<NodeModel> joints = new ArrayList<>();
                    for (NodeModel child : children)
                        if (skin.getJoints().contains(child))
                            joints.add(child);
                    children = joints;
                    if (children.size() == 0) continue;
                }

                Bone bone = lookup.get(model);
                index = bones.indexOf(bone);
                bone.firstChild = bones.indexOf(lookup.get(children.get(0)));
                for (int i = 0; i < children.size(); ++i)
                    lookup.get(children.get(i)).parent = index;
                for (int i = 1; i < children.size(); ++i)
                    lookup.get(children.get(i - 1)).nextSibling =
                        bones.indexOf(lookup.get(children.get(i)));
            }
        }

        this.bones = bones.toArray(Bone[]::new);

        // Convert all local skin pose matrices to global
        for (Bone bone : bones)
        {
            if (bone.parent == -1)
                this.convertToGlobalSkinPose(bone, new Matrix4f().identity());
        }
    }

    @SuppressWarnings("unchecked")
    public RMesh getMesh()
    {
        if (!hasTangents())
            ResourceSystem.println("ModelImporter", "Model is missing tangents, this will " +
                                                    "cause " +
                                                    "issues with normals! Please re-export " +
                                                    "model with tangents.");


        Skeleton skeleton = null;
        if (this.config.skeleton != null)
        {
            skeleton = SKELETONS.getSkeletons().get(this.config.skeleton.getValue());
            this.bones = skeleton.bones;
        }
        else this.getCustomSkeleton();

        for (Bone bone : this.bones)
        {
            this.minVert.put(bone, new Vector3f(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY));
            this.maxVert.put(bone, new Vector3f(Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY));
        }

        for (int i = 0; i < this.bones.length; ++i)
            this.jointLookup.put(this.bones[i].getName(), i);

        int totalVertCount = this.getTotalVertexCount();
        if (totalVertCount >= 0xFFFF)
        {
            ResourceSystem.println("ModelImporter", "Max vertex count is 65,535, can't " +
                                                    "import " +
                                                    "model!");
            return null;
        }

        this.attributeCount = this.getMaxAttributeCount();
        System.out.println("UV Count: " + this.attributeCount);
        this.morphCount = this.getMaxMorphCount();

        if (this.morphCount >= RMesh.MAX_MORPHS)
        {
            ResourceSystem.println("ModelImporter", "Max morph count is 32, can't import " +
                                                    "model!");
            return null;
        }

        this.vertexStreams[RMesh.STREAM_POS_BONEINDICES] =
            new MemoryOutputStream(totalVertCount * 0x10);
        this.vertexStreams[RMesh.STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM] =
            new MemoryOutputStream(totalVertCount * 0x10);
        for (int i = 0; i < this.morphCount; ++i)
            this.vertexStreams[RMesh.STREAM_MORPHS0 + i] =
                new MemoryOutputStream(totalVertCount * 0x10);
        this.attributeStream =
            new MemoryOutputStream(totalVertCount * (this.attributeCount * 0x8));
        this.indexStream = new MemoryOutputStream(this.getTotalIndexCount() * 0x2);

        for (NodeModel node : gltf.getNodeModels())
        {
            if (node.getMeshModels().size() != 0)
            {
                MeshModel mesh = node.getMeshModels().get(0);

                Map<String, Object> extras = (Map<String, Object>) mesh.getExtras();
                ArrayList<String> targetNames = null;
                if (extras != null)
                    targetNames = (ArrayList<String>) extras.get("targetNames");
                if (targetNames == null)
                {
                    targetNames = new ArrayList<>(this.morphCount);
                    for (int i = 0; i < this.morphCount; ++i)
                        targetNames.add("Morph_" + i);
                }

                this.addMesh(mesh, node.getSkinModel(), targetNames);
            }
        }

        byte[][] streams = new byte[2 + this.morphCount][];
        for (int i = 0; i < streams.length; ++i)
            streams[i] = this.vertexStreams[i].getBuffer();

        RMesh mesh = new RMesh(
            streams,
            this.attributeStream.getBuffer(),
            this.indexStream.getBuffer(),
            this.bones
        );

        if (skeleton != null)
            mesh.applySkeleton(skeleton);

        mesh.calculateBoundBoxes(this.config.skeleton == null);

        // Set morph names
        for (int i = 0; i < this.morphs.size(); ++i)
        {
            String morph = this.morphs.get(i);
            if (morph.length() > 15)
                morph = morph.substring(0, 15);
            mesh.setMorphName(morph, i);
        }

        mesh.setPrimitives(this.primitives);

        mesh.setMinUV(this.minUV);
        mesh.setMaxUV(this.maxUV);

        int[] regions = new int[this.config.regionsIDsToHide.size()];
        {
            int i = 0;
            for (int region : this.config.regionsIDsToHide)
                regions[i++] = region;
        }
        mesh.setRegionIDsToHide(regions);

        mesh.setCostumeCategoriesUsed(CostumePieceCategory.getFlags(this.config.categories));
        mesh.setHairMorphs(this.config.hairMorph);
        mesh.setSkeletonType(this.config.skeleton != null ? this.config.skeleton :
            SkeletonType.SACKBOY);

        return mesh;
    }

    public HashMap<MaterialModel, Primitive> getPrimitiveMappings()
    {
        return this.gltfMaterials;
    }
}
