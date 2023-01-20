package cwlib.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.HairMorph;
import cwlib.enums.Branch;
import cwlib.enums.FlipType;
import cwlib.enums.PrimitiveType;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.enums.SkeletonType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.CullBone;
import cwlib.structs.mesh.ImplicitEllipsoid;
import cwlib.structs.mesh.ImplicitPlane;
import cwlib.structs.mesh.Morph;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.mesh.SoftbodyClusterData;
import cwlib.structs.mesh.SoftbodySpring;
import cwlib.structs.mesh.SoftbodyVertEquivalence;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.Bytes;

/**
 * Resource that stores skinned meshes.
 */
public class RMesh implements Compressable, Serializable {
    public static final int STREAM_POS_BONEINDICES = 0x0;
    public static final int STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM = 0x1;
    public static final int STREAM_MORPHS0 = 0x2;
    
    public static final int MAX_MORPHS = 0x20;

    private static final int BASE_ALLOCATION_SIZE = 0x400 + SoftbodyClusterData.BASE_ALLOCATION_SIZE;

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
    private int numEdgeIndices;

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
     * Names of the morphs in-use by this mesh.
     */
    private String[] morphNames = new String[MAX_MORPHS];
    
    private float[] minUV = new float[] { 0.0f, 0.0f, };
    private float[] maxUV = new float[] { 1.0f, 1.0f };
    private float areaScaleFactor = 500;
    
    /**
     * STREAM[0] = v3 Pos, u32 Bone Indices
     * STREAM[1] = u8 w2, u8 w1, u8 w0, u8 b1, u24 norm, u8 b2, u24 tangent, u8 b3, u24 smoothnormal, b4
     */
    private byte[][] streams;

    /**
     * Texture coordinate (UV) buffer.
     */
    private byte[] attributes;

    /**
     * Face indices buffer.
     */
    private byte[] indices;

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
     * The skeleton of this mesh.
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
    private PrimitiveType primitiveType = PrimitiveType.GL_TRIANGLE_STRIP;
    
    private SoftbodyClusterData softbodyCluster;
    private SoftbodySpring[] softbodySprings;

    /**
     * Vertices that are equivalent, but only separate for texturing reasons.
     */
    private SoftbodyVertEquivalence[] softbodyEquivs;

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
    public RMesh() {};

    public RMesh(byte[][] streams, byte[] attributes, byte[] indices, Bone[] bones) {
        // We're not using triangle strips, we're using triangle lists,
        // so treat the indices buffer as such.
        this.primitiveType = PrimitiveType.GL_TRIANGLES;
        if (indices != null) {
            if (indices.length % 0x6 != 0)
                throw new IllegalArgumentException("Indices buffer must be divisible by 0x6 since it contains triangle data!");
            this.numTris = indices.length / 0x6;
            this.numIndices = indices.length / 0x2;
        }
        this.init(streams, attributes, indices, bones);
    }

    /**
     * Creates a new mesh from raw streams.
     * NOTE: Edge indices are not included.
     * NOTE: Primitive type is assumed to be GL_TRIANGLES.
     * @param streams All vertex streams, including geometry, skinning/lighting, and morphs.
     * @param attributes Texture coordinate (UV) data stream.
     * @param indices Face indices stream
     * @param bones Skeleton of this mesh
     */
    public RMesh(byte[][] streams, byte[] attributes, byte[] indices, Bone[] bones, int numIndices, int numEdgeIndices, int numTris) {
        this.numIndices = numIndices;
        this.numEdgeIndices = numEdgeIndices;
        this.numTris = numTris;
        this.init(streams, attributes, indices, bones);
    }

    private void init(byte[][] streams, byte[] attributes, byte[] indices, Bone[] bones) {
        // Just return an empty mesh if the streams are null or empty,
        // it's effectively as such anyway.
        if (streams == null || streams.length == 0) return;

        if (streams.length > 34)
            throw new IllegalArgumentException("Meshes cannot have more than 32 morphs!");

        // Validate the vertex streams, we won't actually check
        // if the data in the streams are valid, because that's
        // the user's problem!
        int size = streams[0].length;
        for (byte[] stream : streams) {
            if (stream.length != size)
                throw new IllegalArgumentException("All vertex streams must be the same size!");
            if ((stream.length % 0x10) != 0)
                throw new IllegalArgumentException("All vertex streams must be divisible by 0x10!");
        }

        this.streams = streams;
        this.streamCount = streams.length;
        this.numVerts = size / 0x10;

        // This won't be used if the mesh doesn't have softbody
        // data, but we'll still create the array anway.
        this.mass = new float[this.numVerts];

        // If the stream length is 2, it means we
        // only have geometry and skinning data, no morph data.
        if (this.streams.length > 2) {
            this.morphCount = this.streams.length - 2;
            this.mirrorMorphs = new short[this.morphCount];
        }
        else this.morphCount = 0;

        if (attributes != null) {
            // A single attribute stream should consist of
            // UV0 (8 bytes per vertex).
            int attributeSize = this.numVerts * 0x8;

            if ((attributes.length % attributeSize) != 0)
                throw new IllegalArgumentException("Attribute buffer doesn't match vertex count!");
            
            this.attributeCount = attributes.length / attributeSize;
        }
        this.attributes = attributes;
        this.indices = indices;

        // Initialize the mirror arrays if the bones aren't null.
        if (bones != null) {
            this.mirrorBones = new short[bones.length];
            this.mirrorBoneFlipTypes = new FlipType[bones.length];
            Arrays.fill(this.mirrorBoneFlipTypes, FlipType.MAX);
        }
        this.bones = bones;
        this.cullBones = new CullBone[bones.length];
        for (int i = 0; i < bones.length; ++i) {
            CullBone bone = new CullBone();
            bone.boundBoxMax = bones[i].boundBoxMax;
            bone.boundBoxMin = bones[i].boundBoxMin;
            bone.invSkinPoseMatrix = bones[i].invSkinPoseMatrix;
            this.cullBones[i] = bone;
        }
        
        this.vertexColors = new int[this.numVerts];
        for (int i = 0; i < this.numVerts; ++i)
            this.vertexColors[i] = 0xFFFFFFFF;
    }

