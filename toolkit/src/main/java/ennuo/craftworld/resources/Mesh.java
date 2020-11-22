package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.resources.structs.mesh.Bone;
import ennuo.craftworld.resources.structs.mesh.MeshPrimitive;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.memory.Vector2f;
import ennuo.craftworld.memory.Vector3f;
import ennuo.craftworld.memory.Vector4f;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.mesh.CullBone;
import ennuo.craftworld.resources.structs.mesh.ImplicitEllipsoid;
import ennuo.craftworld.resources.structs.mesh.ImplicitPlane;
import ennuo.craftworld.resources.structs.mesh.SoftbodyCluster;
import ennuo.craftworld.resources.structs.mesh.SoftbodySpring;
import ennuo.craftworld.resources.structs.mesh.SoftbodyVertEquivalence;

public class Mesh extends Resource {
    
    
    int numTris = 0;
    int morphCount = 0;
    
    float[] minUV = new float[] { 0.0f, 0.0f };
    float[] maxUV = new float[] { 0.0f, 0.0f };
    
    float areaScaleFactor = 100;
    
    public short[] faces;
    public short[] extraFaces;
    public short[] edges;
    public Vector4f[][] streams;
    
    public int attributeCount;
    public int uvCount;
    
    public Vector2f[][] attributes;
    public String[] morphNames; 
    public MeshPrimitive[] meshPrimitives;
    public Bone[] bones;
    public short[] mirrorMorphs;
    byte primitiveType;
    SoftbodyCluster softbodyCluster;
    SoftbodySpring[] softbodySprings;
    SoftbodyVertEquivalence[] softbodyEquivs;
    float[] mass;
    ImplicitEllipsoid[] implicitEllipsoids;
    float[][] clusterImplicitEllipsoids;
    ImplicitEllipsoid[] insideImplicitEllipsoids;
    ImplicitPlane[] implicitPlanes;
    ResourcePtr softPhysSettings;
    int minSpringVert, maxSpringVert;
    int minUnalignedSpringVert;
    short[] springyTriIndices;
    int springTrisStripped;
    Vector4f softbodyContainingBoundBoxMin;
    Vector4f softbodyContainingBoundBoxMax;
    CullBone[] cullBones;
    long[] regionIDsToHide;
    int costumeCategoriesUsed;
    int hairMorphs;
    int bevelVertexCount;
    boolean implicitBevelSprings;
    byte skeletonType;
    
    
    public Mesh(byte[] data) {
        super(data);
        if (this.data == null) {
            System.out.println("No data provided to Mesh constructor");
            return;
        }
        process();
    }
    
