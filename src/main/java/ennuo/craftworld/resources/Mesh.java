package ennuo.craftworld.resources;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.structs.mesh.*;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.Compressor;
import java.util.ArrayList;
import java.util.HashMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Mesh implements Serializable {
    public String name;
    
    public int numVerts;
    public int numIndices;
    public int numEdgeIndices;
    public int numTris;
    public int streamCount;
    public int attributeCount;
    public int morphCount;
    
    public String[] morphNames;
    
    public float[] minUV = new float[] { 0.0f, 0.0f, };
    public float[] maxUV = new float[] { 1.0f, 1.0f };
    public float areaScaleFactor = 500;
    
    public byte[][] streams;
    public byte[] attributes;
    public byte[] indices;
    public byte[] triangles;
    
    public MeshPrimitive[] meshPrimitives;
    public Bone[] bones;
    
    public short[] mirror;
    public byte[] mirrorType;
    public short[] mirrorMorphs;
    
    // NOTE(Abz): Primitive type 5 is for triangle lists, although the game
    // generally tends to use triangle strips. As a reference for possible
    // values, see: 
    // https://www.khronos.org/opengl/wiki/Primitive
    
    public byte primitiveType = 5;
    
    public SoftbodyCluster softbodyCluster;
    public SoftbodySpring[] softbodySprings;
    public SoftbodyVertEquivalence[] softbodyEquivs;
    public float[] mass;
    public ImplicitEllipsoid[] implicitEllipsoids;
    public Matrix4f[] clusterImplicitEllipsoids;
    public ImplicitEllipsoid[] insideImplicitEllipsoids;
    public ImplicitPlane[] implicitPlanes;
    public ResourceDescriptor softPhysSettings;
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
    
    public Mesh(){}
    public Mesh(String name, byte[] data) {
        this.name = name;
        Serializer serializer = new Serializer(new Resource(data).handle);
        this.serialize(serializer, this);
    }

    public Mesh serialize(Serializer serializer, Serializable structure) {
        Mesh mesh = (structure == null) ? new Mesh() : (Mesh) structure;
        
        mesh.numVerts = serializer.i32(mesh.numVerts);
        mesh.numIndices = serializer.i32(mesh.numIndices);
        mesh.numEdgeIndices = serializer.i32(mesh.numEdgeIndices);
        mesh.numTris = serializer.i32(mesh.numTris);
        mesh.streamCount = serializer.i32(mesh.streamCount);
        mesh.attributeCount = serializer.i32(mesh.attributeCount);
        mesh.morphCount = serializer.i32(mesh.morphCount);
        
        if (!serializer.isWriting) 
            mesh.morphNames = new String[0x20];
        for (int i = 0; i < 0x20; ++i)
            mesh.morphNames[i] = serializer.str(mesh.morphNames[i], 0x10);
        
        if (serializer.revision >= 0x239) {
            mesh.minUV = serializer.f32a(mesh.minUV);
            mesh.maxUV = serializer.f32a(mesh.maxUV);
            mesh.areaScaleFactor = serializer.f32(mesh.areaScaleFactor);
        }
        
        if (serializer.isWriting) {
            int offset = 0;
            serializer.output.i32(offset);
            for (int i = 0; i < mesh.streams.length; ++i) {
                offset += mesh.streams[i].length;
                serializer.output.i32(offset);
            }
            serializer.output.i32(offset);
            for (byte[] stream : mesh.streams)
                serializer.output.bytes(stream);
        } else {
            // NOTE(Abz): We're skipping source stream offsets. 
            for (int i = 0; i < mesh.streamCount + 1; ++i)
                serializer.input.i32();
            serializer.input.i32();
            
            mesh.streams = new byte[mesh.streamCount][];
            for (int i = 0; i < mesh.streamCount; ++i)
                mesh.streams[i] = serializer.input.bytes(mesh.numVerts * 0x10);
        }
        
        mesh.attributes = serializer.i8a(mesh.attributes);
        mesh.indices = serializer.i8a(mesh.indices);
        if (serializer.revision >= 0x016b03f0)
            mesh.triangles = serializer.i8a(mesh.triangles);
        
        mesh.meshPrimitives = serializer.array(mesh.meshPrimitives, MeshPrimitive.class);
        mesh.bones = serializer.array(mesh.bones, Bone.class);
        
        mesh.mirror = serializer.i16a(mesh.mirror);
        mesh.mirrorType = serializer.i8a(mesh.mirrorType);
        mesh.mirrorMorphs = serializer.i16a(mesh.mirrorMorphs);
        
        mesh.primitiveType = serializer.i8(mesh.primitiveType);
        
        mesh.softbodyCluster = serializer.struct(mesh.softbodyCluster, SoftbodyCluster.class);
        mesh.softbodySprings = serializer.array(mesh.softbodySprings, SoftbodySpring.class);
        mesh.softbodyEquivs = serializer.array(mesh.softbodyEquivs, SoftbodyVertEquivalence.class);
        mesh.mass = serializer.f32a(mesh.mass);
        mesh.implicitEllipsoids = serializer.array(mesh.implicitEllipsoids, ImplicitEllipsoid.class);
        
        if (!serializer.isWriting) mesh.clusterImplicitEllipsoids = new Matrix4f[serializer.input.i32()];
        else serializer.output.i32(mesh.clusterImplicitEllipsoids.length);
        for (int i = 0; i < mesh.clusterImplicitEllipsoids.length; ++i)
            mesh.clusterImplicitEllipsoids[i] = serializer.matrix(mesh.clusterImplicitEllipsoids[i]);
        
        mesh.insideImplicitEllipsoids = serializer.array(mesh.insideImplicitEllipsoids, ImplicitEllipsoid.class);
        mesh.implicitPlanes = serializer.array(mesh.implicitPlanes, ImplicitPlane.class);
        mesh.softPhysSettings = serializer.resource(mesh.softPhysSettings, ResourceType.SETTINGS_SOFT_PHYS);
        mesh.minSpringVert = serializer.i32(mesh.minSpringVert);
        mesh.maxSpringVert = serializer.i32(mesh.maxSpringVert);
        mesh.minUnalignedSpringVert = serializer.i32(mesh.minUnalignedSpringVert);
        mesh.springyTriIndices = serializer.i16a(mesh.springyTriIndices);
        mesh.springTrisStripped = serializer.i32(mesh.springTrisStripped);
        mesh.softbodyContainingBoundBoxMin = serializer.v4(mesh.softbodyContainingBoundBoxMin);
        mesh.softbodyContainingBoundBoxMax = serializer.v4(mesh.softbodyContainingBoundBoxMax);
        
        mesh.cullBones = serializer.array(mesh.cullBones, CullBone.class);
        
        // NOTE(Abz): I have no idea whatsoever why region IDs are encoded like this,
        // is there some benefit to it?
        
        if (serializer.revision < 0x272)
            mesh.regionIDsToHide = serializer.u32a(mesh.regionIDsToHide);
        else if (serializer.isWriting) {
            if (mesh.regionIDsToHide != null && mesh.regionIDsToHide.length != 0) {
                serializer.output.i32(mesh.regionIDsToHide.length);
                serializer.output.i32(4);
                byte[][] regionIDs = new byte[mesh.regionIDsToHide.length][];
                for (int i = 0; i < regionIDs.length; ++i) {
                    long regionID = mesh.regionIDsToHide[i];
                    regionIDs[i] = Bytes.toBytesLE(regionID);
                }
                for (int i = 0; i < 4; ++i)
                    for (int j = 0; j < mesh.regionIDsToHide.length; ++j)
                        serializer.output.i8(regionIDs[j][i]);                
            }
            else serializer.output.i32(0);
        } else {
            mesh.regionIDsToHide = new long[serializer.input.i32()];
            if (mesh.regionIDsToHide.length != 0) {
                byte[][] regionIDs = new byte[mesh.regionIDsToHide.length][];
                int packSize = serializer.input.i32();
                for (int i = 0; i < regionIDs.length; ++i)
                    regionIDs[i] = new byte[packSize];
                for (int i = 0; i < packSize; ++i)
                    for (int j = 0; j < regionIDs.length; ++i)
                        regionIDs[j][i] = serializer.input.i8();
                for (int i = 0; i < regionIDs.length; ++i)
                    mesh.regionIDsToHide[i] = Bytes.toIntegerLE(regionIDs[i]);
            }
        }
        
        mesh.costumeCategoriesUsed = serializer.i32(mesh.costumeCategoriesUsed);
        mesh.hairMorphs = serializer.i32(mesh.hairMorphs);
        mesh.bevelVertexCount = serializer.i32(mesh.bevelVertexCount);
        mesh.implicitBevelSprings = serializer.bool(mesh.implicitBevelSprings);
        
        if (serializer.revision >= 0x015f03ef)
            mesh.skeletonType = serializer.i8(mesh.skeletonType);
        
        return mesh;
    }
    
    public byte[] build(int revision) {
        int dataSize = 1024 * 500;
        for (byte[] stream : this.streams)
            dataSize += stream.length;
        if (this.attributes != null) dataSize += this.attributes.length;
        if (this.indices != null) dataSize += this.indices.length;
        if (this.triangles != null) dataSize += this.triangles.length;
        Serializer serializer = new Serializer(dataSize, revision);
        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.MESH);    
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
    
    public Vector3f[] getVertices(int start, int count) {
        Data data = new Data(this.streams[0]);
        data.offset = 0x10 * start;
        Vector3f[] vertices = new Vector3f[count];
        for (int i = 0; i < count; ++i) {
            vertices[i] = data.v3();
            data.f32(); // NOTE(Abz): We're skipping that 0x000000FF bit, no idea what it is, seems irrelevant!
        }
        return vertices;
    }
    
    public Vector3f[] getVertices(MeshPrimitive primitive) {
        return this.getVertices(primitive.minVert, primitive.maxVert - primitive.minVert);
    }
    
    public Vector3f[] getVertices() { return this.getVertices(0, this.numVerts); }
    
    public Vector2f[] getUVs(int start, int count, int channel) {
        if (channel < 0 || (channel + 1 > this.attributeCount)) return null;
        Data data = new Data(this.attributes);
        data.offset = (0x8 * this.attributeCount) * start;
        Vector2f[] UVs = new Vector2f[count];
        for (int i = 0; i < count; ++i) {
            data.offset = start + (0x8 * this.attributeCount * i) + (0x8 * channel);
            UVs[i] = data.v2();
        }
        return UVs;
    }
    
    public Vector2f[] getUVs(MeshPrimitive primitive, int channel) {
        return this.getUVs(primitive.minVert, primitive.maxVert - primitive.minVert, channel);
    }
    
    public Vector2f[] getUVs(int channel) { return this.getUVs(0, this.numVerts, channel); }
    
    public Vector3f[] getNormals(int start, int count) {
        Data data = new Data(this.streams[1]);
        Vector3f[] normals = new Vector3f[count];
        for (int i = 0; i < count; ++i) {
            data.offset = (0x10 * start) + (0x10 * i) + 0x4;
            normals[i] = Bytes.decodeI24(data.i24());
        }
        return normals;
    }
    
    public Vector3f[] getNormals(MeshPrimitive primitive) {
        return this.getNormals(primitive.minVert, primitive.maxVert - primitive.minVert);
    }
    
    public Vector3f[] getNormals() { return this.getNormals(0, this.numVerts); }
    
    public byte[][] getJoints(int start, int count) {
       Data data = new Data(this.streams[1]);
       data.offset = 0x10 * start;
       byte[][] joints = new byte[count][];
       for (int i = 0; i < count; ++i) {
           byte[] buffer = data.bytes(0x10);
           joints[i] = new byte[] { 
               buffer[3], buffer[7], buffer[0xB], buffer[0xF] 
           };
       }
       return joints;
    }
    
    public byte[][] getJoints(MeshPrimitive primitive) {
        return this.getJoints(primitive.minVert, primitive.maxVert - primitive.minVert);
    }
    
    public byte[][] getJoints() { return this.getJoints(0, this.numVerts); }
    
    public Vector4f[] getWeights(int start, int count) {
        Data data = new Data(this.streams[1]);
        data.offset = 0x10 * start;
        Vector4f[] weights = new Vector4f[count];
        for (int i = 0; i < count; ++i) {
            byte[] buffer = data.bytes(0x10);
            float[] rawWeight = new float[] {
                (float)((int)buffer[2] & 0xFF), 
                (float)((int)buffer[1] & 0xFF), 
                (float)((int)buffer[0] & 0xFF), 
                0
            };
            
            if (rawWeight[0] != 0xFF) {
                rawWeight[3] = 0xFE - rawWeight[2] - rawWeight[1] - rawWeight[0];
                weights[i] = new Vector4f(
                        rawWeight[0] / 0xFE,
                        rawWeight[1] / 0xFE,
                        rawWeight[2] / 0xFE,
                        rawWeight[3] / 0xFE
                );
            } else weights[i] = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);
        }
        return weights;
    }
    
    public Vector4f[] getWeights(MeshPrimitive primitive) {
        return this.getWeights(primitive.minVert, primitive.maxVert - primitive.minVert);
    }
    
    public Vector4f[] getWeights() {
        return this.getWeights(0, this.numVerts);
    }
    
    public Morph[] getMorphs() {
        Morph[] morphs = new Morph[this.morphCount];
        for (int i = 0; i < this.morphCount; ++i) {
            Data data = new Data(this.streams[2 + i]);
            Morph morph = new Morph();
            morph.vertices = new Vector3f[this.numVerts];
            morph.normals = new Vector3f[this.numVerts];
            for (int j = 0; j < this.numVerts; ++j) {
                morph.vertices[j] = data.v3();
                morph.normals[j] = Bytes.decodeI32(data.u32f());
            }
            morphs[i] = morph;
        }
        return morphs;
    }
    
    public short[] getIndices(int start, int count) {
        short[] faces = new short[count];
        Data data = new Data(this.indices);
        data.offset = 0x2 * start;
        for (int i = 0; i < count; ++i)
            faces[i] = data.i16();
        if (this.primitiveType == 5) return faces;
        ArrayList<Short> triangles = new ArrayList<Short>(count * 3);
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
    
    public short[] getIndices(MeshPrimitive primitive) {
        return this.getIndices(primitive.firstIndex, primitive.numIndices);
    }
    
    public short[] getIndices() { return this.getIndices(0, this.numIndices); }
}