    @SuppressWarnings("unchecked")
    @Override public RMesh serialize(Serializer serializer, Serializable structure) {
        RMesh mesh = (structure == null) ? new RMesh() : (RMesh) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();
        
        mesh.numVerts = serializer.i32(mesh.numVerts);
        mesh.numIndices = serializer.i32(mesh.numIndices);
        mesh.numEdgeIndices = serializer.i32(mesh.numEdgeIndices);
        mesh.numTris = serializer.i32(mesh.numTris);
        mesh.streamCount = serializer.i32(mesh.streamCount);
        mesh.attributeCount = serializer.i32(mesh.attributeCount);
        mesh.morphCount = serializer.i32(mesh.morphCount);
        
        if (!serializer.isWriting()) 
            mesh.morphNames = new String[MAX_MORPHS];
        for (int i = 0; i < MAX_MORPHS; ++i)
            mesh.morphNames[i] = serializer.str(mesh.morphNames[i], 0x10);
        
        if (version >= Revisions.MESH_MINMAX_UV) {
            mesh.minUV = serializer.floatarray(mesh.minUV);
            mesh.maxUV = serializer.floatarray(mesh.maxUV);
            mesh.areaScaleFactor = serializer.f32(mesh.areaScaleFactor);
        }
        
        if (serializer.isWriting()) {
            int offset = 0;
            MemoryOutputStream stream = serializer.getOutput();
            stream.i32(offset);
            for (int i = 0; i < mesh.streams.length; ++i) {
                offset += mesh.streams[i].length;
                stream.i32(offset);
            }
            stream.i32(offset);
            for (byte[] vertexStream : mesh.streams)
                stream.bytes(vertexStream);
        } else {
            MemoryInputStream stream = serializer.getInput();
            // We're skipping source stream offsets. 
            for (int i = 0; i < mesh.streamCount + 1; ++i)
                stream.i32();
            stream.i32();
            
            mesh.streams = new byte[mesh.streamCount][];
            for (int i = 0; i < mesh.streamCount; ++i)
                mesh.streams[i] = stream.bytes(mesh.numVerts * 0x10);
        }
        
        mesh.attributes = serializer.bytearray(mesh.attributes);
        mesh.indices = serializer.bytearray(mesh.indices);
        if (subVersion >= Revisions.FUZZ)
            mesh.triangleAdjacencyInfos = serializer.bytearray(mesh.triangleAdjacencyInfos);
        
        mesh.primitives = serializer.arraylist(mesh.primitives, Primitive.class);
        mesh.bones = serializer.array(mesh.bones, Bone.class);
        
        mesh.mirrorBones = serializer.shortarray(mesh.mirrorBones);
        mesh.mirrorBoneFlipTypes = serializer.enumarray(mesh.mirrorBoneFlipTypes, FlipType.class);
        mesh.mirrorMorphs = serializer.shortarray(mesh.mirrorMorphs);
        
        mesh.primitiveType = serializer.enum8(mesh.primitiveType);
        
        mesh.softbodyCluster = serializer.struct(mesh.softbodyCluster, SoftbodyClusterData.class);
        mesh.softbodySprings = serializer.array(mesh.softbodySprings, SoftbodySpring.class);
        mesh.softbodyEquivs = serializer.array(mesh.softbodyEquivs, SoftbodyVertEquivalence.class);

        // Don't write mass field if there's no softbody data on the mesh.
        if (serializer.isWriting()) {
            if (this.hasSoftbodyData()) serializer.floatarray(mesh.mass);
            else serializer.getOutput().i32(0);
        } else mesh.mass = serializer.floatarray(mesh.mass);

        mesh.implicitEllipsoids = serializer.array(mesh.implicitEllipsoids, ImplicitEllipsoid.class);
        
        if (!serializer.isWriting()) 
            mesh.clusterImplicitEllipsoids = new Matrix4f[serializer.getInput().i32()];
        else serializer.getOutput().i32(mesh.clusterImplicitEllipsoids.length);
        for (int i = 0; i < mesh.clusterImplicitEllipsoids.length; ++i)
            mesh.clusterImplicitEllipsoids[i] = serializer.m44(mesh.clusterImplicitEllipsoids[i]);
        
        mesh.insideImplicitEllipsoids = serializer.array(mesh.insideImplicitEllipsoids, ImplicitEllipsoid.class);
        mesh.implicitPlanes = serializer.array(mesh.implicitPlanes, ImplicitPlane.class);
        mesh.softPhysSettings = serializer.resource(mesh.softPhysSettings, ResourceType.SETTINGS_SOFT_PHYS);
        mesh.minSpringVert = serializer.i32(mesh.minSpringVert);
        mesh.maxSpringVert = serializer.i32(mesh.maxSpringVert);
        mesh.minUnalignedSpringVert = serializer.i32(mesh.minUnalignedSpringVert);
        mesh.springyTriIndices = serializer.shortarray(mesh.springyTriIndices);
        mesh.springTrisStripped = serializer.intbool(mesh.springTrisStripped);
        mesh.softbodyContainingBoundBoxMin = serializer.v4(mesh.softbodyContainingBoundBoxMin);
        mesh.softbodyContainingBoundBoxMax = serializer.v4(mesh.softbodyContainingBoundBoxMax);
        
        mesh.cullBones = serializer.array(mesh.cullBones, CullBone.class);
        mesh.regionIDsToHide = serializer.intvector(mesh.regionIDsToHide);
        
        mesh.costumeCategoriesUsed = serializer.i32(mesh.costumeCategoriesUsed);
        if (version >= 0x141)
            mesh.hairMorphs = serializer.enum32(mesh.hairMorphs);
        mesh.bevelVertexCount = serializer.i32(mesh.bevelVertexCount);
        mesh.implicitBevelSprings = serializer.bool(mesh.implicitBevelSprings);

        if (revision.has(Branch.DOUBLE11, Revisions.D1_VERTEX_COLORS)) {
            if (!serializer.isWriting()) mesh.vertexColors = new int[serializer.getInput().i32()];
            else serializer.getOutput().i32(mesh.vertexColors.length);
            for (int i = 0; i < mesh.vertexColors.length; ++i)
                mesh.vertexColors[i] = serializer.i32(mesh.vertexColors[i], true);
        } else if (!serializer.isWriting()) {
            mesh.vertexColors = new int[mesh.numVerts];
            for (int i = 0; i < mesh.numVerts; ++i)
                mesh.vertexColors[i] = 0xFFFFFFFF;
        }

        if (subVersion >= Revisions.MESH_SKELETON_TYPE)
            mesh.skeletonType = serializer.enum8(mesh.skeletonType);

        serializer.log("end of stream");
        
        return mesh;
    }

