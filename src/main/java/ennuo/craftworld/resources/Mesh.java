package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.resources.structs.mesh.Bone;
import ennuo.craftworld.resources.structs.mesh.MeshPrimitive;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.io.MeshIO;
import ennuo.craftworld.resources.structs.mesh.CullBone;
import ennuo.craftworld.resources.structs.mesh.ImplicitEllipsoid;
import ennuo.craftworld.resources.structs.mesh.ImplicitPlane;
import ennuo.craftworld.resources.structs.mesh.Morph;
import ennuo.craftworld.resources.structs.mesh.SkinWeight;
import ennuo.craftworld.resources.structs.mesh.SoftbodyCluster;
import ennuo.craftworld.resources.structs.mesh.SoftbodySpring;
import ennuo.craftworld.resources.structs.mesh.SoftbodyVertEquivalence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Mesh {
    public String name;
    
    int numTris = 0;
    int morphCount = 0;

    float[] minUV = new float[] { 0.0f, 0.0f };
    float[] maxUV = new float[] { 0.0f, 0.0f };
    
    float areaScaleFactor = 100;

    public short[] faces;
    public short[] extraFaces;
    public short[] edges;
    
    public Vector3f[] vertices;
    public SkinWeight[] weights;
    public Morph[] morphs;
    
    public int attributeCount;
    public int uvCount;

    public Vector2f[][] attributes;
    public String[] morphNames;
    public MeshPrimitive[] meshPrimitives;
    public Bone[] bones;
    public short[] mirrorMorphs;
    public byte primitiveType;
    public SoftbodyCluster softbodyCluster;
    public SoftbodySpring[] softbodySprings;
    public SoftbodyVertEquivalence[] softbodyEquivs;
    public float[] mass;
    public ImplicitEllipsoid[] implicitEllipsoids;
    public float[][] clusterImplicitEllipsoids;
    public ImplicitEllipsoid[] insideImplicitEllipsoids;
    public ImplicitPlane[] implicitPlanes;
    public ResourcePtr softPhysSettings;
    public int minSpringVert, maxSpringVert;
    public int minUnalignedSpringVert;
    public short[] springyTriIndices;
    public int springTrisStripped;
    public Vector4f softbodyContainingBoundBoxMin;
    public Vector4f softbodyContainingBoundBoxMax;
    public CullBone[] cullBones;
    public long[] regionIDsToHide;
    public int costumeCategoriesUsed;
    public int hairMorphs;
    public int bevelVertexCount;
    public boolean implicitBevelSprings;
    public byte skeletonType;


    public Mesh(String name, byte[] data) {
        this.name = name;
        if (data == null) {
            System.err.println("No data provided to Mesh constructor");
            return;
        }
        process(new Resource(data));
    }

    private void process(Resource data) {
        data.decompress(true);
        System.out.println("Parsing Mesh...");
        int numVerts = data.int32(), numIndices = data.int32(),
            numEdgeIndices = data.int32();
        numTris = data.int32();

        int streamCount = data.int32();
        attributeCount = data.int32();
        morphCount = data.int32();

        System.out.println("Morph Count -> " + morphCount);

        morphNames = new String[0x20];
        for (int i = 0; i < 0x20; ++i)
            morphNames[i] = data.str(0x10);
        for (int i = 0; i < morphCount; ++i)
            System.out.println("Morph[" + i + "] -> " + morphNames[i]);

        if (data.revision > 0x238) {
            minUV = data.float32arr();
            maxUV = data.float32arr();
            areaScaleFactor = data.float32();
        }

        int[] streamOffsets = new int[streamCount + 1];
        for (int i = 0; i < streamCount + 1; ++i)
            streamOffsets[i] = data.int32();

        System.out.println("Vertex Count -> " + numVerts);
        System.out.println("Stream Count -> " + streamCount);
        
        int streamSize = data.int32();
        
        vertices = new Vector3f[numVerts];
        for (int i = 0; i < numVerts; ++i) {
            vertices[i] = data.v3();
            data.float32();
        }
        
        if (streamCount > 1) {
            weights = new SkinWeight[numVerts];
            for (int i = 0; i < numVerts; ++i)
                weights[i] = new SkinWeight(data);
            if (streamCount > 2) {
                morphs = new Morph[streamCount - 2];
                for (int i = 0; i < streamCount - 2; ++i) {
                    Vector3f[] vertices = new Vector3f[numVerts];
                    Vector3f[] normals = new Vector3f[numVerts];
                    for (int j = 0; j < numVerts; ++j) {
                        vertices[j] = data.v3();
                        normals[j] = SkinWeight.decodeI32(data.uint32f());
                    }
                    Morph morph = new Morph(morphNames[streamCount - 2]);
                    morph.vertices = vertices;
                    morph.normals = normals;
                    morphs[i] = morph;
                }
            }
        }

        System.out.println("UV Offset -> 0x" + Bytes.toHex(data.offset));

        int uvSize = data.int32();
        uvCount = uvSize / (0x8 * attributeCount);

        System.out.println("UV Count -> " + uvCount);
        attributes = new Vector2f[uvCount][];
        for (int i = 0; i < uvCount; ++i) {
            attributes[i] = new Vector2f[attributeCount];
            for (int j = 0; j < attributeCount; ++j)
                attributes[i][j] = data.v2();
        }

        System.out.println("Indices Offset -> 0x" + Bytes.toHex(data.offset));
        int faceSize = data.int32();
        System.out.println("Index Count -> " + numIndices);
        faces = new short[numIndices];
        for (int i = 0; i < numIndices; ++i)
            faces[i] = data.int16();
        System.out.println("Edge Indices Offset -> 0x" + Bytes.toHex(data.offset));
        System.out.println("Edge Index Count -> " + numEdgeIndices);
        edges = new short[numEdgeIndices];
        for (int i = 0; i < numEdgeIndices; ++i)
            edges[i] = data.int16();

        if (data.revision >= 0x016b03ef) {
            System.out.println("Extra Indices Offset -> 0x" + Bytes.toHex(data.offset));
            int extraFaceCount = data.int32() / 2;
            System.out.println(extraFaceCount);
            extraFaces = new short[extraFaceCount];
            for (int i = 0; i < extraFaceCount; ++i)
                extraFaces[i] = data.int16();
        }

        System.out.println("Mesh Primitives Offset -> 0x" + Bytes.toHex(data.offset));
        meshPrimitives = MeshPrimitive.array(data);

        System.out.println("Bones Offset -> 0x" + Bytes.toHex(data.offset));
        bones = Bone.array(data);

        int mirrorCount = data.int32();
        for (int i = 0; i < mirrorCount; ++i)
            bones[i].mirror = data.int16();
        int mirrorTypeCount = data.int32();
        for (int i = 0; i < mirrorTypeCount; ++i)
            bones[i].mirrorType = data.int8();

        
        /*
        for (int i = 1; i < bones.length; ++i) {
            Bone bone = bones[i];
            System.out.println(
                String.format("Bone %d [%s] (%s) has Parent=%d (%s) and Mirror=%d (%s) with MirrorType=%s", 
                i, Bytes.toHex(bone.animHash), bone.name, bone.parent, bones[bone.parent].name, bone.mirror, bones[bone.mirror].name, getMirrorType(bone.mirrorType))
            );   
        }
        */

        System.out.println("Mirror Morph Offset  -> 0x" + Bytes.toHex(data.offset));
        int mirrorMorphCount = data.int32();
        mirrorMorphs = new short[mirrorMorphCount];
        for (int i = 0; i < mirrorMorphCount; ++i)
            mirrorMorphs[i] = data.int16();

        System.out.println("Primitive Type Offset  -> 0x" + Bytes.toHex(data.offset));
        primitiveType = data.int8();
        System.out.println("Softbody Cluster Offset  -> 0x" + Bytes.toHex(data.offset));
        softbodyCluster = new SoftbodyCluster(data);
        System.out.println("Softbody Springs Offset  -> 0x" + Bytes.toHex(data.offset));
        softbodySprings = SoftbodySpring.array(data);
        System.out.println("Softbody Vert Equivalences Offset  -> 0x" + Bytes.toHex(data.offset));
        softbodyEquivs = SoftbodyVertEquivalence.array(data);
        System.out.println("Mass Offset  -> 0x" + Bytes.toHex(data.offset));
        mass = data.float32arr();
        System.out.println("Implicit Ellipsoids Offset  -> 0x" + Bytes.toHex(data.offset));
        implicitEllipsoids = ImplicitEllipsoid.array(data);
        System.out.println("Cluster Implicit Ellipsoids Offset  -> 0x" + Bytes.toHex(data.offset));
        clusterImplicitEllipsoids = new float[data.int32()][];
        for (int i = 0; i < clusterImplicitEllipsoids.length; ++i)
            clusterImplicitEllipsoids[i] = data.matrix();
        System.out.println("Inside Implicit Ellipsoids Offset  -> 0x" + Bytes.toHex(data.offset));
        insideImplicitEllipsoids = ImplicitEllipsoid.array(data);
        System.out.println("Implicit Planes Offset  -> 0x" + Bytes.toHex(data.offset));
        implicitPlanes = ImplicitPlane.array(data);
        System.out.println("Soft Phys Settings Offset  -> 0x" + Bytes.toHex(data.offset));
        softPhysSettings = data.resource(RType.SETTINGS_SOFT_PHYS);
        System.out.println("Min Spring Vert Offset  -> 0x" + Bytes.toHex(data.offset));
        minSpringVert = data.int32();
        System.out.println("Max Spring Vert Offset  -> 0x" + Bytes.toHex(data.offset));
        maxSpringVert = data.int32();
        System.out.println("Min Unaligned Spring Vert Offset  -> 0x" + Bytes.toHex(data.offset));
        minUnalignedSpringVert = data.int32();

        System.out.println("Springy Tri Indices Offset  -> 0x" + Bytes.toHex(data.offset));
        springyTriIndices = new short[data.int32()];
        for (int i = 0; i < springyTriIndices.length; ++i)
            springyTriIndices[i] = data.int16();

        System.out.println("Spring Tris Stripped Offset  -> 0x" + Bytes.toHex(data.offset));
        springTrisStripped = data.int32();

        System.out.println("Softbody Contained Bound Box Min Offset  -> 0x" + Bytes.toHex(data.offset));
        softbodyContainingBoundBoxMin = data.v4();
        System.out.println("Softbody Contained Bound Box Max Offset  -> 0x" + Bytes.toHex(data.offset));
        softbodyContainingBoundBoxMax = data.v4();

        System.out.println("Cull Bones Offset  -> 0x" + Bytes.toHex(data.offset));
        cullBones = CullBone.array(data);

        System.out.println("Region IDs To Hide Offset -> 0x" + Bytes.toHex(data.offset));
        regionIDsToHide = new long[data.int32()];
        if (regionIDsToHide.length != 0 && data.isEncoded()) data.int8();
        for (int i = 0; i < regionIDsToHide.length; ++i)
            regionIDsToHide[i] = data.uint32f();

        costumeCategoriesUsed = data.int32();
        hairMorphs = data.int32();
        bevelVertexCount = data.int32();
        implicitBevelSprings = data.bool();

        if (data.revision >= 0x016703ef)
            skeletonType = data.int8();
    }
    
    public MeshPrimitive[][] getSubmeshes() {
        HashMap<Integer, ArrayList<MeshPrimitive>> meshes = new HashMap<Integer, ArrayList<MeshPrimitive>>();
        for (MeshPrimitive primitive : this.meshPrimitives) {
            if (meshes.containsKey(primitive.region))
                meshes.get(primitive.region).add(primitive);
            else {
                ArrayList primitives = new ArrayList<MeshPrimitive>();
                primitives.add(primitive);
                meshes.put(primitive.region, primitives);
            }
        }
        
        MeshPrimitive[][] primitives = new MeshPrimitive[meshes.values().size()][];
        
        int i = 0;
        for (ArrayList<MeshPrimitive> primitiveList : meshes.values()) {
            primitives[i] = new MeshPrimitive[primitiveList.size()];
            primitiveList.toArray(primitives[i]);
            ++i;
        }
        
        return primitives;
    }
    
    public String getBoneName(long animHash) {
        for (int i = 0; i < this.bones.length; ++i)
            if (this.bones[i].animHash == animHash)
                return this.bones[i].name;
        return null;
    }
    
    public int getBoneIndex(Bone bone) {
        for (int i = 0; i < this.bones.length; ++i)
            if (this.bones[i].name.equals(bone.name))
                return i;
        return -1;
    }
    
    public Bone[] getBoneChildren(Bone parent) {
        ArrayList<Bone> bones = new ArrayList<Bone>(this.bones.length);
        int index = getBoneIndex(parent);
        if (index == -1) return null;
        for (Bone bone : this.bones) {
            if (bone == parent) continue;
            if (bone.parent == index)
                bones.add(bone);
        }
        Bone[] output = new Bone[bones.size()];
        bones.toArray(output);
        return output;
    }
    
    public short[] triangulate() { return triangulate(0, faces.length); }
    
    
    public short[] triangulate(MeshPrimitive primitive) {
        return this.triangulate(primitive.firstIndex, primitive.numIndices);
    }
    
    public short[] triangulate(int offset, int count) {
        ArrayList<Short> triangles = new ArrayList<Short>(count * 3);
        short[] faces = Arrays.copyOfRange(this.faces, offset, offset + count);
        
        if (this.primitiveType == 5)
            return faces;
        
        for (int i = -1, j = 1; i < faces.length; ++i, ++j) {
            if (i == -1 || (faces[i] == -1)) {
                if (i + 3 >= count) break;
                triangles.add(faces[i + 1]);
                triangles.add(faces[i + 2]);
                triangles.add(faces[i + 3]);
                i += 3;
                j = 0;
            } else {
                if ((j & 1) == 1) {
                    triangles.add(faces[i - 2]);
                    triangles.add(faces[i]);
                    triangles.add(faces[i - 1]);
                } else {
                    triangles.add(faces[i - 2]);
                    triangles.add(faces[i - 1]);
                    triangles.add(faces[i]);
                }
            }
        }
        
        short[] tris = new short[triangles.size()];
        for (int i = 0; i < tris.length; ++i)
            tris[i] = triangles.get(i);
        return tris;
        
    }

    public byte[] serialize(int revision) {
        Output output = new Output(0x5000000, revision);

        output.int32(vertices.length);
        output.int32(faces.length);
        output.int32(edges.length);
        output.int32(numTris);

        int streamCount = 1;
        if (weights != null) streamCount++;
        if (morphs != null) streamCount += morphs.length;
        
        
        output.int32(streamCount);
        output.int32(attributeCount);
        if (morphs != null)
            output.int32(morphs.length);
        else output.int32(0);

        for (int i = 0; i < 0x20; ++i)
            output.string(morphNames[i], 0x10);

        if (revision > 0x238) {
            output.float32arr(minUV);
            output.float32arr(maxUV);
            output.float32(areaScaleFactor);
        }

        output.int32(0);
        for (int i = 0; i < streamCount; ++i)
            output.int32((i + 1) * vertices.length * 0x10);
        output.int32(vertices.length * vertices.length * 0x10);

        for (int i = 0; i < vertices.length; ++i) {
            output.v3(vertices[i]);
            output.int32f(0xFF);
        }
        
        if (weights != null) {
            for (SkinWeight weight : weights)
                weight.serialize(output);
        }
        
        if (morphs != null) {
            for (int i = 0; i < morphs.length; ++i)
                morphs[i].serialize(output);
        }

        output.int32(uvCount * attributeCount * 0x8);

        for (int i = 0; i < uvCount; ++i)
            for (int j = 0; j < attributeCount; ++j)
                output.v2(attributes[i][j]);

        output.int32((faces.length + edges.length) * 2);

        for (int i = 0; i < faces.length; ++i)
            output.int16(faces[i]);

        for (int i = 0; i < edges.length; ++i)
            output.int16(edges[i]);

        if (revision >= 0x016b03ef) {
            output.int32(extraFaces.length * 0x2);
            for (int i = 0; i < extraFaces.length; ++i)
                output.int16(extraFaces[i]);
        }

        output.int32(meshPrimitives.length);
        for (int i = 0; i < meshPrimitives.length; ++i)
            meshPrimitives[i].serialize(output);

        output.int32(bones.length);
        for (int i = 0; i < bones.length; ++i)
            bones[i].serialize(output);

        output.int32(bones.length);
        for (int i = 0; i < bones.length; ++i)
            output.int16(bones[i].mirror);

        output.int32(bones.length);
        for (int i = 0; i < bones.length; ++i)
            output.int8(bones[i].mirrorType);

        output.int32(mirrorMorphs.length);
        for (int i = 0; i < mirrorMorphs.length; ++i)
            output.int16(mirrorMorphs[i]);

        output.int8(primitiveType);

        softbodyCluster.serialize(output);

        output.int32(softbodySprings.length);
        for (int i = 0; i < softbodySprings.length; ++i)
            softbodySprings[i].serialize(output);

        output.int32(softbodyEquivs.length);
        for (int i = 0; i < softbodyEquivs.length; ++i)
            softbodyEquivs[i].serialize(output);

        output.float32arr(mass);

        output.int32(implicitEllipsoids.length);
        for (int i = 0; i < implicitEllipsoids.length; ++i)
            implicitEllipsoids[i].serialize(output);

        output.int32(clusterImplicitEllipsoids.length);
        for (int i = 0; i < clusterImplicitEllipsoids.length; ++i)
            output.matrix(clusterImplicitEllipsoids[i]);


        output.int32(insideImplicitEllipsoids.length);
        for (int i = 0; i < insideImplicitEllipsoids.length; ++i)
            insideImplicitEllipsoids[i].serialize(output);

        output.int32(implicitPlanes.length);
        for (int i = 0; i < implicitPlanes.length; ++i)
            implicitPlanes[i].serialize(output);

        output.resource(softPhysSettings);

        output.int32(minSpringVert);
        output.int32(maxSpringVert);
        output.int32(minUnalignedSpringVert);

        output.int32(springyTriIndices.length);
        for (int i = 0; i < springyTriIndices.length; ++i)
            output.int16(springyTriIndices[i]);

        output.int32(springTrisStripped);
        output.v4(softbodyContainingBoundBoxMin);
        output.v4(softbodyContainingBoundBoxMax);

        output.int32(cullBones.length);
        for (int i = 0; i < cullBones.length; ++i)
            cullBones[i].serialize(output);

        output.int32(regionIDsToHide.length);
        if (regionIDsToHide.length != 0 && revision >= 0x272)
            output.int8(0x4);
        for (int i = 0; i < regionIDsToHide.length; ++i)
            output.uint32f(regionIDsToHide[i]);

        output.int32(costumeCategoriesUsed);
        output.int32(hairMorphs);
        output.int32(bevelVertexCount);
        output.bool(implicitBevelSprings);

        if (revision >= 0x016703ef)
            output.int8(skeletonType);

        output.shrinkToFit();

        return output.buffer;
    }

    private String getMirrorType(byte type) {
        switch (type) {
            case 0:
                return "Max (X.pos, X.rot)";
            case 1:
                return "BipRoot (Y.pos, Y.rot + prerot)";
            case 2:
                return "BipPelvis (Z.pos, Z.rot + prerot)";
            case 3:
                return "BipBone (Z.pos, Z.rot)";
            case 4:
                return "ParentWasBipBone (Z.pos, Z.rot + null prerot)";
            case 5:
                return "GrandparentWasBipBone (X.pos, X.rot + prerot)";
            case 6:
                return "Copy (no flip, straight copy from mirror bone)";
        }
        return "UNKNOWN";
    }
}
