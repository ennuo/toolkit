package cwlib.resources;

import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.management.RuntimeErrorException;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.FMatrix4;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.CommonOps_FDRM;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.CellGcmPrimitive;
import cwlib.enums.FlipType;
import cwlib.enums.HairMorph;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.enums.SkeletonType;
import cwlib.external.nvtristrip.PrimitiveGroup;
import cwlib.external.nvtristrip.TriStrip;
import cwlib.io.Resource;
import cwlib.io.exports.MeshExporter;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.custom.Skeleton;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.CullBone;
import cwlib.structs.mesh.ImplicitEllipsoid;
import cwlib.structs.mesh.ImplicitPlane;
import cwlib.structs.mesh.Morph;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.mesh.SoftbodyCluster;
import cwlib.structs.mesh.SoftbodyClusterData;
import cwlib.structs.mesh.SoftbodySpring;
import cwlib.structs.mesh.SoftbodyVertEquivalence;
import cwlib.structs.mesh.Submesh;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.BinaryPrimitives;
import cwlib.util.Bytes;
import cwlib.util.FileIO;

/**
 * Resource that stores skinned meshes.
 */
public class RMesh implements Resource
{
    public static final int STREAM_POS_BONEINDICES = 0x0;
    public static final int STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM = 0x1;
    public static final int STREAM_MORPHS0 = 0x2;

    public static final int MAX_MORPHS = 0x20;

    private static final int BASE_ALLOCATION_SIZE =
        0x400 + SoftbodyClusterData.BASE_ALLOCATION_SIZE;

    /**
     * The number of vertices this mesh has.
     */
    private int numVerts;

    /**
     * The number of face indices this mesh has.
     */
    private int numIndices;

    /**
     * The number of edge indices his mesh has.
     */
    public int numEdgeIndices;

    /**
     * The number of triangles this mesh has.
     */
    private int numTris;

    /**
     * The number of vertex streams this model has.
     */
    private int streamCount;

    /**
     * How number of UVs this model has.
     */
    private int attributeCount;

    /**
     * The number of morphs this model has.
     */
    private int morphCount;

    /**
     * Names of the morphs in-use by this this.
     */
    private String[] morphNames = new String[MAX_MORPHS];

    private float[] minUV = new float[] { 0.0f, 0.0f, };
    private float[] maxUV = new float[] { 1.0f, 1.0f };
    private float areaScaleFactor = 500;

    /**
     * STREAM[0] = v3 Pos, u32 Bone Indices
     * STREAM[1] = u8 w2, u8 w1, u8 w0, u8 b1, u24 norm, u8 b2, u24 tangent, u8 b3, u24
     * smoothnormal, b4
     */
    private byte[][] streams;

    /**
     * Texture coordinate (UV) buffer.
     */
    private byte[] attributes;

    /**
     * Face indices buffer.
     */
    public byte[] indices;

    /**
     * Triangle adjacency buffer used for
     * geometry shaders in LittleBigPlanet 3.
     */
    private byte[] triangleAdjacencyInfos;

    /**
     * Primitive objects controlling how each part of this
     * mesh gets rendered.
     */
    private ArrayList<Primitive> primitives = new ArrayList<>();

    /**
     * The skeleton of this this.
     */
    private Bone[] bones;

    /**
     * Each index refers to a bone, the value at each index
     * refers to the bone of which the first is a mirror.
     */
    private short[] mirrorBones;

    /**
     * The type of mirroring between the bones.
     */
    private FlipType[] mirrorBoneFlipTypes;

    /**
     * Each index refers to a morph, the value at each index
     * refers to the morph of which the first is a mirror.
     */
    private short[] mirrorMorphs;

    /**
     * How this mesh will get rendered.
     */
    private CellGcmPrimitive primitiveType = CellGcmPrimitive.TRIANGLE_STRIP;

    private SoftbodyClusterData softbodyCluster;
    private SoftbodySpring[] softbodySprings;

    /**
     * Vertices that are equivalent, but only separate for texturing reasons.
     */
    private ArrayList<SoftbodyVertEquivalence> softbodyEquivs = new ArrayList<>();

    /**
     * Mass of each vertex, used for softbody physics.
     */
    private float[] mass;

    private ImplicitEllipsoid[] implicitEllipsoids = new ImplicitEllipsoid[0];
    private Matrix4f[] clusterImplicitEllipsoids = new Matrix4f[0];
    private ImplicitEllipsoid[] insideImplicitEllipsoids = new ImplicitEllipsoid[0];
    private ImplicitPlane[] implicitPlanes = new ImplicitPlane[0];

    /**
     * Settings for how this mesh's softbody physics behave.
     */
    private ResourceDescriptor softPhysSettings;

    /**
     * Min and max vertices where this mesh contains springs.
     */
    private int minSpringVert, maxSpringVert;

    private int minUnalignedSpringVert;

    /**
     * Indices of the mesh that are springy.
     */
    private short[] springyTriIndices;

    /**
     * Whether or not the mesh's spring indices
     * are tri-stripped or not.
     * (Stored as an integer, but only used as a boolean)
     */
    private boolean springTrisStripped;

    private Vector4f softbodyContainingBoundBoxMin;
    private Vector4f softbodyContainingBoundBoxMax;

    /**
     * Bones that control render culling behavior.
     */
    private CullBone[] cullBones;

    /**
     * Parts of the player mesh that gets hidden
     * when you wear this mesh as a costume piece.
     */
    private int[] regionIDsToHide;

    /**
     * Costume slots this mesh takes up.
     */
    private int costumeCategoriesUsed;

    /**
     * How this costume piece hides a player's
     * hair when it's equipped.
     */
    private HairMorph hairMorphs = HairMorph.HAT;

    private int bevelVertexCount;
    private boolean implicitBevelSprings;

    private int[] vertexColors;

    /**
     * Which character this mesh is for.
     */
    private SkeletonType skeletonType = SkeletonType.SACKBOY;
    
    /* Creates an empty mesh, used for serialization. */
    public RMesh() { }

    public RMesh(byte[][] streams, byte[] attributes, byte[] indices, Bone[] bones)
    {
        // We're not using triangle strips, we're using triangle lists,
        // so treat the indices buffer as such.
        this.primitiveType = CellGcmPrimitive.TRIANGLES;
        if (indices != null)
        {
            if (indices.length % 0x6 != 0)
                throw new IllegalArgumentException("Indices buffer must be divisible by " +
                                                   "0x6 since" +
                                                   " it contains triangle data!");
            this.numTris = indices.length / 0x6;
            this.numIndices = indices.length / 0x2;
        }
        this.init(streams, attributes, indices, bones);
    }

    /**
     * Creates a new mesh from raw streams.
     * NOTE: Edge indices are not included.
     * NOTE: Primitive type is assumed to be GL_TRIANGLES.
     *
     * @param streams    All vertex streams, including geometry, skinning/lighting, and morphs.
     * @param attributes Texture coordinate (UV) data stream.
     * @param indices    Face indices stream
     * @param bones      Skeleton of this mesh
     */
    public RMesh(byte[][] streams, byte[] attributes, byte[] indices, Bone[] bones,
                 int numIndices, int numEdgeIndices, int numTris)
    {
        this.numIndices = numIndices;
        this.numEdgeIndices = numEdgeIndices;
        this.numTris = numTris;
        this.init(streams, attributes, indices, bones);
    }