    private void process() {
        decompress(true);
        System.out.println("Parsing Mesh...");
        int numVerts = int32(), numIndices = int32(),
        numEdgeIndices = int32();
        numTris = int32();
        
        int streamCount = int32();
        attributeCount = int32();
        morphCount = int32();
        
        System.out.println("Morph Count -> " + morphCount);
        
        morphNames = new String[0x20];
        for (int i = 0; i < 0x20; ++i)
            morphNames[i] = str(0x10);
        for (int i = 0; i < morphCount; ++i)
            System.out.println("Morph[" + i + "] -> " + morphNames[i]);
        
        if (revision > 0x238) {
            minUV = float32arr();
            maxUV = float32arr();
            areaScaleFactor = float32();   
        }
        
        int[] streamOffsets = new int[streamCount + 1];
        for (int i = 0; i < streamCount + 1; ++i)
            streamOffsets[i] = int32();
        
        int streamSize = int32();
        System.out.println("Vertex Count -> " + numVerts);
        System.out.println("Stream Count -> " + streamCount);
        streams = new Vector4f[streamCount][];
        for (int i = 0; i < streamCount; ++i) {
            System.out.println("Stream[" + i + "] Offset -> 0x" + Bytes.toHex(offset));
            Vector4f[] stream = new Vector4f[numVerts];
            for (int j = 0; j < numVerts; ++j) 
                stream[j] = v4();
            streams[i] = stream;
        }
        
        System.out.println("UV Offset -> 0x" + Bytes.toHex(offset));
        
        int uvSize = int32();
        uvCount = uvSize / (0x8 * attributeCount);
        
        System.out.println("UV Count -> " + uvCount);
        attributes = new Vector2f[uvCount][];
        for (int i = 0; i < uvCount; ++i) {
            attributes[i] = new Vector2f[attributeCount];
            for (int j = 0; j < attributeCount; ++j)
                attributes[i][j] = v2();
        }
        
        System.out.println("Indices Offset -> 0x" + Bytes.toHex(offset));
        int faceSize = int32();
        System.out.println("Index Count -> " + numIndices);
        faces = new short[numIndices];
        for (int i = 0; i < numIndices; ++i)
            faces[i] = int16();
        System.out.println("Edge Indices Offset -> 0x" + Bytes.toHex(offset));
        System.out.println("Edge Index Count -> " + numEdgeIndices);
        edges = new short[numEdgeIndices];
        for (int i = 0; i < numEdgeIndices; ++i)
            edges[i] = int16();
        
        if (revision >= 0x016b03ef) {
            int extraFaceCount = int32() / 2;
            System.out.println(extraFaceCount);
            extraFaces = new short[extraFaceCount];
            for (int i = 0; i < extraFaceCount; ++i)
                extraFaces[i] = int16();
        }
        
        System.out.println("Mesh Primitives Offset -> 0x" + Bytes.toHex(offset));
        meshPrimitives = MeshPrimitive.array(this);
        
        System.out.println("Bones Offset -> 0x" + Bytes.toHex(offset));
        bones = Bone.array(this);

        int mirrorCount = int32();
        for (int i = 0; i < mirrorCount; ++i)
            bones[i].mirror = int16();
        int mirrorTypeCount = int32();
        for (int i = 0; i < mirrorTypeCount; ++i)
            bones[i].mirrorType = int8();
        
        /*
        for (int i = 1; i < bones.length; ++i) {
            Bone bone = bones[i];
            System.out.println(
                String.format("Bone %d [%s] (%s) has Parent=%d (%s) and Mirror=%d (%s) with MirrorType=%s", 
                i, Bytes.toHex(bone.animHash), bone.name, bone.parent, bones[bone.parent].name, bone.mirror, bones[bone.mirror].name, getMirrorType(bone.mirrorType))
            );   
        }
        */
        
        System.out.println("Mirror Morph Offset  -> 0x" + Bytes.toHex(offset));
        int mirrorMorphCount = int32();
        mirrorMorphs = new short[mirrorMorphCount];
        for (int i = 0; i < mirrorMorphCount; ++i)
            mirrorMorphs[i] = int16();
        
        System.out.println("Primitive Type Offset  -> 0x" + Bytes.toHex(offset));
        primitiveType = int8();
        System.out.println("Softbody Cluster Offset  -> 0x" + Bytes.toHex(offset));
        softbodyCluster = new SoftbodyCluster(this);
        System.out.println("Softbody Springs Offset  -> 0x" + Bytes.toHex(offset));
        softbodySprings = SoftbodySpring.array(this);
        System.out.println("Softbody Vert Equivalences Offset  -> 0x" + Bytes.toHex(offset));
        softbodyEquivs = SoftbodyVertEquivalence.array(this);
        System.out.println("Mass Offset  -> 0x" + Bytes.toHex(offset));
        mass = float32arr();
        System.out.println("Implicit Ellipsoids Offset  -> 0x" + Bytes.toHex(offset));
        implicitEllipsoids = ImplicitEllipsoid.array(this);
        System.out.println("Cluster Implicit Ellipsoids Offset  -> 0x" + Bytes.toHex(offset));
        clusterImplicitEllipsoids = new float[int32()][];
        for (int i = 0; i < clusterImplicitEllipsoids.length; ++i)
            clusterImplicitEllipsoids[i] = matrix();
        System.out.println("Inside Implicit Ellipsoids Offset  -> 0x" + Bytes.toHex(offset));
        insideImplicitEllipsoids = ImplicitEllipsoid.array(this);
        System.out.println("Implicit Planes Offset  -> 0x" + Bytes.toHex(offset));
        implicitPlanes = ImplicitPlane.array(this);
        System.out.println("Soft Phys Settings Offset  -> 0x" + Bytes.toHex(offset));
        softPhysSettings = resource(RType.SETTINGS_SOFT_PHYS);
        System.out.println("Min Spring Vert Offset  -> 0x" + Bytes.toHex(offset));
        minSpringVert = int32();
        System.out.println("Max Spring Vert Offset  -> 0x" + Bytes.toHex(offset));
        maxSpringVert = int32();
        System.out.println("Min Unaligned Spring Vert Offset  -> 0x" + Bytes.toHex(offset));
        minUnalignedSpringVert = int32();
        
        System.out.println("Springy Tri Indices Offset  -> 0x" + Bytes.toHex(offset));
        springyTriIndices = new short[int32()];
        for (int i = 0; i < springyTriIndices.length; ++i)
            springyTriIndices[i] = int16();
        
        System.out.println("Spring Tris Stripped Offset  -> 0x" + Bytes.toHex(offset));
        springTrisStripped = int32();
        
        System.out.println("Softbody Contained Bound Box Min Offset  -> 0x" + Bytes.toHex(offset));
        softbodyContainingBoundBoxMin = v4();
        System.out.println("Softbody Contained Bound Box Max Offset  -> 0x" + Bytes.toHex(offset));
        softbodyContainingBoundBoxMax = v4();
        
        System.out.println("Cull Bones Offset  -> 0x" + Bytes.toHex(offset));
        cullBones = CullBone.array(this);
        
        System.out.println("Region IDs To Hide Offset -> 0x" + Bytes.toHex(offset));
        regionIDsToHide = new long[int32()];
        if (regionIDsToHide.length != 0 && revision >= 0x272) int8();
        for (int i = 0; i < regionIDsToHide.length; ++i)
            regionIDsToHide[i] = uint32f();
        
        costumeCategoriesUsed = int32();
        hairMorphs = int32();
        bevelVertexCount = int32();
        implicitBevelSprings = bool();
        
        if (revision >= 0x016703ef)
            skeletonType = int8();
    }
    