    @Override public int getAllocatedSize() { 
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
            size += (this.softbodyEquivs.length * SoftbodyVertEquivalence.BASE_ALLOCATION_SIZE);
        if (this.mass != null) size += (this.mass.length * 4);
        if (this.implicitEllipsoids != null) 
            size += (this.implicitEllipsoids.length * ImplicitEllipsoid.BASE_ALLOCATION_SIZE);
        if (this.clusterImplicitEllipsoids != null) size += (this.clusterImplicitEllipsoids.length * 0x40);
        if (this.insideImplicitEllipsoids != null) 
            size += (this.insideImplicitEllipsoids.length * ImplicitEllipsoid.BASE_ALLOCATION_SIZE);
        if (this.implicitPlanes != null) size += (this.implicitPlanes.length * ImplicitPlane.BASE_ALLOCATION_SIZE);
        if (this.springyTriIndices != null) size += (this.springyTriIndices.length * 2);
        if (this.cullBones != null) size += (this.cullBones.length * CullBone.BASE_ALLOCATION_SIZE);
        if (this.regionIDsToHide != null) size += (this.regionIDsToHide.length * 0x8);
        
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize() + 0x8000, revision, compressionFlags);
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
     * @return True if the mesh uses tri-strips, false otherwise.
     */
    public boolean isStripped() {
        return this.primitiveType.equals(PrimitiveType.GL_TRIANGLE_STRIP);
    }

    /**
     * Checks if this mesh uses softbody simulation.
     * @return True if the mesh uses softbody simulation, false otherwise.
     */
    public boolean hasSoftbodyData() {
        if (this.softbodyCluster == null) return false;
        return this.softbodyCluster.getClusters().size() != 0;
    }

    public int getNumVerts() { return this.numVerts; }
    public int getNumIndices() { return this.numIndices; }
    public int getNumEdgeIndices() { return this.numEdgeIndices; }
    public int getNumTris() { return this.numTris; }
    public int getStreamCount() { return this.streamCount; }
    public int getAttributeCount() { return this.attributeCount; }
    public int getMorphCount() { return this.morphCount; }