    private void init(byte[][] streams, byte[] attributes, byte[] indices, Bone[] bones)
    {
        // Just return an empty mesh if the streams are null or empty,
        // it's effectively as such anyway.
        if (streams == null || streams.length == 0) return;

        if (streams.length > 34)
            throw new IllegalArgumentException("Meshes cannot have more than 32 morphs!");

        // Validate the vertex streams, we won't actually check
        // if the data in the streams are valid, because that's
        // the user's problem!
        int size = streams[0].length;
        for (byte[] stream : streams)
        {
            if (stream.length != size)
                throw new IllegalArgumentException("All vertex streams must be the same " +
                                                   "size!");
            if ((stream.length % 0x10) != 0)
                throw new IllegalArgumentException("All vertex streams must be divisible " +
                                                   "by 0x10!");
        }

        this.streams = streams;
        this.streamCount = streams.length;
        this.numVerts = size / 0x10;

        // This won't be used if the mesh doesn't have softbody
        // data, but we'll still create the array anway.
        this.mass = new float[this.numVerts];

        // If the stream length is 2, it means we
        // only have geometry and skinning data, no morph data.
        if (this.streams.length > 2)
        {
            this.morphCount = this.streams.length - 2;
            this.mirrorMorphs = new short[this.morphCount];
        }
        else this.morphCount = 0;

        if (attributes != null)
        {
            // A single attribute stream should consist of
            // UV0 (8 bytes per vertex).
            int attributeSize = this.numVerts * 0x8;

            if ((attributes.length % attributeSize) != 0)
                throw new IllegalArgumentException("Attribute buffer doesn't match vertex" +
                                                   " count!");

            this.attributeCount = attributes.length / attributeSize;
        }
        this.attributes = attributes;
        this.indices = indices;

        // Initialize the mirror arrays if the bones aren't null.
        if (bones != null)
        {
            this.mirrorBones = new short[bones.length];
            for (int i = 0; i < this.mirrorBones.length; ++i)
                this.mirrorBones[i] = (short) i;
            this.mirrorBoneFlipTypes = new FlipType[bones.length];
            Arrays.fill(this.mirrorBoneFlipTypes, FlipType.MAX);
        }
        this.bones = bones;
        this.cullBones = new CullBone[this.bones.length];
        for (int i = 0; i < this.bones.length; ++i)
        {
            CullBone bone = new CullBone();
            bone.boundBoxMax = this.bones[i].boundBoxMax;
            bone.boundBoxMin = this.bones[i].boundBoxMin;
            bone.invSkinPoseMatrix = this.bones[i].invSkinPoseMatrix;
            this.cullBones[i] = bone;
        }

        this.vertexColors = new int[this.numVerts];
        for (int i = 0; i < this.numVerts; ++i)
            this.vertexColors[i] = 0xFFFFFFFF;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        this.numVerts = serializer.i32(this.numVerts);
        this.numIndices = serializer.i32(this.numIndices);
        this.numEdgeIndices = serializer.i32(this.numEdgeIndices);
        this.numTris = serializer.i32(this.numTris);
        this.streamCount = serializer.i32(this.streamCount);
        this.attributeCount = serializer.i32(this.attributeCount);
        this.morphCount = serializer.i32(this.morphCount);

        if (!serializer.isWriting())
            this.morphNames = new String[MAX_MORPHS];
        for (int i = 0; i < MAX_MORPHS; ++i)
            this.morphNames[i] = serializer.str(this.morphNames[i], 0x10);

        if (version >= Revisions.MESH_MINMAX_UV)
        {
            this.minUV = serializer.floatarray(this.minUV);
            this.maxUV = serializer.floatarray(this.maxUV);
            this.areaScaleFactor = serializer.f32(this.areaScaleFactor);
        }

        if (serializer.isWriting())
        {
            int offset = 0;
            MemoryOutputStream stream = serializer.getOutput();
            stream.i32(offset);
            for (int i = 0; i < this.streams.length; ++i)
            {
                offset += this.streams[i].length;
                stream.i32(offset);
            }
            stream.i32(offset);
            for (byte[] vertexStream : this.streams)
                stream.bytes(vertexStream);
        }
        else
        {
            MemoryInputStream stream = serializer.getInput();
            // We're skipping source stream offsets.
            for (int i = 0; i < this.streamCount + 1; ++i)
                stream.i32();
            stream.i32();

            this.streams = new byte[this.streamCount][];
            for (int i = 0; i < this.streamCount; ++i)
                this.streams[i] = stream.bytes(this.numVerts * 0x10);
        }

        this.attributes = serializer.bytearray(this.attributes);
        this.indices = serializer.bytearray(this.indices);
        if (subVersion >= Revisions.FUZZ)
            this.triangleAdjacencyInfos = serializer.bytearray(this.triangleAdjacencyInfos);

        this.primitives = serializer.arraylist(this.primitives, Primitive.class);
        this.bones = serializer.array(this.bones, Bone.class);

        this.mirrorBones = serializer.shortarray(this.mirrorBones);
        this.mirrorBoneFlipTypes = serializer.enumarray(this.mirrorBoneFlipTypes,
            FlipType.class);
        this.mirrorMorphs = serializer.shortarray(this.mirrorMorphs);

        this.primitiveType = serializer.enum8(this.primitiveType);

        this.softbodyCluster = serializer.struct(this.softbodyCluster,
            SoftbodyClusterData.class);
        this.softbodySprings = serializer.array(this.softbodySprings, SoftbodySpring.class);
        this.softbodyEquivs = serializer.arraylist(this.softbodyEquivs,
            SoftbodyVertEquivalence.class);

        // Don't write mass field if there's no softbody data on the this.
        if (serializer.isWriting())
        {
            if (this.hasSoftbodyData()) serializer.floatarray(this.mass);
            else serializer.getOutput().i32(0);
        }
        else this.mass = serializer.floatarray(this.mass);

        this.implicitEllipsoids = serializer.array(this.implicitEllipsoids,
            ImplicitEllipsoid.class);

        if (!serializer.isWriting())
            this.clusterImplicitEllipsoids = new Matrix4f[serializer.getInput().i32()];
        else serializer.getOutput().i32(this.clusterImplicitEllipsoids.length);
        for (int i = 0; i < this.clusterImplicitEllipsoids.length; ++i)
            this.clusterImplicitEllipsoids[i] =
                serializer.m44(this.clusterImplicitEllipsoids[i]);

        this.insideImplicitEllipsoids = serializer.array(this.insideImplicitEllipsoids,
            ImplicitEllipsoid.class);
        this.implicitPlanes = serializer.array(this.implicitPlanes, ImplicitPlane.class);
        this.softPhysSettings = serializer.resource(this.softPhysSettings,
            ResourceType.SETTINGS_SOFT_PHYS);
        this.minSpringVert = serializer.i32(this.minSpringVert);
        this.maxSpringVert = serializer.i32(this.maxSpringVert);
        this.minUnalignedSpringVert = serializer.i32(this.minUnalignedSpringVert);
        this.springyTriIndices = serializer.shortarray(this.springyTriIndices);
        this.springTrisStripped = serializer.intbool(this.springTrisStripped);
        this.softbodyContainingBoundBoxMin = serializer.v4(this.softbodyContainingBoundBoxMin);
        this.softbodyContainingBoundBoxMax = serializer.v4(this.softbodyContainingBoundBoxMax);

        this.cullBones = serializer.array(this.cullBones, CullBone.class);
        this.regionIDsToHide = serializer.intvector(this.regionIDsToHide);

        this.costumeCategoriesUsed = serializer.i32(this.costumeCategoriesUsed);
        if (version >= 0x141)
            this.hairMorphs = serializer.enum32(this.hairMorphs);
        this.bevelVertexCount = serializer.i32(this.bevelVertexCount);
        this.implicitBevelSprings = serializer.bool(this.implicitBevelSprings);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_VERTEX_COLORS))
        {
            if (!serializer.isWriting())
                this.vertexColors = new int[serializer.getInput().i32()];
            else serializer.getOutput().i32(this.vertexColors.length);
            for (int i = 0; i < this.vertexColors.length; ++i)
                this.vertexColors[i] = serializer.i32(this.vertexColors[i], true);
        }
        else if (!serializer.isWriting())
        {
            this.vertexColors = new int[0];
            // this.vertexColors = new int[this.numVerts];
            // for (int i = 0; i < this.numVerts; ++i)
            //     this.vertexColors[i] = 0xFFFFFFFF;
        }

        if (subVersion >= Revisions.MESH_SKELETON_TYPE)
            this.skeletonType = serializer.enum8(this.skeletonType);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;

        if (this.streams != null)
            for (byte[] stream : this.streams)
                size += stream.length;
        if (this.attributes != null) size += this.attributes.length;
        if (this.indices != null) size += this.indices.length;
        if (this.triangleAdjacencyInfos != null) size += this.triangleAdjacencyInfos.length;

        if (this.primitives != null)
            size += (this.primitives.size() * Primitive.BASE_ALLOCATION_SIZE);

        if (this.bones != null)
            for (Bone bone : this.bones)
                size += bone.getAllocatedSize();

        if (this.mirrorBones != null) size += (this.mirrorBones.length * 2);
        if (this.mirrorBoneFlipTypes != null) size += this.mirrorBoneFlipTypes.length;
        if (this.mirrorMorphs != null) size += (this.mirrorMorphs.length * 2);
        if (this.softbodyCluster != null) size += this.softbodyCluster.getAllocatedSize();
        if (this.softbodySprings != null)
            size += (this.softbodySprings.length * SoftbodySpring.BASE_ALLOCATION_SIZE);
        if (this.softbodyEquivs != null)
            size += (this.softbodyEquivs.size() * SoftbodyVertEquivalence.BASE_ALLOCATION_SIZE);
        if (this.mass != null) size += (this.mass.length * 4);
        if (this.implicitEllipsoids != null)
            size += (this.implicitEllipsoids.length * ImplicitEllipsoid.BASE_ALLOCATION_SIZE);
        if (this.clusterImplicitEllipsoids != null)
            size += (this.clusterImplicitEllipsoids.length * 0x40);
        if (this.insideImplicitEllipsoids != null)
            size += (this.insideImplicitEllipsoids.length * ImplicitEllipsoid.BASE_ALLOCATION_SIZE);
        if (this.implicitPlanes != null)
            size += (this.implicitPlanes.length * ImplicitPlane.BASE_ALLOCATION_SIZE);
        if (this.springyTriIndices != null) size += (this.springyTriIndices.length * 2);
        if (this.cullBones != null)
            size += (this.cullBones.length * CullBone.BASE_ALLOCATION_SIZE);
        if (this.regionIDsToHide != null) size += (this.regionIDsToHide.length * 0x8);

        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize() + 0x8000, revision,
            compressionFlags);
        serializer.struct(this, RMesh.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.MESH,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }

    /**
     * Checks if this mesh uses tri-strips.
     *
     * @return True if the mesh uses tri-strips, false otherwise.
     */
    public boolean isStripped()
    {
        return this.primitiveType.equals(CellGcmPrimitive.TRIANGLE_STRIP);
    }

    /**
     * Checks if this mesh uses softbody simulation.
     *
     * @return True if the mesh uses softbody simulation, false otherwise.
     */
    public boolean hasSoftbodyData()
    {
        if (this.softbodyCluster == null) return false;
        return this.softbodyCluster.getClusters().size() != 0;
    }

    public int getNumVerts()
    {
        return this.numVerts;
    }

    public int getNumIndices()
    {
        return this.numIndices;
    }

    public int getNumEdgeIndices()
    {
        return this.numEdgeIndices;
    }

    public int getNumTris()
    {
        return this.numTris;
    }

    public int getStreamCount()
    {
        return this.streamCount;
    }

    public int getAttributeCount()
    {
        return this.attributeCount;
    }

    public int getMorphCount()
    {
        return this.morphCount;
    }

    public String[] getMorphNames()
    {
        return this.morphNames;
    }

    public Vector2f getMinUV()
    {
        if (this.minUV == null || this.minUV.length != 2)
            return new Vector2f().zero();
        return new Vector2f(this.minUV[0], this.minUV[1]);
    }

    public Vector2f getMaxUV()
    {
        if (this.maxUV == null || this.maxUV.length != 2)
            return new Vector2f().zero();
        return new Vector2f(this.maxUV[0], this.maxUV[1]);
    }

    public float getAreaScaleFactor()
    {
        return this.areaScaleFactor;
    }

    /**
     * Gets a stream at specified index.
     *
     * @param index Index of stream
     * @return Stream at index
     */
    public byte[] getVertexStream(int index)
    {
        if (this.streams == null || index < 0 || index >= this.streams.length)
            throw new NullPointerException("Vertex stream at position " + index + " does " +
                                           "not " +
                                           "exist!");
        return this.streams[index];
    }

    public byte[][] getVertexStreams()
    {
        return this.streams;
    }

    /**
     * Gets the main vertex stream.
     *
     * @return Main vertex stream.
     */
    public byte[] getVertexStream()
    {
        if (this.streams.length <= STREAM_POS_BONEINDICES)
            throw new NullPointerException("Vertex stream doesn't exist on this mesh!");
        return this.streams[STREAM_POS_BONEINDICES];
    }

    /**
     * Gets the stream that contains skinning data,
     * as well as normals, tangents, and smooth normals.
     *
     * @return Skinning/Normal stream.
     */
    public byte[] getSkinningStream()
    {
        if (this.streams.length <= STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM)
            throw new NullPointerException("Skinning stream doesn't exist on this mesh!");
        return this.streams[STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM];
    }

    /**
     * Gets all streams containing morph data.
     *
     * @return Morph data streams
     */
    public byte[][] getMorphStreams()
    {
        byte[][] streams = new byte[this.morphCount][];
        if (streamCount - 2 >= 0)
            System.arraycopy(this.streams, 2, streams, 0, streamCount - 2);
        return streams;
    }

    public byte[] getAttributeStream()
    {
        return this.attributes;
    }

    public byte[] getIndexStream()
    {
        return this.indices;
    }

    public byte[] getTriangleAdjacencyStream()
    {
        return this.triangleAdjacencyInfos;
    }

    public ArrayList<Primitive> getPrimitives()
    {
        return this.primitives;
    }

    public Bone[] getBones()
    {
        return this.bones;
    }

    public short[] getMirrorBones()
    {
        return this.mirrorBones;
    }

    public FlipType[] getMirrorTypes()
    {
        return this.mirrorBoneFlipTypes;
    }

    public short[] getMirrorMorphs()
    {
        return this.mirrorMorphs;
    }

    public CellGcmPrimitive getPrimitiveType()
    {
        return this.primitiveType;
    }

    public SoftbodyClusterData getSoftbodyCluster()
    {
        return this.softbodyCluster;
    }

    public SoftbodySpring[] getSoftbodySprings()
    {
        return this.softbodySprings;
    }

    public ArrayList<SoftbodyVertEquivalence> getSoftbodyEquivs()
    {
        return this.softbodyEquivs;
    }

    public float[] getVertexMasses()
    {
        return this.mass;
    }

    public ImplicitEllipsoid[] getImplicitEllipsoids()
    {
        return this.implicitEllipsoids;
    }

    public Matrix4f[] getClusterImplicitEllipsoids()
    {
        return this.clusterImplicitEllipsoids;
    }

    public ImplicitEllipsoid[] getInsideImplicitEllipsoids()
    {
        return this.insideImplicitEllipsoids;
    }

    public ImplicitPlane[] getImplicitPlanes()
    {
        return this.implicitPlanes;
    }

    public ResourceDescriptor getSoftPhysSettings()
    {
        return this.softPhysSettings;
    }

    public int getMinSpringVert()
    {
        return this.minSpringVert;
    }

    public int getMaxSpringVert()
    {
        return this.maxSpringVert;
    }

    public int getMinUnalignedSpringVert()
    {
        return this.minUnalignedSpringVert;
    }

    public short[] getSpringyTriIndices()
    {
        return this.springyTriIndices;
    }

    public boolean areSpringTrisStripped()
    {
        return this.springTrisStripped;
    }

    public Vector4f getSoftbodyContainingBoundBoxMin()
    {
        return this.softbodyContainingBoundBoxMin;
    }

    public Vector4f getSoftbodyContainingBoundBoxMax()
    {
        return this.softbodyContainingBoundBoxMax;
    }

    public CullBone[] getCullBones()
    {
        return this.cullBones;
    }

    public int[] getRegionIDsToHide()
    {
        return this.regionIDsToHide;
    }

    public int getCostumeCategoriesUsed()
    {
        return this.costumeCategoriesUsed;
    }

    public HairMorph getHairMorphs()
    {
        return this.hairMorphs;
    }

    public int getBevelVertexCount()
    {
        return this.bevelVertexCount;
    }

    public boolean implicitBevelSprings()
    {
        return this.implicitBevelSprings;
    }

    public void clearVertexColors()
    {
        this.vertexColors = new int[0];
    }

    public int[] getVertexColors()
    {
        return this.vertexColors;
    }

    public SkeletonType getSkeletonType()
    {
        return this.skeletonType;
    }

    public void setMorphNames(String[] names)
    {
        if (names == null)
            throw new NullPointerException("Morph names cannot be null!");
        if (names.length != MAX_MORPHS)
            throw new IllegalArgumentException("Morph name array must have length of 32!");
        this.morphNames = names;
    }

    /**
     * Sets the morph name at index.
     *
     * @param name  Morph name
     * @param index Index of morph
     */
    public void setMorphName(String name, int index)
    {
        this.morphNames[index] = name;
    }

    public void setMinUV(Vector2f minUV)
    {
        if (minUV == null)
        {
            this.minUV = null;
            return;
        }
        this.minUV = new float[] { minUV.x, minUV.y };
    }

    public void setMaxUV(Vector2f maxUV)
    {
        if (maxUV == null)
        {
            this.maxUV = null;
            return;
        }
        this.maxUV = new float[] { maxUV.x, maxUV.y };
    }

    public void setAreaScaleFactor(float factor)
    {
        this.areaScaleFactor = factor;
    }

    public void setPrimitives(ArrayList<Primitive> primitives)
    {
        this.primitives = primitives;
    }

    /**
     * Sets a bone's mirror.
     *
     * @param index  Bone index
     * @param mirror Bone mirror index
     */
    public void setBoneMirror(int index, int mirror)
    {
        if (this.mirrorBones == null || index < 0 || index >= this.mirrorBones.length)
            throw new NullPointerException("Bone at position " + index + " does not exist!");
        if (this.mirrorBones == null || mirror < 0 || mirror >= this.mirrorBones.length)
            throw new NullPointerException("Bone at position " + mirror + " does not exist!");
        this.mirrorBones[index] = (short) (mirror & 0xFFFF);
    }

    /**
     * Sets a bone's flip type.
     *
     * @param index Bone index
     * @param type  Bone flip type
     */
    public void setBoneFlipType(int index, FlipType type)
    {
        if (this.mirrorBones == null || index < 0 || index >= this.mirrorBones.length)
            throw new NullPointerException("Bone at position " + index + " does not exist!");
        this.mirrorBoneFlipTypes[index] = type;
    }

    public void setMorphMirror(int index, int mirror)
    {
        if (this.mirrorMorphs == null || index < 0 || index >= this.mirrorMorphs.length)
            throw new NullPointerException("Morph at position " + index + " does not exist!");
        if (this.mirrorMorphs == null || mirror < 0 || mirror >= this.mirrorMorphs.length)
            throw new NullPointerException("Morph at position " + mirror + " does not " +
                                           "exist!");
        this.mirrorMorphs[index] = (short) (mirror & 0xFFFF);
    }

    public void setPrimitiveType(CellGcmPrimitive type)
    {
        if (type == null)
            throw new NullPointerException("Primitive type cannot be null!");
        this.primitiveType = type;
    }

    public void setSoftbodyCluster(SoftbodyClusterData cluster)
    {
        if (cluster == null)
            throw new NullPointerException("Softbody cluster information cannot be null!");
        this.softbodyCluster = cluster;
    }

    public void setSoftbodySprings(SoftbodySpring[] springs)
    {
        this.softbodySprings = springs;
    }

    public void setSoftbodyEquivs(ArrayList<SoftbodyVertEquivalence> equivs)
    {
        this.softbodyEquivs = equivs;
    }

    /**
     * Sets the mass of a vertex at a given position,
     * this is used for softbody simulations.
     *
     * @param index Index of the vertex
     * @param mass  New mass of the vertex
     */
    public void setVertexMass(int index, float mass)
    {
        if (this.mass == null || this.mass.length != this.numVerts)
        {
            this.mass = new float[this.numVerts];
            for (int i = 0; i < this.numVerts; ++i)
                this.mass[i] = 1.0f;
        }

        if (index < 0 || index >= this.mass.length)
            throw new NullPointerException("Vertex at position " + index + " does not " +
                                           "exist!");
        this.mass[index] = mass;
    }

    public void setImplicitEllipsoids(ImplicitEllipsoid[] ellipsoids)
    {
        this.implicitEllipsoids = ellipsoids;
    }

    public void setClusterImplicitEllipsoids(Matrix4f[] ellipsoids)
    {
        this.clusterImplicitEllipsoids = ellipsoids;
    }

    public void setInsideImplicitEllipsoids(ImplicitEllipsoid[] ellipsoids)
    {
        this.insideImplicitEllipsoids = ellipsoids;
    }

    public void setImplicitPlanes(ImplicitPlane[] planes)
    {
        this.implicitPlanes = planes;
    }

    public void setSoftPhysSettings(ResourceDescriptor settings)
    {
        this.softPhysSettings = settings;
    }

    public void setMinSpringVert(int vert)
    {
        this.minSpringVert = vert;
    }

    public void setMaxSpringVert(int vert)
    {
        this.maxSpringVert = vert;
    }

    public void setMinUnalignedSpringVert(int vert)
    {
        this.minUnalignedSpringVert = vert;
    }

    public void setSpringyTriIndices(short[] indices)
    {
        this.springyTriIndices = indices;
    }

    public void setSpringTrisStripped(boolean stripped)
    {
        this.springTrisStripped = stripped;
    }

    public void setSoftbodyContainingBoundBoxMin(Vector4f vec)
    {
        this.softbodyContainingBoundBoxMin = vec;
    }

    public void setSoftbodyContainingBoundBoxMax(Vector4f vec)
    {
        this.softbodyContainingBoundBoxMax = vec;
    }

    public void setRegionIDsToHide(int... regions)
    {
        this.regionIDsToHide = regions;
    }

    public void setCostumeCategoriesUsed(int categories)
    {
        this.costumeCategoriesUsed = categories;
    }

    public void setHairMorphs(HairMorph morph)
    {
        if (morph == null)
            throw new NullPointerException("Hair morphs cannot be null!");
        this.hairMorphs = morph;
    }

    public void setBevelVertexCount(int count)
    {
        this.bevelVertexCount = count;
    }

    public void setImplicitBevelSprings(boolean implicit)
    {
        this.implicitBevelSprings = implicit;
    }

    public void setSkeletonType(SkeletonType type)
    {
        if (type == null)
            throw new NullPointerException("Skeleton type cannot be null!");
        this.skeletonType = type;
    }

    /**
     * Gets all sub-meshes contained in this this.
     *
     * @return Sub-meshes
     */
    public Primitive[][] getSubmeshes()
    {
        HashMap<Integer, ArrayList<Primitive>> meshes = new HashMap<>();
        for (Primitive primitive : this.primitives)
        {
            int region = primitive.getRegion();
            if (!meshes.containsKey(region))
            {
                ArrayList<Primitive> primitives = new ArrayList<>();
                primitives.add(primitive);
                meshes.put(region, primitives);
            }
            else meshes.get(region).add(primitive);
        }

        Primitive[][] primitives = new Primitive[meshes.values().size()][];

        int index = 0;
        for (ArrayList<Primitive> primitiveList : meshes.values())
        {
            primitives[index] = primitiveList.toArray(Primitive[]::new);
            ++index;
        }

        return primitives;
    }

    public int getClusterIndex(int vertex)
    {
        return (int)this.getVertexStream()[(vertex * 0x10) + 0xc] / 2;
    }

    /**
     * Gets all the vertices in a specified range
     *
     * @param start First vertex
     * @param count Number of vertices
     * @return Vertices of range
     */
    public Vector3f[] getVertices(int start, int count)
    {
        MemoryInputStream stream = new MemoryInputStream(this.getVertexStream());
        stream.seek(start * 0x10);
        Vector3f[] vertices = new Vector3f[count];
        for (int i = 0; i < count; ++i)
        {
            vertices[i] = stream.v3();

            // stream.i8(cluster_index * 2);
            // stream.i8(0)
            // stream.i8(0)
            // stream.i8(vertex_weight)

            stream.i32(true);
        }
        return vertices;
    }

    /**
     * Gets all the softbody weights in a specified range
     *
     * @param start First vertex
     * @param count Number of vertices
     * @return Softbody weights of range
     */
    public float[] getSoftbodyWeights(int start, int count)
    {
        MemoryInputStream stream = new MemoryInputStream(this.getVertexStream());
        stream.seek(start * 0x10);
        float[] weights = new float[count];
        for (int i = 0; i < count; ++i)
        {
            stream.seek(0xC + 0x3);
            float c = ((float) stream.u8()) / ((float) 0xff);
            weights[i] = c;
        }
        return weights;
    }

    public byte[][] getBlendIndices(int start, int count)
    {
        MemoryInputStream stream = new MemoryInputStream(this.getVertexStream());
        stream.seek(start * 0x10);
        byte[][] vertices = new byte[count][];
        for (int i = 0; i < count; ++i)
        {
            stream.v3();

            // This is always 0x000000FF, the field is bone indices, but those seem
            // depreciated.
            // It's probably only kept for alignment reasons?
            vertices[i] = stream.bytes(4);
        }
        return vertices;
    }

    /**
     * Gets all the vertices from a mesh primitive.
     *
     * @param primitive Mesh primitive to get vertices from
     * @return Vertices
     */
    public Vector3f[] getVertices(Primitive primitive)
    {
        return this.getVertices(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets all the vertices from this this.
     *
     * @return Vertices
     */
    public Vector3f[] getVertices()
    {
        return this.getVertices(0, this.numVerts);
    }

    public void swapUV01()
    {
        for (int i = 0; i < this.numVerts; i++)
        {
            int uv0 = (this.attributeCount * 0x8 * i);
            int uv1 = uv0 + 0x8;

            byte[] tmp = Arrays.copyOfRange(this.attributes, uv0, uv1); // copy uv0 into tmp
            System.arraycopy(this.attributes, uv1, this.attributes, uv0, 0x8);
            System.arraycopy(tmp, 0, this.attributes, uv1, 0x8);
        }
    }

    /**
     * Gets the UVs in a specified range.
     *
     * @param start   First vertex to get UVs of
     * @param count   Number of UVs
     * @param channel UV channel
     * @return UVs
     */
    public Vector2f[] getUVs(int start, int count, int channel)
    {
        if (this.attributes == null)
            throw new IllegalStateException("This mesh doesn't have texture coordinates!");
        if (channel < 0 || (channel + 1 > this.attributeCount))
            throw new IllegalArgumentException("Invalid UV channel!");
        MemoryInputStream stream = new MemoryInputStream(this.attributes);
        Vector2f[] UVs = new Vector2f[count];
        for (int i = 0; i < count; ++i)
        {
            stream.seek(start + (this.attributeCount * 0x8 * i) + (0x8 * channel),
                SeekMode.Begin);
            UVs[i] = stream.v2();
        }
        return UVs;
    }


    /**
     * Gets the UVs of a mesh primitive from a specified channel
     *
     * @param primitive Primitive to get UVs from
     * @param channel   UV channel
     * @return UVs
     */
    public Vector2f[] getUVs(Primitive primitive, int channel)
    {
        return this.getUVs(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1,
            channel
        );
    }

    /**
     * Gets the UVs of this mesh from a specified channel.
     *
     * @param channel UV channel
     * @return UVs
     */
    public Vector2f[] getUVs(int channel)
    {
        return this.getUVs(0, this.numVerts, channel);
    }

    /**
     * Gets the vertex normals in a specified range.
     *
     * @param start First vertex to get normals of
     * @param count Number of vertices
     * @return Vertex normals of range
     */
    public Vector3f[] getNormals(int start, int count)
    {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        Vector3f[] normals = new Vector3f[count];
        for (int i = 0; i < count; ++i)
        {
            stream.seek((start * 0x10) + (i * 0x10) + 0x4, SeekMode.Begin);
            normals[i] = Bytes.unpackNormal24(stream.u24());
        }
        return normals;
    }

    /**
     * Gets all the vertex normals from this this.
     *
     * @return Vertex normals
     */
    public Vector3f[] getNormals()
    {
        return this.getNormals(0, this.numVerts);
    }

    /**
     * Gets all the vertex normals from a mesh primitive.
     *
     * @param primitive Primitive to get vertex normals from
     * @return Vertex normals
     */
    public Vector3f[] getNormals(Primitive primitive)
    {
        return this.getNormals(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets all the vertex tangents from this this.
     *
     * @return Vertex tangents
     */
    public Vector4f[] getTangents()
    {
        return this.getTangents(0, this.numVerts);
    }

    /**
     * Gets the vertex tangents in a specified range.
     *
     * @param start First vertex to get tangents of
     * @param count Number of vertices
     * @return Vertex tangents of range
     */
    public Vector4f[] getTangents(int start, int count)
    {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        Vector4f[] tangents = new Vector4f[count];
        for (int i = 0; i < count; ++i)
        {
            stream.seek((start * 0x10) + (i * 0x10) + 0xc, SeekMode.Begin);
            Vector3f tangent = Bytes.unpackNormal24(stream.u24());
            tangents[i] = new Vector4f(tangent, 1.0f);
        }
        return tangents;
    }

    /**
     * Gets all the vertex tangents from a mesh primitive.
     *
     * @param primitive Primitive to get vertex tangents from
     * @return Vertex tangents
     */
    public Vector4f[] getTangents(Primitive primitive)
    {
        return this.getTangents(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets the smooth vertex normals in a specified range.
     *
     * @param start First vertex to get smooth normals of
     * @param count Number of vertices
     * @return Smooth vertex normals of range
     */
    public Vector3f[] getSmoothNormals(int start, int count)
    {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        Vector3f[] normals = new Vector3f[count];
        for (int i = 0; i < count; ++i)
        {
            stream.seek((start * 0x10) + (i * 0x10) + 0x8, SeekMode.Begin);
            normals[i] = Bytes.unpackNormal24(stream.u24());
        }
        return normals;
    }

    /**
     * Gets all the smooth vertex normals from this this.
     *
     * @return Smooth vertex normals
     */
    public Vector3f[] getSmoothNormals()
    {
        return this.getSmoothNormals(0, this.numVerts);
    }

    /**
     * Gets all the smooth vertex normals from a mesh primitive.
     *
     * @param primitive Primitive to get smooth vertex normals from
     * @return Smooth vertex normals
     */
    public Vector3f[] getSmoothNormals(Primitive primitive)
    {
        return this.getSmoothNormals(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets the joints that have an influence on each vertex in a range.
     *
     * @param start First vertex to get joints of
     * @param count Number of vertices
     * @return Joint indices per vertex in range
     */
    public byte[][] getJoints(int start, int count)
    {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        stream.seek(start * 0x10);
        byte[][] joints = new byte[count][];
        for (int i = 0; i < count; ++i)
        {
            byte[] buffer = stream.bytes(0x10);
            joints[i] = new byte[] {
                buffer[3], buffer[7], buffer[0xB], buffer[0xF]
            };
        }
        return joints;
    }

    /**
     * Gets the joints that have an influence on each vertex in a mesh primitive.
     *
     * @param primitive Primitive to get vertices from
     * @return Joint indices per vertex
     */
    public byte[][] getJoints(Primitive primitive)
    {
        return this.getJoints(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets the joints that have an influence on each vertex.
     *
     * @return Joint indices per vertex
     */
    public byte[][] getJoints()
    {
        return this.getJoints(0, this.numVerts);
    }

    /**
     * Gets a number of weight influences from this skinned mesh,
     * starting from a certain position
     *
     * @param start First vertex to get weights of
     * @param count Number of vertices to get weights of
     * @return Weight influences of vertex range
     */
    public Vector4f[] getWeights(int start, int count)
    {
        MemoryInputStream stream =
            new MemoryInputStream(this.getSkinningStream());
        stream.seek(start * 0x10);
        Vector4f[] weights = new Vector4f[count];
        for (int i = 0; i < count; ++i)
        {
            byte[] buffer = stream.bytes(0x10);
            float[] rawWeights = {
                (float) ((int) buffer[2] & 0xFF),
                (float) ((int) buffer[1] & 0xFF),
                (float) ((int) buffer[0] & 0xFF),
                0
            };

            // If this vertex isn't weighted against a single bone,
            // we'll need to calculate the last weight.
            if (rawWeights[0] != 0xFF)
            {
                rawWeights[3] = 0xFF - rawWeights[2] - rawWeights[1] - rawWeights[0];
                weights[i] = new Vector4f(
                    rawWeights[0] / 0xFF,
                    rawWeights[1] / 0xFF,
                    rawWeights[2] / 0xFF,
                    rawWeights[3] / 0xFF
                );
            }
            else weights[i] = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);
        }
        return weights;
    }

    /**
     * Gets all weight influences from a mesh primitive.
     *
     * @param primitive Primitive to get weights of
     * @return Weight influences of given primitive
     */
    public Vector4f[] getWeights(Primitive primitive)
    {
        return this.getWeights(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets all weight influences from this skinned this.
     *
     * @return Weight influences
     */
    public Vector4f[] getWeights()
    {
        return this.getWeights(0, this.numVerts);
    }

    /**
     * Parses all morphs from the morph streams.
     *
     * @return Parsed morphs
     */
    public Morph[] getMorphs()
    {
        if (this.morphCount == 0)
            throw new IllegalStateException("Can't get morphs from mesh that has no morph " +
                                            "data!");
        Morph[] morphs = new Morph[this.morphCount];
        for (int i = 0; i < this.morphCount; ++i)
        {
            MemoryInputStream stream = new MemoryInputStream(this.streams[i + 2]);
            Vector3f[] offsets = new Vector3f[this.numVerts];
            Vector3f[] normals = new Vector3f[this.numVerts];
            for (int j = 0; j < this.numVerts; ++j)
            {
                offsets[j] = stream.v3();
                normals[j] = Bytes.unpackNormal32(stream.u32(true));
            }
            morphs[i] = new Morph(offsets, normals);
        }
        return morphs;
    }

    /**
     * Calculates a triangle list from a given range in the index buffer.
     *
     * @return Mesh's triangle list
     */
    public int[] getSpringyTriangles()
    {
        if (this.indices == null)
            throw new IllegalStateException("Can't get triangles from mesh without index " +
                                            "buffer!");

        int[] faces = new int[this.springyTriIndices.length];
        short[] stream = this.springyTriIndices;
        for (int i = 0; i < faces.length; ++i)
            faces[i] = stream[i] & 0xffff;

        if (!this.springTrisStripped) return faces;

        ArrayList<Integer> triangles = new ArrayList<>(this.numVerts * 0x3);
        Collections.addAll(triangles, faces[0], faces[1], faces[2]);
        for (int i = 3, j = 1; i < faces.length; ++i, ++j)
        {
            if (faces[i] == 65535)
            {
                Collections.addAll(triangles, faces[i + 1], faces[i + 2], faces[i + 3]);
                i += 3;
                j = 0;
                continue;
            }
            if ((j & 1) != 0)
                Collections.addAll(triangles, faces[i - 2], faces[i], faces[i - 1]);
            else Collections.addAll(triangles, faces[i - 2], faces[i - 1], faces[i]);
        }

        return triangles.stream().mapToInt(Integer::valueOf).toArray();
    }

    /**
     * Calculates a triangle list from a given range in the index buffer.
     *
     * @param start First face to include in list
     * @param count Number of faces from start
     * @return Mesh's triangle list
     */
    public int[] getTriangles(int start, int count)
    {
        if (this.indices == null)
            throw new IllegalStateException("Can't get triangles from mesh without index " +
                                            "buffer!");

        int[] faces = new int[count];
        MemoryInputStream stream = new MemoryInputStream(this.indices);
        stream.seek(start * 0x2);
        for (int i = 0; i < count; ++i)
            faces[i] = stream.u16();

        if (!this.isStripped()) return faces;

        ArrayList<Integer> triangles = new ArrayList<>(this.numVerts * 0x3);
        Collections.addAll(triangles, faces[0], faces[1], faces[2]);
        for (int i = 3, j = 1; i < faces.length; ++i, ++j)
        {
            if (faces[i] == 65535)
            {
                if (i + 3 >= count) break;
                Collections.addAll(triangles, faces[i + 1], faces[i + 2], faces[i + 3]);
                i += 3;
                j = 0;
                continue;
            }
            if ((j & 1) != 0)
                Collections.addAll(triangles, faces[i - 2], faces[i], faces[i - 1]);
            else Collections.addAll(triangles, faces[i - 2], faces[i - 1], faces[i]);
        }

        return triangles.stream().mapToInt(Integer::valueOf).toArray();
    }

    /**
     * Calcuates a triangle list from a given mesh primitive.
     *
     * @param primitive Primitive to get triangles from
     * @return Mesh's triangle list
     */
    public int[] getTriangles(Primitive primitive)
    {
        if (primitive == null)
            throw new NullPointerException("Can't get triangles from null primitive!");
        return this.getTriangles(primitive.getFirstIndex(), primitive.getNumIndices());
    }

    /**
     * Gets this mesh's triangle list, triangulating if necessary.
     *
     * @return Mesh's triangle list
     */
    public int[] getTriangles()
    {
        return this.getTriangles(0, this.numIndices);
    }

    public void applySkeleton(Skeleton skeleton)
    {
        if (skeleton == null) return;
        bones = skeleton.bones;
        cullBones = skeleton.cullBones;
        mirrorBoneFlipTypes = skeleton.mirrorType;
        mirrorBones = skeleton.mirror;

    }

    public void fixupSkinForExport()
    {
        // Vector4f[] weights = getWeights();
        // int[] modulo = new int[] { 0x3, 0x7, 0xb, 0xf };
        // for (int i = 0; i < numVerts; ++i)
        // {
        //     byte[] stream = getSkinningStream();
        //     for (int j = 0; j < 4; ++j)
        //     {
        //         if (weights[i].get(j) == 0.0f)
        //             stream[(i * 0x10) + modulo[j]] = 0;
        //     }

        //     for (int j = 0; j < 4; ++j)
        //     {
        //         int joint = ((int) stream[(i * 0x10) + modulo[j]]) - 1;
        //         if (joint == -1) joint = 0;
        //         stream[(i * 0x10) + modulo[j]] = (byte) (joint);
        //     }
        // }

        for (Bone bone : bones)
        {
            bone.skinPoseMatrix.normalize3x3();
            bone.skinPoseMatrix.setTranslation(bone.skinPoseMatrix.getTranslation(new Vector3f()).mul(MeshExporter.WORLD_SCALE));
            MeshExporter.YUP.mul(bone.skinPoseMatrix, bone.skinPoseMatrix);
            bone.invSkinPoseMatrix = bone.skinPoseMatrix.invert(new Matrix4f());
        }
    }

    public Submesh[] findAllMeshes()
    {
        ArrayList<Submesh> meshes = new ArrayList<>();
        for (int boneIndex = 0; boneIndex < bones.length; ++boneIndex)
        {
            Bone bone = bones[boneIndex];
            if (bone.parent != -1) continue;

            Submesh mesh = new Submesh();
            mesh.transform = bone.skinPoseMatrix;
            mesh.locator = boneIndex;
            mesh.skinned = bone.firstChild != -1;

            ArrayList<Primitive> primitives = new ArrayList<>();
            byte[] stream = getSkinningStream();
            for (Primitive primitive : this.primitives)
            {
                // Just test the first vertex of each primitive to see
                // if they belong to this subthis.
                Bone joint = bones[stream[(primitive.minVert * 0x10) + 0x3]];
                while (joint.parent != -1)
                    joint = bones[joint.parent];

                if (joint == bone)
                    primitives.add(primitive);
            }

            mesh.primitives = primitives.toArray(Primitive[]::new);
            meshes.add(mesh);
        }

        return meshes.toArray(Submesh[]::new);
    }

    private int getSoftbodyVertex(int vertex)
    {
        for (SoftbodyVertEquivalence equiv : softbodyEquivs)
        {
            if (vertex >= equiv.first && vertex <= equiv.first + equiv.count)
                return equiv.first;
        }

        return vertex;
    }

    private static class QuadCandidate
    {
        int TriangleIndex;
        int AdjacentTriangleIndex;
        Quad quad;
        public float Error = 0.0f;

        public QuadCandidate(int ti, int ati, int v0, int v1, int v2, int v3)
        {
            TriangleIndex = ti;
            AdjacentTriangleIndex = ati;
            quad = new Quad(v0, v1, v2, v3);
        }
    }

    private static class EdgeCandidate
    {
        public Triangle A, B;
        public Edge Edge;
        public Quad Quad;
        public float Error;
    }

    public static Vector3f TriangleNormal(Vector3f v1, Vector3f v2, Vector3f v3)
    {
        Vector3f n1 = v1.sub(v2, new Vector3f());
        Vector3f n2 = v2.sub(v3, new Vector3f());
        Vector3f n = n1.cross(n2, new Vector3f());

        if (v1.dot(v2) <= 1.0e035f)
            return n.zero();
        
        return n.normalize();
    }

    public static float TriangleArea(Vector3f v1, Vector3f v2, Vector3f v3)
    {
        Vector3f n1 = v1.sub(v2, new Vector3f());
        Vector3f n2 = v2.sub(v3, new Vector3f());
        Vector3f n = n1.cross(n2, new Vector3f());
        return n.length() * 0.5f;
    }

    public static float AngleNormalized(Vector3f v1, Vector3f v2)
    {
        return v1.angle(v2);
    }

    public static float CalculateQuadError(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3)
    {
        final float FLT_EPSILON = 1.192092896e-07f;
        float error = 0.0f;
        
        // stolen from blender
        // normal difference
        {
            Vector3f n1, n2;
            n1 = TriangleNormal(v0, v1, v2);
            n2 = TriangleNormal(v0, v2, v3);

            float aAngle = n1.equals(n2, FLT_EPSILON) ? 0.0f : AngleNormalized(n1, n2);

            n1 = TriangleNormal(v1, v2, v3);
            n2 = TriangleNormal(v3, v0, v1);
            float bAngle = n1.equals(n2, FLT_EPSILON) ? 0.0f : AngleNormalized(n1, n2);

            float diff = (aAngle + bAngle) / (float)(Math.PI * 2.0);
            assert diff >= 0.0f;

            error += diff;
        }

        // co linearity
        {
            Vector3f[] edges = new Vector3f[]
            {
                v0.sub(v1, new Vector3f()).normalize(),
                v1.sub(v2, new Vector3f()).normalize(),
                v2.sub(v3, new Vector3f()).normalize(),
                v3.sub(v0, new Vector3f()).normalize()
            };

            float diff =
                (float)((Math.abs(AngleNormalized(edges[0], edges[1]) - (Math.PI / 2.0)) +
                Math.abs(AngleNormalized(edges[1], edges[2]) - (Math.PI / 2.0)) +
                Math.abs(AngleNormalized(edges[2], edges[3]) - (Math.PI / 2.0)) +
                Math.abs(AngleNormalized(edges[3], edges[0]) - (Math.PI / 2.0))) / (Math.PI * 2.0));

            assert(diff >= 0.0f);

            error += diff;
        }

        // concavity
        {
            float aArea = TriangleArea(v0, v1, v2) + TriangleArea(v0, v2, v3);
            float bArea = TriangleArea(v1, v2, v3) + TriangleArea(v3, v0, v1);
            
            float min = Math.min(aArea, bArea);
            float max = Math.max(aArea, bArea);

            float diff = max != 0.0f ? (1.0f - (min / max)) : 1.0f;
            assert(diff >= 0.0f);


            error += diff;
        }

        return error;
    }

    public void applyMorphDestructive(String name)
    {
        if (morphCount == 0) return;

        for (int i = 0; i < morphCount; ++i)
        {
            if (!morphNames[i].equals(name)) continue;

            var morph = getMorphs()[i];
            var vertices = getVertices();


            var stream0 = streams[STREAM_POS_BONEINDICES];
            var stream1 = streams[STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM];

            for (int j = 0; j < vertices.length; ++j)
            {
                var v = vertices[j];
                v = v.add(morph.offsets[j]);

                BinaryPrimitives.writeSingleBigEndian(stream0, (j * 0x10) + 0x0, v.x);
                BinaryPrimitives.writeSingleBigEndian(stream0, (j * 0x10) + 0x4, v.y);
                BinaryPrimitives.writeSingleBigEndian(stream0, (j * 0x10) + 0x8, v.z);

                int n = Bytes.packNormal24(morph.normals[j]);

                stream1[(j * 0x10) + 0x4] = (byte)(n >>> 16);
                stream1[(j * 0x10) + 0x5] = (byte)(n >>> 8);
                stream1[(j * 0x10) + 0x6] = (byte)(n & 0xff);
            }
        }

        streams = new byte[][]
        {
            streams[0],
            streams[1]
        };

        streamCount = 2;

        morphCount = 0;
        mirrorMorphs = new short[] {};
        morphNames = new String[MAX_MORPHS];
    }

    // appends another mesh onto this mesh,
    // assumes same bones i dont give a fuck right now
    public void add(RMesh mesh)
    {
        if (attributeCount != mesh.attributeCount)
        {
            if (mesh.attributeCount > attributeCount)
                throw new RuntimeException("fuck");

            // pad out the fucking attribute buffer man
            var output = new MemoryOutputStream(attributeCount * mesh.numVerts * 0x8);
            var uvs = new Vector2f[attributeCount][];
            for (int i = 0; i < attributeCount; ++i)
                uvs[i] = mesh.getUVs(i % mesh.attributeCount);

            for (int i = 0; i < mesh.numVerts; ++i)
            for (int j = 0; j < uvs.length; ++j)
                output.v2(uvs[j][i]);

            mesh.attributes = output.getBuffer();
        }

        // lazy concat, merge it on
        primitives.addAll(mesh.primitives);
        for (var primitive : mesh.primitives)
        {
            primitive.firstIndex += numIndices;
            primitive.minVert += numVerts;
            primitive.maxVert += numVerts;
        }

        streams[0] = Bytes.combine(streams[0], mesh.streams[0]);
        streams[1] = Bytes.combine(streams[1], mesh.streams[1]);
        attributes = Bytes.combine(attributes, mesh.attributes);

        var output = new MemoryOutputStream((numIndices + mesh.numIndices) * 2);
        System.arraycopy(indices, 0, output.getBuffer(), 0, numIndices * 2);
        output.seek(numIndices * 2, SeekMode.Begin);
        for (int j = 0; j < mesh.numIndices; ++j)
        {
            int f = ((mesh.indices[(j * 2) + 0] & 0xFF) << 8) |
                    ((mesh.indices[(j * 2) + 1] & 0xFF));
            
            if (f != 65535)
                f += numVerts;
            
            output.u16(f);
        }

        indices = output.getBuffer();

        numVerts += mesh.numVerts;
        numIndices += mesh.numIndices;
        numTris += mesh.numTris;
        numEdgeIndices = 0;
    }

    public void removeAllClusterData()
    {
        softPhysSettings = null;
        softbodyCluster = new SoftbodyClusterData();
        softbodyEquivs.clear();
        softbodySprings = new SoftbodySpring[] {};

        for (int i = 0; i < streams[0].length; i += 0x10)
        {
            streams[0][i + 0xc] = 0x00;
            streams[0][i + 0xd] = 0x00;
            streams[0][i + 0xe] = 0x00;
            streams[0][i + 0xf] = (byte)0xFF;
        }

        springyTriIndices = new short[] {};
        implicitEllipsoids = new ImplicitEllipsoid[] {};
        insideImplicitEllipsoids = new ImplicitEllipsoid[] {};
        implicitPlanes = new ImplicitPlane[] {};
        mass = new float[] {};
        softbodyEquivs.clear();
        clusterImplicitEllipsoids = new Matrix4f[] {};
    }


    public void generateSprings(ArrayList<SoftbodySpring> springs)
    {
        // Dissolve any springs that are stiff
        float[] weights = getSoftbodyWeights(0, numVerts);
        for (int i = 0; i < springs.size(); ++i)
        {
            var spring = springs.get(i);
            if (weights[spring.A] == 1.0f && weights[spring.B] == 1.0f)
                springs.remove(i--);
        }

        softbodySprings = springs.toArray(SoftbodySpring[]::new);

        var vertexSet = new HashSet<Integer>(softbodySprings.length * 2);
        for (var spring : softbodySprings)
        {
            vertexSet.add(spring.A & 0xffff);
            vertexSet.add(spring.B & 0xffff);
        }

        int[] triangles = getTriangles();
        springyTriIndices = new short[triangles.length];
        int springyTriSize = 0;

        for (int i = 0; i < triangles.length; i += 3)
        {
            int a = triangles[i + 0];
            int b = triangles[i + 1];
            int c = triangles[i + 2];

            // if (vertexSet.contains(a) && vertexSet.contains(b) && vertexSet.contains(c))
            {
                springyTriIndices[springyTriSize++] = (short)a;
                springyTriIndices[springyTriSize++] = (short)b;
                springyTriIndices[springyTriSize++] = (short)c;
            }
        }

        springyTriIndices = Arrays.copyOf(springyTriIndices, springyTriSize);
        springTrisStripped = false;

        minSpringVert = Integer.MAX_VALUE;
        maxSpringVert = Integer.MIN_VALUE;
        for (int i = 0; i < springyTriIndices.length; ++i)
        {
            int face = springyTriIndices[i] & 0xffff;

            if (minSpringVert > face) minSpringVert = face;
            if (maxSpringVert < face) maxSpringVert = face;
        }

        minUnalignedSpringVert = minSpringVert;

        // The minimum spring vertex has to be aligned to
        // an 8 byte boundary.
        while ((minSpringVert % 8) != 0)
            minSpringVert--;

        // Adjust springs into local space
        for (var spring : softbodySprings)
        {
            spring.A -= minSpringVert;
            spring.B -= minSpringVert;
        }

        // dissolve any springs that are functionally duplicate
        var set = new HashSet<SoftbodySpring>(Arrays.asList(softbodySprings));
        softbodySprings = new ArrayList<>(set).toArray(SoftbodySpring[]::new);
    }

    public void generateClusters(String[] clusterNames)
    {
        if (clusterNames == null || clusterNames.length == 0) return;
        
        softbodyCluster = new SoftbodyClusterData();

        var basis = new SoftbodyCluster();
        var clusters = softbodyCluster.getClusters();

        clusters.add(basis);
        for (var name : clusterNames)
            clusters.add(new SoftbodyCluster(name));


        int[] vertsPerCluster = new int[clusters.size()];
        float[] massPerCluster = new float[clusters.size()];
        int[] vertexToCluster = new int[numVerts];
        var vertices = getVertices();
        
        // First pass to gather the center of mass of each cluster
        for (int i = 0; i < vertices.length; ++i)
        {
            int clusterIndex = getClusterIndex(i);
            vertexToCluster[i] = clusterIndex;
            var cluster = clusters.get(clusterIndex);

            vertsPerCluster[clusterIndex]++;
            cluster.TEMP3_BECAUSE_IM_LAZY.add(vertices[i]);
        }

        
        for (int i = 0; i < clusters.size(); ++i)
        {
            var cluster = clusters.get(i);
            float count = vertsPerCluster[i];
            if (count != 0)
                cluster.setRestCenterOfMass(new Vector4f(cluster.TEMP3_BECAUSE_IM_LAZY.div(count), 0.0f));
        }

        mass = new float[numVerts];

        // Second pass to gather the mass of each vertex relative
        // to the center of their clusters
        for (int i = 0; i < vertices.length; ++i)
        {
            int clusterIndex = vertexToCluster[i];
            var com = clusters.get(clusterIndex).getRestCenterOfMass();
            
            float d = 1.0f / vertices[i].distance(com.x, com.y, com.z);
            
            mass[i] = d;
            massPerCluster[clusterIndex] += d;
        }

        for (int i = 0; i < vertices.length; ++i)
            mass[i] /= massPerCluster[vertexToCluster[i]];

        basis.setRestCenterOfMass(clusters.get(1).getRestCenterOfMass());
        
        for (int c = 0; c < clusters.size(); ++c)
        {
            var cluster = clusters.get(c);
            var com3 = cluster.getRestCenterOfMass3();
            Matrix3f aqq = new Matrix3f().zero();
            
            if (vertsPerCluster[c] == 0) continue;

            
            for (int i = 0; i < vertices.length; ++i)
            {
                if (vertexToCluster[i] != c) continue;


                float w = mass[i];
                var q = vertices[i].sub(com3, new Vector3f());

                float qx = q.x;
                float qy = q.y;
                float qz = q.z;


                aqq.add(new Matrix3f(
                    w * qx * qx,
                    w * qy * qx,
                    w * qz * qx,
                    w * qx * qy,
                    w * qy * qy,
                    w * qz * qy,
                    w * qx * qz,
                    w * qy * qz,
                    w * qz * qz
                ));
            }

            

            aqq = aqq.invert();

            cluster.setRestDyadicSum(new Matrix4f().identity().set3x3(aqq));

            if (cluster == basis) continue;

            FMatrixRMaj mat = new FMatrixRMaj(9, 9);
            mat.zero();

            for (int i = 0; i < vertices.length; ++i)
            {
                if (vertexToCluster[i] != c) continue;
                
                float w = mass[i];
                var q = vertices[i].sub(com3, new Vector3f());

                float qx = q.x;
                float qy = q.y;
                float qz = q.z;

                FMatrixRMaj t9x1 = new FMatrixRMaj(9, 1, true, new float[] {
                    qx, qy, qz,
                    qx * qx, qy * qy, qz * qz,
                    qx * qy, qy * qz, qz * qx
                });

                FMatrixRMaj trans = CommonOps_FDRM.transpose(t9x1, new FMatrixRMaj());

                var mat0 = CommonOps_FDRM.mult(t9x1, trans, new FMatrixRMaj());
            
                for (int j = 0; j < 81; ++j)
                    mat.data[j] += mat0.data[j] * w;
            }
            CommonOps_FDRM.invert(mat);

            

            for (int col = 0; col < 9; ++col)
            for (int row = 0; row < 9; ++row)
                cluster.getRestQuadraticDyadicSum()[col * 9 + row] = mat.get(row, col);
        }
    }

    public void generateEquivs()
    {
        Vector3f[] vertices = getVertices();

        // Generate vertex equivalences that are in the range
        // of our softbody spring indices.
        softbodyEquivs.clear();
        int start = 0, count = 1;
        for (int i = 1; i < vertices.length; ++i)
        {
            if (vertices[i].equals(vertices[i - 1]))
            {
                count++;
            }
            else
            {
                if (count != 1)
                    softbodyEquivs.add(new SoftbodyVertEquivalence(start, count));
                
                start = i;
                count = 1;
            }
        }

        if (count != 1)
            softbodyEquivs.add(new SoftbodyVertEquivalence(start, count));
    }

    public void generateEdges()
    {
        int[] triangles = getTriangles();

        HashSet<Edge> edges = new HashSet<>(triangles.length / 2);
        for (int i = 0; i < triangles.length; i += 3)
        {
            int f0 = triangles[i];
            int f1 = triangles[i + 1];
            int f2 = triangles[i + 2];

            if (f0 != f1) edges.add(new Edge(f0, f1));
            if (f1 != f2) edges.add(new Edge(f1, f2));
            if (f2 != f0) edges.add(new Edge(f2, f0));
        }

        numEdgeIndices = edges.size() * 2;
        byte[] buffer = new byte[numIndices * 2 + edges.size() * 4];
        System.arraycopy(indices, 0, buffer, 0, numIndices * 2);
        int offset = numIndices * 2;
        for (Edge edge : edges)
        {
            buffer[offset++] = (byte)(edge.A >>> 8);
            buffer[offset++] = (byte)(edge.A & 0xFF);
            buffer[offset++] = (byte)(edge.B >>> 8);
            buffer[offset++] = (byte)(edge.B & 0xFF);
        }

        indices = buffer;
    }

    public void strip()
    {
        if (primitiveType != CellGcmPrimitive.TRIANGLE_STRIP)
        {
            MemoryOutputStream stream = new MemoryOutputStream(((numVerts - 2) + numVerts) * 4 + (numVerts * 4));

            numTris = 0;
            for (Primitive primitive : primitives)
            {
                int[] triangles = getTriangles(primitive);
                numTris += triangles.length / 3;
                PrimitiveGroup[] groups = new TriStrip().generateStrips(triangles);

                primitive.numIndices = 0;
                primitive.firstIndex = stream.getOffset() / 0x2;
                for (PrimitiveGroup group : groups)
                {
                    for (int i = 0; i < group.numIndices; ++i)
                        stream.u16(group.indices[i]);
                    stream.u16(0xFFFF);
                    primitive.numIndices += group.numIndices + 1;
                }
            }

            numEdgeIndices = 0;
            numIndices = stream.getOffset() / 0x2;
            primitiveType = CellGcmPrimitive.TRIANGLE_STRIP;
            indices = stream.shrink().getBuffer();
            generateEdges();
        }

        if (!springTrisStripped && springyTriIndices != null && springyTriIndices.length != 0)
        {
            PrimitiveGroup[] groups = new TriStrip().generateStrips(getSpringyTriangles());
            int size = 0;
            
            for (PrimitiveGroup group : groups)
                size += group.numIndices + 1;
            size -= 1;

            springyTriIndices = new short[size];
            int i = 0;
            for (PrimitiveGroup group : groups)
            {
                for (int j = 0; j < group.numIndices; ++j)
                    springyTriIndices[i++] = (short)group.indices[j];
                if (i != size)
                    springyTriIndices[i++] = -1;
            }

            springTrisStripped = true;
        }

    }

    public static final class Edge
    {
        public final int A;
        public final int B;

        public Edge(int a, int b)
        {
            if (a < b)
            {
                A = a;
                B = b;
            }
            else
            {
                A = b;
                B = a;
            }
        }

        @Override public boolean equals(Object other)
        {
            if (other == this) return true;
            if (!(other instanceof Edge edge)) return false;
            return edge.A == A && edge.B == B;
        }

        @Override public int hashCode()
        {
            int result = (int) (A ^ (A >>> 32));
            result = 31 * result + B;
            return result;
        }
    }

    public static final class Quad
    {
        public final int v0, v1, v2, v3;

        public Quad(int a, int b, int c, int d)
        {
            v0 = a;
            v1 = b;
            v2 = c;
            v3 = d;
        }
        
        @Override public int hashCode()
        {
            int result = (int) (this.v0 ^ (this.v0 >>> 32));
            result = 31 * result + v1;
            result = 31 * result + v2;
            result = 31 * result + v3;
            return result;
        }

        @Override public boolean equals(Object other)
        {
            if (!(other instanceof Quad quad)) return false;
            return quad.v0 == v0 && quad.v1 == v1 && quad.v2 == v2 && quad.v3 == v3;
        }

        @Override public String toString()
        {
            return String.format("<%d, %d, %d, %d>", v0, v1, v2, v3);
        }
    }


    public static final class Triangle 
    {
        public int A, B, C;

        public Triangle(int a, int b, int c)
        {
            A = a;
            B = b;
            C = c;
        }

        @Override public int hashCode()
        {
            int result = (int) (this.A ^ (this.A >>> 32));
            result = 31 * result + B;
            result = 31 * result + C;
            return result;
        }

        @Override public boolean equals(Object other)
        {
            if (!(other instanceof Triangle tri)) return false;
            
            return tri.A == A && tri.B == B && tri.C == C;
        }

        @Override public String toString()
        {
            return String.format("<%d, %d, %d>", A, B, C);
        }
    }

    public void calculateSmoothNormals()
    {
        Vector3f[] vertices = getVertices();
        HashMap<Vector3f, Vector3f> normalCache = new HashMap<>();
        for (int i = 0; i < vertices.length; ++i)
            normalCache.put(vertices[i], new Vector3f().zero());
        
        ArrayList<Vector3f> sharedVertices = new ArrayList<>(vertices.length);
        HashSet<Triangle> sharedTriangles = new HashSet<>();

        int[] triangles = getTriangles();
        for (int i = 0; i < triangles.length; ++i)
        {
            Vector3f vertex = vertices[triangles[i]];

            int index = sharedVertices.indexOf(vertex);
            if (index != -1)
            {
                triangles[i] = index;
                continue;
            }

            index = sharedVertices.size();
            triangles[i] = index;
            sharedVertices.add(vertex);
        }

        for (int i = 0; i < triangles.length; i += 3)
        {
            int a = triangles[i + 0];
            int b = triangles[i + 1];
            int c = triangles[i + 2];

            if (a == b || a == c || b == c) continue;

            sharedTriangles.add(new Triangle(a, b, c));
        }
        
        // System.out.printf("Reduced triangle count from %d to %d\n", triangles.length / 3, sharedTriangles.size());
        // System.out.printf("Reduced vertex count from %d to %d\n", vertices.length, sharedVertices.size());

        final boolean areaWeighting = false;
        for (Triangle triangle : sharedTriangles)
        {
            Vector3f a = sharedVertices.get(triangle.A);
            Vector3f b = sharedVertices.get(triangle.B);
            Vector3f c = sharedVertices.get(triangle.C);

            Vector3f n = b.sub(a, new Vector3f()).cross(c.sub(a, new Vector3f()));
            if (!areaWeighting) n.normalize();

            float aAngle = b.sub(a, new Vector3f()).angle(c.sub(a, new Vector3f()));
            float bAngle = c.sub(b, new Vector3f()).angle(a.sub(b, new Vector3f()));
            float cAngle = a.sub(c, new Vector3f()).angle(b.sub(c, new Vector3f()));

            // System.out.printf("<%f, %f, %f>\n", aAngle, bAngle, cAngle);

            normalCache.get(a).add(n.mul(aAngle, new Vector3f()));
            normalCache.get(b).add(n.mul(bAngle, new Vector3f()));
            normalCache.get(c).add(n.mul(cAngle, new Vector3f()));
        }

        Vector3f[] normals = getNormals();
        for (int i = 0; i < vertices.length; ++i)
        {
            Vector3f normal = normalCache.get(vertices[i]).normalize();
            if (normal.length() == 0.0f) normal = normals[i];
            
            int n = Bytes.packNormal24(normal);
            int offset = (i * 0x10) + 0x8;
            byte[] stream = streams[RMesh.STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM];
            
            stream[offset + 0] = (byte)(n >>> 16);
            stream[offset + 1] = (byte)(n >>> 8);
            stream[offset + 2] = (byte)(n & 0xff);
        }
    }

    /**
     * Recalculates the bounding boxes for each bone in this model.
     *
     * @param setOBB Whether or not to set the OBB field from AABB bounding boxes.
     */
    public void calculateBoundBoxes(boolean setOBB)
    {
        Vector3f[] vertices = this.getVertices();
        byte[][] joints = this.getJoints();

        HashMap<Bone, Vector4f> minVert = new HashMap<>();
        HashMap<Bone, Vector4f> maxVert = new HashMap<>();

        for (Bone bone : this.bones)
        {
            minVert.put(bone, new Vector4f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 1.0f));
            maxVert.put(bone, new Vector4f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 1.0f));
        }

        for (int i = 0; i < vertices.length; ++i)
        {
            Vector3f v = vertices[i];
            byte[] jointCache = joints[i];
            for (int j = 0; j < 4; ++j)
            {
                Vector4f local = new Vector4f(v, 1.0f).mul(this.bones[jointCache[j]].invSkinPoseMatrix);
                Vector4f max = maxVert.get(this.bones[jointCache[j]]);
                Vector4f min = minVert.get(this.bones[jointCache[j]]);

                max = max.max(local);
                min = min.min(local);
            }
        }

        for (int i = 0; i < this.bones.length; ++i)
        {
            Bone bone = this.bones[i];
            CullBone culler = this.cullBones[i];

            Vector4f max = maxVert.get(bone);
            Vector4f min = minVert.get(bone);

            if (min.x == Float.POSITIVE_INFINITY || max.x == Float.NEGATIVE_INFINITY)
            {
                bone.boundBoxMin = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
                bone.boundBoxMax = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
                bone.boundSphere = new Vector4f().zero();

                culler.boundBoxMin = bone.boundBoxMin;
                culler.boundBoxMax = bone.boundBoxMax;
                culler.invSkinPoseMatrix = bone.invSkinPoseMatrix;

                continue;
            }

            bone.boundBoxMin = min;
            bone.boundBoxMax = max;
            if (setOBB)
            {
                bone.obbMin = bone.boundBoxMin;
                bone.obbMax = bone.boundBoxMax;
            }
            
            Vector4f sphere = max.add(min, new Vector4f()).div(2.0f);
            sphere.w = Math.abs((max.sub(min, new Vector4f()).length() + 0.001f) / 2.0f);
            bone.boundSphere = sphere;

            culler.boundBoxMin = bone.boundBoxMin;
            culler.boundBoxMax = bone.boundBoxMax;
            culler.invSkinPoseMatrix = bone.invSkinPoseMatrix;
        }

        if (springyTriIndices != null && springyTriIndices.length != 0)
        {
            Vector4f min = new Vector4f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 1.0f);
            Vector4f max = new Vector4f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 1.0f);



            int[] indices = getSpringyTriangles();
            for (int i = 0; i < indices.length; ++ i)
            {
                Vector4f local = new Vector4f(vertices[indices[i]], 1.0f);
                max = max.max(local);
                min = min.min(local);
            }

            Vector4f dist = max.absolute().max(min.absolute());
            softbodyContainingBoundBoxMax = new Vector4f(dist.x, dist.y, dist.z, 1.0f);
            softbodyContainingBoundBoxMin = new Vector4f(-dist.x, -dist.y, -dist.z, 1.0f);
        }

        {
            Vector4f min = new Vector4f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 1.0f);
            Vector4f max = new Vector4f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 1.0f);

            
            for (var vertex : vertices)
            {
                Vector4f local = new Vector4f(vertex, 1.0f);
                max = max.max(local);
                min = min.min(local);
            }
            
            Vector4f dist = max.absolute().max(min.absolute());
            softbodyContainingBoundBoxMax = new Vector4f(dist.x, dist.y, dist.z, 1.0f);
            softbodyContainingBoundBoxMin = new Vector4f(-dist.x, -dist.y, -dist.z, 1.0f);
        }


    }


}
