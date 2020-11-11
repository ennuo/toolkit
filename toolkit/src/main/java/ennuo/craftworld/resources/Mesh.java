package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.resources.structs.Bone;
import ennuo.craftworld.resources.structs.MeshPrimitive;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Vector2f;
import ennuo.craftworld.memory.Vector3f;
import ennuo.craftworld.resources.structs.SoftbodyCluster;
import ennuo.craftworld.resources.structs.StringEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Mesh extends Resource {
    public short[] faces;
    public short[] extraFaces;
    public short[] edges;
    public Vector3f[][] streams;
    
    public int attributeCount;
    public int uvCount;
    
    public Vector2f[][] attributes;
    public String[] morphNames; 
    public MeshPrimitive[] meshPrimitives;
    public Bone[] bones;
    public short[] mirrorMorphs;
    byte primitiveType;
    SoftbodyCluster softbodyCluster;
    
    
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
        numEdgeIndices = int32(), numTris = int32();
        
        int streamCount = int32();
        attributeCount = int32();
        int morphCount = int32();
        
        System.out.println("Morph Count -> " + morphCount);
        
        morphNames = new String[0x20];
        for (int i = 0; i < 0x20; ++i)
            morphNames[i] = str(0x10);
        for (int i = 0; i < morphCount; ++i)
            System.out.println("Morph[" + i + "] -> " + morphNames[i]);
        
        Vector2f minUV, maxUV;
        if (revision > 0x238) {
            if (int32() == 2) minUV = v2();
            if (int32() == 2) maxUV = v2();
            float areaScaleFactor = float32();   
        }
        
        int[] streamOffsets = new int[streamCount + 1];
        for (int i = 0; i < streamCount + 1; ++i)
            streamOffsets[i] = int32();
        
        int[] streamSizes = new int[streamCount];
        for (int i = 1; i < streamCount + 1; ++i) 
            streamSizes[i - 1] = streamOffsets[i] - streamOffsets[i - 1];
        
        int streamSize = int32();
        System.out.println("Vertex Count -> " + numVerts);
        System.out.println("Stream Count -> " + streamCount);
        streams = new Vector3f[streamCount][];
        for (int i = 0; i < streamCount; ++i) {
            System.out.println("Stream[" + i + "] Offset -> 0x" + Bytes.toHex(offset));
            int vertCount = streamSizes[i] / 0x10;
            if (vertCount != 0) {
                Vector3f[] stream = new Vector3f[vertCount];
                for (int j = 0; j < vertCount; ++j) {
                    stream[j] = v3();
                    float32();
                }
                streams[i] = stream;
            } else streams[i] = new Vector3f[0];
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
        
        int faceSize = int32();
        System.out.println("Indices Offset -> 0x" + Bytes.toHex(offset));
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
        
        /*
        
        
        System.out.println("Mesh Primitives Offset -> 0x" + Bytes.toHex(offset));
        int primitiveCount = int32();
        System.out.println("Mesh Primitive Count -> " + primitiveCount);
        
        meshPrimitives = new MeshPrimitive[primitiveCount];
        for (int i = 0; i < primitiveCount; ++i) {
            MeshPrimitive mp = new MeshPrimitive(this);
            System.out.println("MeshPrimitive[" + i + "] (Vertices = " + mp.minVert + ":" + mp.maxVert + ")" + " (Indices = " + mp.firstIndex + ":" + (mp.firstIndex + mp.numIndices) + ")");
            meshPrimitives[i] = mp;
        }
        
        System.out.println("Bone Offset -> 0x" + Bytes.toHex(offset));
        int boneCount = int32();
        System.out.println("Bone Count -> " + boneCount);

                
        bones = new Bone[boneCount];
        for (int i = 0; i < boneCount; ++i)
            bones[i] = new Bone(this);

        int mirrorCount = int32();
        System.out.println("Mirror Bones Offset -> 0x" + Bytes.toHex(offset));
        for (int i = 0; i < mirrorCount; ++i)
            bones[i].mirror = int16();
        int mirrorTypeCount = int32();
        System.out.println("Mirror Types Offset -> 0x" + Bytes.toHex(offset));
        for (int i = 0; i < mirrorTypeCount; ++i)
            bones[i].mirrorType = int8();
        
        for (int i = 1; i < boneCount; ++i) {
            Bone bone = bones[i];
            System.out.println(
                String.format("Bone %d [%s] (%s) has Parent=%d (%s) and Mirror=%d (%s) with MirrorType=%s", 
                i, Bytes.toHex(bone.animHash), bone.name, bone.parent, bones[bone.parent].name, bone.mirror, bones[bone.mirror].name, getMirrorType(bone.mirrorType))
            );   
        }
        
        
        int mirrorMorphCount = int32();
        System.out.println("Mirror Morphs Offset -> 0x " + Bytes.toHex(offset));
        mirrorMorphs = new short[mirrorMorphCount];
        for (int i = 0; i < mirrorMorphCount; ++i)
            mirrorMorphs[i] = int16();
        
        primitiveType = int8();
        
        */

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