    public String[] getMorphNames() { return this.morphNames; }

    public Vector2f getMinUV() {
        if (this.minUV == null || this.minUV.length != 2)
            return new Vector2f().zero();
        return new Vector2f(this.minUV[0], this.minUV[1]);
    }

    public Vector2f getMaxUV() {
        if (this.maxUV == null || this.maxUV.length != 2)
            return new Vector2f().zero();
        return new Vector2f(this.maxUV[0], this.maxUV[1]);
    }

    public float getAreaScaleFactor() { return this.areaScaleFactor; }

    /**
     * Gets a stream at specified index.
     * @param index Index of stream
     * @return Stream at index
     */
    public byte[] getVertexStream(int index) {
        if (this.streams == null || index < 0 || index >= this.streams.length)
            throw new NullPointerException("Vertex stream at position " + index + " does not exist!");
        return this.streams[index];
    }
    public byte[][] getVertexStreams() { return this.streams; }

    /**
     * Gets the main vertex stream.
     * @return Main vertex stream.
     */
    public byte[] getVertexStream() { 
        if (this.streams.length <= STREAM_POS_BONEINDICES)
            throw new NullPointerException("Vertex stream doesn't exist on this mesh!");
        return this.streams[STREAM_POS_BONEINDICES]; 
    }

    /**
     * Gets the stream that contains skinning data,
     * as well as normals, tangents, and smooth normals.
     * @return Skinning/Normal stream.
     */
    public byte[] getSkinningStream() {
        if (this.streams.length <= STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM)
            throw new NullPointerException("Skinning stream doesn't exist on this mesh!");
        return this.streams[STREAM_BONEWEIGHTS_NORM_TANGENT_SMOOTH_NORM]; 
    }

    /**
     * Gets all streams containing morph data.
     * @return Morph data streams
     */
    public byte[][] getMorphStreams() {
        byte[][] streams = new byte[this.morphCount][];
        for (int i = 2; i < streamCount; ++i)
            streams[i - 2] = this.streams[i];
        return streams;
    }

    public byte[] getAttributeStream() { return this.attributes; }
    public byte[] getIndexStream() { return this.indices; }
    public byte[] getTriangleAdjacencyStream() { return this.triangleAdjacencyInfos; }
    
    public ArrayList<Primitive> getPrimitives() { return this.primitives; }

    public Bone[] getBones() { return this.bones; }
    public short[] getMirrorBones() { return this.mirrorBones; }
    public FlipType[] getMirrorTypes() { return this.mirrorBoneFlipTypes; }
    public short[] getMirrorMorphs() { return this.mirrorMorphs; }

    public PrimitiveType getPrimitiveType() { return this.primitiveType; }

    public SoftbodyClusterData getSoftbodyCluster() { return this.softbodyCluster; }
    public SoftbodySpring[] getSoftbodySprings() { return this.softbodySprings; }
    public SoftbodyVertEquivalence[] getSoftbodyEquivs() { return this.softbodyEquivs; }

    public float[] getVertexMasses() { return this.mass; }

    public ImplicitEllipsoid[] getImplicitEllipsoids() { return this.implicitEllipsoids; }
    public Matrix4f[] getClusterImplicitEllipsoids() { return this.clusterImplicitEllipsoids; }
    public ImplicitEllipsoid[] getInsideImplicitEllipsoids() { return this.insideImplicitEllipsoids; }
    public ImplicitPlane[] getImplicitPlanes() { return this.implicitPlanes; }

    public ResourceDescriptor getSoftPhysSettings() { return this.softPhysSettings; }

    public int getMinSpringVert() { return this.minSpringVert; }
    public int getMaxSpringVert() { return this.maxSpringVert; }
    public int getMinUnalignedSpringVert() { return this.minUnalignedSpringVert; }
    public short[] getSpringyTriIndices() { return this.springyTriIndices; }
    public boolean areSpringTrisStripped() { return this.springTrisStripped; }

    public Vector4f getSoftbodyContainingBoundBoxMin() { return this.softbodyContainingBoundBoxMin; }
    public Vector4f getSoftbodyContainingBoundBoxMax() { return this.softbodyContainingBoundBoxMax; }

    public CullBone[] getCullBones() { return this.cullBones; }
    
    public int[] getRegionIDsToHide() { return this.regionIDsToHide; }
    public int getCostumeCategoriesUsed() { return this.costumeCategoriesUsed; }
    public HairMorph getHairMorphs() { return this.hairMorphs; }

    public int getBevelVertexCount() { return this.bevelVertexCount; }
    public boolean implicitBevelSprings() { return this.implicitBevelSprings; }

    public int[] getVertexColors() { return this.vertexColors; }
    public SkeletonType getSkeletonType() { return this.skeletonType; }

    public void setMorphNames(String[] names) {
        if (names == null)
            throw new NullPointerException("Morph names cannot be null!");
        if (names.length != MAX_MORPHS)
            throw new IllegalArgumentException("Morph name array must have length of 32!");
        this.morphNames = names;
    }