    public byte[] serialize(int revision) {
        Output output = new Output(0x5000000, revision);
        
        output.int32(streams[0].length);
        output.int32(faces.length);
        output.int32(edges.length);
        output.int32(numTris);
       
        output.int32(streams.length);
        output.int32(attributeCount);
        output.int32(morphCount);
        
        for (int i = 0; i < 0x20; ++i)
            output.string(morphNames[i], 0x10);
        
        if (revision > 0x238) {
            output.float32arr(minUV);
            output.float32arr(maxUV);
            output.float32(areaScaleFactor);
        }
        
        output.int32(0);
        for (int i = 0; i < streams.length; ++i)
            output.int32((i + 1) * streams[0].length * 0x10);
        output.int32(streams[0].length * streams.length * 0x10);
        
        for (int i = 0; i < streams.length; ++i)
            for (int j = 0; j < streams[i].length; ++j)
                output.v4(streams[i][j]);
        
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
    
    public void export (String path) { export(path, 0); }
    public void export(String path, int channel) {
        StringBuilder builder = new StringBuilder((streams[0].length * 41) + (uvCount * 42) + (faces.length * 40));
        for (int j = 0; j < streams[0].length; ++j)
             builder.append("v " + streams[0][j].x + " " + streams[0][j].y + " " + streams[0][j].z + '\n');
        for (int i = 0; i < uvCount; ++i)
            builder.append("vt " + attributes[i][channel].x + " " + (1.0f - attributes[i][channel].y) + '\n');
        for (int i = -1, j = 1; i < faces.length; ++i, ++j) {
            if (i == -1 || faces[i] == -1) {
                String str = "f ";
                str += (faces[i + 1] + 1) + "/" + (faces[i + 1] + 1) + " ";
                str += (faces[i + 2] + 1) + "/" + (faces[i + 2] + 1) + " ";
                str += (faces[i + 3] + 1) + "/" + (faces[i + 3] + 1) + '\n';
                
                builder.append(str);
                i += 3; j = 0;
            } else {
                if ((j & 1) == 1) {
                    String str = "f ";
                    str += (faces[i - 2] + 1) + "/" + (faces[i - 2] + 1) + " ";
                    str += (faces[i] + 1) + "/" + (faces[i] + 1) + " ";
                    str += (faces[i - 1] + 1) + "/" + (faces[i - 1] + 1) + '\n';
                    builder.append(str);
                }
                else {
                    String str = "f ";
                    str += (faces[i - 2] + 1) + "/" + (faces[i - 2] + 1) + " ";
                    str += (faces[i - 1] + 1) + "/" + (faces[i - 1] + 1) + " ";
                    str += (faces[i] + 1) + "/" + (faces[i] + 1) + '\n';
                    builder.append(str);
                }
            }
        }
        
        FileIO.write(builder.toString().getBytes(), path);
        
        
    }

    private String getMirrorType(byte type) {
        switch (type) {
            case 0: return "Max (X.pos, X.rot)";
            case 1: return "BipRoot (Y.pos, Y.rot + prerot)";
            case 2: return "BipPelvis (Z.pos, Z.rot + prerot)";
            case 3: return "BipBone (Z.pos, Z.rot)";
            case 4: return "ParentWasBipBone (Z.pos, Z.rot + null prerot)";
            case 5: return "GrandparentWasBipBone (X.pos, X.rot + prerot)";
            case 6: return "Copy (no flip, straight copy from mirror bone)";
        }
        return "UNKNOWN";
    }
}