    /**
     * Sets the morph name at index.
     * @param name Morph name
     * @param index Index of morph
     */
    public void setMorphName(String name, int index) {
        this.morphNames[index] = name;
    }

    public void setMinUV(Vector2f minUV) {
        if (minUV == null) {
            this.minUV = null;
            return;
        }
        this.minUV = new float[] { minUV.x, minUV.y };
    }

    public void setMaxUV(Vector2f maxUV) {
        if (maxUV == null) {
            this.maxUV = null;
            return;
        }
        this.maxUV = new float[] { maxUV.x, maxUV.y };
    }

    public void setAreaScaleFactor(float factor) { this.areaScaleFactor = factor; }
    public void setPrimitives(ArrayList<Primitive> primitives) { this.primitives = primitives; }

    /**
     * Sets a bone's mirror.
     * @param index Bone index
     * @param mirror Bone mirror index
     */
    public void setBoneMirror(int index, int mirror) {
        if (this.mirrorBones == null || index < 0 || index >= this.mirrorBones.length)
            throw new NullPointerException("Bone at position " + index + " does not exist!");
        if (this.mirrorBones == null || mirror < 0 || mirror >= this.mirrorBones.length)
            throw new NullPointerException("Bone at position " + mirror + " does not exist!");
        this.mirrorBones[index] = (short) (mirror & 0xFFFF);
    }

    /**
     * Sets a bone's flip type.
     * @param index Bone index
     * @param type Bone flip type
     */
    public void setBoneFlipType(int index, FlipType type) {
        if (this.mirrorBones == null || index < 0 || index >= this.mirrorBones.length)
            throw new NullPointerException("Bone at position " + index + " does not exist!");
        this.mirrorBoneFlipTypes[index] = type;
    }

    public void setMorphMirror(int index, int mirror) {
        if (this.mirrorMorphs == null || index < 0 || index >= this.mirrorMorphs.length)
            throw new NullPointerException("Morph at position " + index + " does not exist!");
        if (this.mirrorMorphs == null || mirror < 0 || mirror >= this.mirrorMorphs.length)
            throw new NullPointerException("Morph at position " + mirror + " does not exist!");
        this.mirrorMorphs[index] = (short) (mirror & 0xFFFF);
    }

    public void setPrimitiveType(PrimitiveType type) {
        if (type == null) 
            throw new NullPointerException("Primitive type cannot be null!");
        this.primitiveType = type;
    }

    public void setSoftbodyCluster(SoftbodyClusterData cluster) {
        if (cluster == null)
            throw new NullPointerException("Softbody cluster information cannot be null!");
        this.softbodyCluster = cluster;
    }

    public void setSoftbodySprings(SoftbodySpring[] springs) { this.softbodySprings = springs; }
    public void setSoftbodyEquivs(SoftbodyVertEquivalence[] equivs) { this.softbodyEquivs = equivs; }
    
    /**
     * Sets the mass of a vertex at a given position,
     * this is used for softbody simulations.
     * @param index Index of the vertex
     * @param mass New mass of the vertex
     */
    public void setVertexMass(int index, float mass) {
        if (this.mass == null || this.mass.length != this.numVerts) {
            this.mass = new float[this.numVerts];
            for (int i = 0; i < this.numVerts; ++i)
                this.mass[i] = 1.0f;
        }

        if (index < 0 || index >= this.mass.length)
            throw new NullPointerException("Vertex at position " + index + " does not exist!");
        this.mass[index] = mass;
    }

    public void setImplicitEllipsoids(ImplicitEllipsoid[] ellipsoids) { this.implicitEllipsoids = ellipsoids; }
    public void setClusterImplicitEllipsoids(Matrix4f[] ellipsoids) { this.clusterImplicitEllipsoids = ellipsoids; }
    public void setInsideImplicitEllipsoids(ImplicitEllipsoid[] ellipsoids) { this.insideImplicitEllipsoids = ellipsoids; }
    public void setImplicitPlanes(ImplicitPlane[] planes) { this.implicitPlanes = planes; }

    public void setSoftPhysSettings(ResourceDescriptor settings) { this.softPhysSettings = settings; }

    public void setMinSpringVert(int vert) { this.minSpringVert = vert; }
    public void setMaxSpringVert(int vert) { this.maxSpringVert = vert; }
    public void setMinUnalignedSpringVert(int vert) { this.minUnalignedSpringVert = vert; }
    public void setSpringyTriIndices(short[] indices) { this.springyTriIndices = indices; }
    public void setSpringTrisStripped(boolean stripped) { this.springTrisStripped = stripped; }

    public void setSoftbodyContainingBoundBoxMin(Vector4f vec) { this.softbodyContainingBoundBoxMin = vec; }
    public void setSoftbodyContainingBoundBoxMax(Vector4f vec) { this.softbodyContainingBoundBoxMax = vec; }

    public void setRegionIDsToHide(int... regions) { this.regionIDsToHide = regions; }
    public void setCostumeCategoriesUsed(int categories) { this.costumeCategoriesUsed = categories; }
    public void setHairMorphs(HairMorph morph) {
        if (morph == null)
            throw new NullPointerException("Hair morphs cannot be null!");
        this.hairMorphs = morph;
    }
    
    public void setBevelVertexCount(int count) { this.bevelVertexCount = count; }
    public void setImplicitBevelSprings(boolean implicit) { this.implicitBevelSprings = implicit; }
    public void setSkeletonType(SkeletonType type) {
        if (type == null)
            throw new NullPointerException("Skeleton type cannot be null!");
        this.skeletonType = type;
    }

    /**
     * Gets all sub-meshes contained in this mesh.
     * @return Sub-meshes
     */
    public Primitive[][] getSubmeshes() {
        HashMap<Integer, ArrayList<Primitive>> meshes = new HashMap<>();
        for (Primitive primitive : this.primitives) {
            int region = primitive.getRegion();
            if (!meshes.containsKey(region)) {
                ArrayList<Primitive> primitives = new ArrayList<>();
                primitives.add(primitive);
                meshes.put(region, primitives);
            } else meshes.get(region).add(primitive);
        }
        
        Primitive[][] primitives = new Primitive[meshes.values().size()][];

        int index = 0;
        for (ArrayList<Primitive> primitiveList : meshes.values()) {
            primitives[index] = primitiveList.toArray(Primitive[]::new);
            ++index;
        }

        return primitives;
    }

    /**
     * Gets all the vertices in a specified range
     * @param start First vertex
     * @param count Number of vertices
     * @return Vertices of range
     */
    public Vector3f[] getVertices(int start, int count) {
        MemoryInputStream stream = new MemoryInputStream(this.getVertexStream());
        stream.seek(start * 0x10);
        Vector3f[] vertices = new Vector3f[count];
        for (int i = 0; i < count; ++i) {
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
     * @param start First vertex
     * @param count Number of vertices
     * @return Softbody weights of range
     */
    public float[] getSoftbodyWeights(int start, int count) {
        MemoryInputStream stream = new MemoryInputStream(this.getVertexStream());
        stream.seek(start * 0x10);
        float[] weights = new float[count];
        for (int i = 0; i < count; ++i) {
            stream.seek(0xC + 0x3);
            float c = ((float)stream.u8()) / ((float)0xff);
            weights[i] = c;
        }
        return weights;
    }

    public byte[][] getBlendIndices(int start, int count) {
        MemoryInputStream stream = new MemoryInputStream(this.getVertexStream());
        stream.seek(start * 0x10);
        byte[][] vertices = new byte[count][];
        for (int i = 0; i < count; ++i) {
            stream.v3();

            // This is always 0x000000FF, the field is bone indices, but those seem depreciated.
            // It's probably only kept for alignment reasons?
            vertices[i] = stream.bytes(4);
        }
        return vertices;
    }
    
    /**
     * Gets all the vertices from a mesh primitive.
     * @param primitive Mesh primitive to get vertices from
     * @return Vertices
     */
    public Vector3f[] getVertices(Primitive primitive) {
        return this.getVertices(
            primitive.getMinVert(), 
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }
    
    /**
     * Gets all the vertices from this mesh.
     * @return Vertices
     */
    public Vector3f[] getVertices() { return this.getVertices(0, this.numVerts); }
    
    /**
     * Gets the UVs in a specified range.
     * @param start First vertex to get UVs of
     * @param count Number of UVs
     * @param channel UV channel
     * @return UVs
     */
    public Vector2f[] getUVs(int start, int count, int channel) {
        if (this.attributes == null)
            throw new IllegalStateException("This mesh doesn't have texture coordinates!");
        if (channel < 0 || (channel + 1 > this.attributeCount))
            throw new IllegalArgumentException("Invalid UV channel!");
        MemoryInputStream stream = new MemoryInputStream(this.attributes);
        Vector2f[] UVs = new Vector2f[count];
        for (int i = 0; i < count; ++i) {
            stream.seek(start + (this.attributeCount * 0x8 * i) + (0x8 * channel), SeekMode.Begin);
            UVs[i] = stream.v2();
        }
        return UVs;
    }
    

    /**
     * Gets the UVs of a mesh primitive from a specified channel
     * @param primitive Primitive to get UVs from
     * @param channel UV channel
     * @return UVs
     */
    public Vector2f[] getUVs(Primitive primitive, int channel) {
        return this.getUVs(
            primitive.getMinVert(), 
            (primitive.getMaxVert() - primitive.getMinVert()) + 1,
            channel
        );
    }
    
    /**
     * Gets the UVs of this mesh from a specified channel.
     * @param channel UV channel
     * @return UVs
     */
    public Vector2f[] getUVs(int channel) { return this.getUVs(0, this.numVerts, channel); }
    
    /**
     * Gets the vertex normals in a specified range.
     * @param start First vertex to get normals of
     * @param count Number of vertices
     * @return Vertex normals of range
     */
    public Vector3f[] getNormals(int start, int count) {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        Vector3f[] normals = new Vector3f[count];
        for (int i = 0; i < count; ++i) {
            stream.seek((start * 0x10) + (i * 0x10) + 0x4, SeekMode.Begin);
            normals[i] = Bytes.unpackNormal24(stream.u24());
        }
        return normals;
    }

    /**
     * Gets all the vertex normals from this mesh.
     * @return Vertex normals
     */
    public Vector3f[] getNormals() { return this.getNormals(0, this.numVerts); }
    
    /**
     * Gets all the vertex normals from a mesh primitive.
     * @param primitive Primitive to get vertex normals from
     * @return Vertex normals
     */
    public Vector3f[] getNormals(Primitive primitive) {
        return this.getNormals(
            primitive.getMinVert(), 
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }
    
    /**
     * Gets all the vertex tangents from this mesh.
     * @return Vertex tangents
     */
    public Vector4f[] getTangents() { return this.getTangents(0, this.numVerts); }

    /**
     * Gets the vertex tangents in a specified range.
     * @param start First vertex to get tangents of
     * @param count Number of vertices
     * @return Vertex tangents of range
     */
    public Vector4f[] getTangents(int start, int count) {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        Vector4f[] tangents = new Vector4f[count];
        for (int i = 0; i < count; ++i) {
            stream.seek((start * 0x10) + (i * 0x10) + 0x8, SeekMode.Begin);
            Vector3f tangent = Bytes.unpackNormal24(stream.u24());
            tangents[i] = new Vector4f(tangent, 1.0f);
        }
        return tangents;
    }
    
    /**
     * Gets all the vertex tangents from a mesh primitive.
     * @param primitive Primitive to get vertex tangents from
     * @return Vertex tangents
     */
    public Vector4f[] getTangents(Primitive primitive) {
        return this.getTangents(
            primitive.getMinVert(), 
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets the smooth vertex normals in a specified range.
     * @param start First vertex to get smooth normals of
     * @param count Number of vertices
     * @return Smooth vertex normals of range
     */
    public Vector3f[] getSmoothNormals(int start, int count) {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        Vector3f[] normals = new Vector3f[count];
        for (int i = 0; i < count; ++i) {
            stream.seek((start * 0x10) + (i * 0x10) + 0xC, SeekMode.Begin);
            normals[i] = Bytes.unpackNormal24(stream.u24());
        }
        return normals;
    }
    
    /**
     * Gets all the smooth vertex normals from this mesh.
     * @return Smooth vertex normals
     */
    public Vector3f[] getSmoothNormals() { return this.getSmoothNormals(0, this.numVerts); }
    
    /**
     * Gets all the smooth vertex normals from a mesh primitive.
     * @param primitive Primitive to get smooth vertex normals from
     * @return Smooth vertex normals
     */
    public Vector3f[] getSmoothNormals(Primitive primitive) {
        return this.getSmoothNormals(
            primitive.getMinVert(), 
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets the joints that have an influence on each vertex in a range.
     * @param start First vertex to get joints of
     * @param count Number of vertices
     * @return Joint indices per vertex in range
     */
    public byte[][] getJoints(int start, int count) {
        MemoryInputStream stream = new MemoryInputStream(this.getSkinningStream());
        stream.seek(start * 0x10);
        byte[][] joints = new byte[count][];
        for (int i = 0; i < count; ++i) {
            byte[] buffer = stream.bytes(0x10);
            joints[i] = new byte[] {
                buffer[3], buffer[7], buffer[0xB], buffer[0xF]
            };
        }
        return joints;
    }

    /**
     * Gets the joints that have an influence on each vertex in a mesh primitive.
     * @param primitive Primitive to get vertices from
     * @return Joint indices per vertex
     */
    public byte[][] getJoints(Primitive primitive) {
        return this.getJoints(
            primitive.getMinVert(),
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets the joints that have an influence on each vertex.
     * @return Joint indices per vertex
     */
    public byte[][] getJoints() { return this.getJoints(0, this.numVerts); }

    /**
     * Gets a number of weight influences from this skinned mesh,
     * starting from a certain position
     * @param start First vertex to get weights of
     * @param count Number of vertices to get weights of
     * @return Weight influences of vertex range
     */
    public Vector4f[] getWeights(int start, int count) {
        MemoryInputStream stream = 
            new MemoryInputStream(this.getSkinningStream());
        stream.seek(start * 0x10);
        Vector4f[] weights = new Vector4f[count];
        for (int i = 0; i < count; ++i) {
            byte[] buffer = stream.bytes(0x10);
            float[] rawWeights = {
                (float) ((int)buffer[2] & 0xFF),
                (float) ((int)buffer[1] & 0xFF),
                (float) ((int)buffer[0] & 0xFF),
                0
            };

            // If this vertex isn't weighted against a single bone,
            // we'll need to calculate the last weight.
            if (rawWeights[0] != 0xFF) {
                rawWeights[3] = 0xFE - rawWeights[2] - rawWeights[1] - rawWeights[0];
                weights[i] = new Vector4f(
                    rawWeights[0] / 0xFE,
                    rawWeights[1] / 0xFE,
                    rawWeights[2] / 0xFE,
                    rawWeights[3] / 0xFE
                );
            } else weights[i] = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);
        }
        return weights;
    }

    /**
     * Gets all weight influences from a mesh primitive.
     * @param primitive Primitive to get weights of
     * @return Weight influences of given primitive
     */
    public Vector4f[] getWeights(Primitive primitive) {
        return this.getWeights(
            primitive.getMinVert(), 
            (primitive.getMaxVert() - primitive.getMinVert()) + 1
        );
    }

    /**
     * Gets all weight influences from this skinned mesh.
     * @return Weight influences
     */
    public Vector4f[] getWeights() { return this.getWeights(0, this.numVerts); }

    /**
     * Parses all morphs from the morph streams.
     * @return Parsed morphs
     */
    public Morph[] getMorphs() {
        if (this.morphCount == 0)
            throw new IllegalStateException("Can't get morphs from mesh that has no morph data!");
        Morph[] morphs = new Morph[this.morphCount];
        for (int i = 0; i < this.morphCount; ++i) {
            MemoryInputStream stream = new MemoryInputStream(this.streams[i + 2]);
            Vector3f[] offsets = new Vector3f[this.numVerts];
            Vector3f[] normals = new Vector3f[this.numVerts];
            for (int j = 0; j < this.numVerts; ++j) {
                offsets[j] = stream.v3();
                normals[j] = Bytes.unpackNormal32(stream.u32(true));
            }
            morphs[i] = new Morph(offsets, normals);
        }
        return morphs;
    }

    /**
     * Calculates a triangle list from a given range in the index buffer.
     * @param start First face to include in list
     * @param count Number of faces from start
     * @return Mesh's triangle list
     */
    public int[] getSpringyTriangles(int start, int count) {
        if (this.indices == null)
            throw new IllegalStateException("Can't get triangles from mesh without index buffer!");
        
        int[] faces = new int[count];
        short[] stream = this.springyTriIndices;
        for (int i = start; i < count; ++i)
            faces[i] = stream[i] & 0xffff;
        
        if (!this.springTrisStripped) return faces;

        ArrayList<Integer> triangles = new ArrayList<>(this.numVerts * 0x3);
        Collections.addAll(triangles, faces[0], faces[1], faces[2]);
        for (int i = 3, j = 1; i < faces.length; ++i, ++j) {
            if (faces[i] == 65535) {
                Collections.addAll(triangles, faces[i + 1], faces[i + 2], faces[i + 3]);
                i += 3;
                j = 0;
                continue;
            }
            if ((j & 1) != 0) Collections.addAll(triangles, faces[i - 2], faces[i], faces[i - 1]);
            else Collections.addAll(triangles, faces[i - 2], faces[i - 1], faces[i]);
        }

        return triangles.stream().mapToInt(Integer::valueOf).toArray();
    }

    /**
     * Calculates a triangle list from a given range in the index buffer.
     * @param start First face to include in list
     * @param count Number of faces from start
     * @return Mesh's triangle list
     */
    public int[] getTriangles(int start, int count) {
        if (this.indices == null)
            throw new IllegalStateException("Can't get triangles from mesh without index buffer!");
        
        int[] faces = new int[count];
        MemoryInputStream stream = new MemoryInputStream(this.indices);
        stream.seek(start * 0x2);
        for (int i = 0; i < count; ++i)
            faces[i] = stream.u16();
        
        if (!this.isStripped()) return faces;

        ArrayList<Integer> triangles = new ArrayList<>(this.numVerts * 0x3);
        Collections.addAll(triangles, faces[0], faces[1], faces[2]);
        for (int i = 3, j = 1; i < faces.length; ++i, ++j) {
            if (faces[i] == 65535) {
                if (i + 3 >= count) break;
                Collections.addAll(triangles, faces[i + 1], faces[i + 2], faces[i + 3]);
                i += 3;
                j = 0;
                continue;
            }
            if ((j & 1) != 0) Collections.addAll(triangles, faces[i - 2], faces[i], faces[i - 1]);
            else Collections.addAll(triangles, faces[i - 2], faces[i - 1], faces[i]);
        }

        return triangles.stream().mapToInt(Integer::valueOf).toArray();
    }

    /**
     * Calcuates a triangle list from a given mesh primitive.
     * @param primitive Primitive to get triangles from
     * @return Mesh's triangle list
     */
    public int[] getTriangles(Primitive primitive) {
        if (primitive == null)
            throw new NullPointerException("Can't get triangles from null primitive!");
        return this.getTriangles(primitive.getFirstIndex(), primitive.getNumIndices());
    }

    /**
     * Gets this mesh's triangle list, triangulating if necessary.
     * @return Mesh's triangle list
     */
    public int[] getTriangles() {
        return this.getTriangles(0, this.numIndices);
    }

    
}
