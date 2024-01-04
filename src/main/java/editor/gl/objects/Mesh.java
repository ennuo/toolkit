package editor.gl.objects;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.BufferUtils;

import cwlib.enums.FlipType;
import cwlib.enums.HairMorph;
import cwlib.enums.PrimitiveType;
import cwlib.resources.RMesh;
import cwlib.resources.RStaticMesh;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Morph;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PShape;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;
import editor.gl.Extruder;
import editor.gl.MeshPrimitive;
import editor.gl.RenderSystem;
import editor.gl.objects.Texture;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    public static HashMap<ResourceDescriptor, Mesh> MESHES = new HashMap<>();
    public static ArrayList<Mesh> PROCEDURAL_MESHES = new ArrayList<>();

    private int VAO, VBO, EBO;
    private int numIndices;
    private int type;

    private Texture morphLUT;

    private MeshPrimitive[] primitives;
    private Bone[] bones;
    private FlipType[] types;
    private HashSet<Integer> regionIDsToHide = new HashSet<>();
    private int costumeCategoriesUsed;
    private HairMorph hairMorphs = HairMorph.HAT;

    private ResourceDescriptor descriptor;

    private Mesh() {};

    public static Mesh getFromCache(ResourceDescriptor descriptor) {
        if (descriptor == null) return null;
        if (MESHES.containsKey(descriptor))
            return MESHES.get(descriptor);
        return null;
    }
    
    public static Mesh getProceduralMesh(PGeneratedMesh parameters, PShape shape) {
        Mesh glMesh = new Mesh();

        glMesh.type = GL_TRIANGLES;
        glMesh.bones = new Bone[] { new Bone("Shape01") };

        // float cutoff = 45.0f;
        // float scale = 1.0f;

        // Vector2f[] bevelPoints = new Vector2f[] {
        //     new Vector2f(-0.5166667f, -1.0f),
        //     new Vector2f(-0.16666667f, -0.8358108f),
        //     new Vector2f(0.0f, -0.49324325f),
        //     new Vector2f(0.0f, 0.5f),
        //     new Vector2f(-0.16666666f, 0.8472973f),
        //     new Vector2f(-0.5f, 1.0f)
        // };

        // for (Vector2f vertex : bevelPoints) {
        //     vertex.x *= 10.0f;
        //     vertex.y *= 10.0f;
        // }


        // if (parameters.bevel != null) {
        //     byte[] data = ResourceSystem.extract(parameters.bevel);
        //     if (data != null) {
        //         RBevel bevel = new Resource(data).loadResource(RBevel.class);
        //         cutoff = bevel.autoSmoothCutoffAngle;
        //         bevelPoints = new Vector2f[bevel.vertices.size()];
        //         for (int i = 0; i < bevelPoints.length; ++i) {
        //             bevelPoints[i] = new Vector2f(
        //                 bevel.vertices.get(i).y * bevel.fixedBevelSize,
        //                 bevel.vertices.get(i).z * bevel.fixedBevelSize
        //             );
        //         }
        //     }
        // }

        Vector2f[] bevelPoints = new Vector2f[] {
            new Vector2f(0.2f, 0.0f),
            new Vector2f(0.1f, 0),
            new Vector2f(0.02928932188f, 0.02928932188f),
            new Vector2f(0, 0.1f),
            new Vector2f(0, 1)
        };

        // for (Vector2f vertex : bevelPoints) {
        //     vertex.x *= 10.0f;
        //     vertex.y *= 10.0f;
        // }


        Vector3f[] vertices = null; Vector2f[] uvs = null; int[] triangles = null;
        Vector3f[] normals = null;

        ArrayList<Vector3f> vertexList = new ArrayList<>();
        ArrayList<Vector2f> uvList = new ArrayList<>();
        ArrayList<Integer> triList = new ArrayList<>();
        ArrayList<Vector3f> normalList = new ArrayList<>();

        Extruder.generateTreviri(
            shape.polygon, bevelPoints, shape.thickness, 
            vertexList, uvList, triList, normalList
        );

        triangles = triList.stream().mapToInt(Integer::valueOf).toArray();
        vertices = vertexList.toArray(Vector3f[]::new);
        uvs = uvList.toArray(Vector2f[]::new);
        normals = normalList.toArray(Vector3f[]::new);

        int attributeCount = 0x6;
        int elementCount = 0x4 * attributeCount;
        int stride = 0x4 * elementCount;
        int numVerts = vertices.length;

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(elementCount * numVerts);
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(triangles.length);
        indexBuffer.put(triangles);

        for (int j = 0; j < vertices.length; ++j) {
            vertexBuffer.put(vertices[j].x);
            vertexBuffer.put(vertices[j].y);
            vertexBuffer.put(vertices[j].z);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(normals[j].x);
            vertexBuffer.put(normals[j].y);
            vertexBuffer.put(normals[j].z);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(uvs[j].x);
            vertexBuffer.put(uvs[j].y);
            vertexBuffer.put(uvs[j].x);
            vertexBuffer.put(uvs[j].y);

            vertexBuffer.put((float) (0));
            vertexBuffer.put((float) (0));
            vertexBuffer.put((float) (0));
            vertexBuffer.put((float) (0));

            vertexBuffer.put(1.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
        }

        vertexBuffer.flip();
        indexBuffer.flip();

        glMesh.VAO = glGenVertexArrays();
        glMesh.VBO = glGenBuffers();
        glMesh.EBO = glGenBuffers();

        glBindVertexArray(glMesh.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, glMesh.VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glMesh.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < attributeCount; ++i) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 0x4, GL_FLOAT, false, stride, 0x10 * i);
        }

        glBindVertexArray(0);

        glMesh.numIndices = triangles.length;
        glMesh.primitives = new MeshPrimitive[] { new MeshPrimitive(parameters.gfxMaterial, PrimitiveType.GL_TRIANGLES, triangles.length) };

        PROCEDURAL_MESHES.add(glMesh);
        
        return glMesh;
    }


    public static Mesh getSkinnedMesh(ResourceDescriptor descriptor) {
        Mesh glMesh = Mesh.getFromCache(descriptor);
        if (glMesh != null) return glMesh;

        byte[] data = RenderSystem.getSceneGraph().getResourceData(descriptor);
        if (data == null) return null;

        glMesh = new Mesh();
        System.out.println("Linking Mesh: " + descriptor);
        glMesh.descriptor = descriptor;
        RMesh mesh = new Resource(data).loadResource(RMesh.class);

        glMesh.type = (mesh.getPrimitiveType().getValue() & 0xff) - 1;
        glMesh.bones = mesh.getBones();

        int attributeCount = 0x6;
        int elementCount = 0x4 * attributeCount;
        int stride = 0x4 * elementCount;

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(elementCount * mesh.getNumVerts());
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(mesh.getNumIndices());

        Vector3f[] vertices = mesh.getVertices();
        Vector3f[] normals = mesh.getNormals();
        Vector4f[] tangents = mesh.getTangents();

        Vector2f[] UV0 = mesh.getUVs(0);
        Vector2f[] UV1 = UV0;
        if (mesh.getAttributeCount() > 1)
            UV1 = mesh.getUVs(1);
        
        byte[][] bones = mesh.getJoints();
        Vector4f[] weights = mesh.getWeights();

        for (int j = 0; j < mesh.getNumVerts(); ++j) {
            vertexBuffer.put(vertices[j].x);
            vertexBuffer.put(vertices[j].y);
            vertexBuffer.put(vertices[j].z);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(normals[j].x);
            vertexBuffer.put(normals[j].y);
            vertexBuffer.put(normals[j].z);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(tangents[j].x);
            vertexBuffer.put(tangents[j].y);
            vertexBuffer.put(tangents[j].z);
            vertexBuffer.put(tangents[j].w);

            vertexBuffer.put(UV0[j].x);
            vertexBuffer.put(UV0[j].y);
            vertexBuffer.put(UV1[j].x);
            vertexBuffer.put(UV1[j].y);

            vertexBuffer.put((float) (bones[j][0] & 0xff));
            vertexBuffer.put((float) (bones[j][1] & 0xff));
            vertexBuffer.put((float) (bones[j][2] & 0xff));
            vertexBuffer.put((float) (bones[j][3] & 0xff));

            vertexBuffer.put(weights[j].x);
            vertexBuffer.put(weights[j].y);
            vertexBuffer.put(weights[j].z);
            vertexBuffer.put(weights[j].w);

            // for (int m = 0; m < 32; ++m) {
            //     Morph morph = morphs[m];
            //     if (morph == null) {
            //         vertexBuffer.position(vertexBuffer.position() + 0x20);
            //         continue;
            //     }
            //     vertexBuffer.put(morph.offsets[j].x);
            //     vertexBuffer.put(morph.offsets[j].y);
            //     vertexBuffer.put(morph.offsets[j].z);
            //     vertexBuffer.put(0.0f);

            //     vertexBuffer.put(morph.normals[j].x);
            //     vertexBuffer.put(morph.normals[j].y);
            //     vertexBuffer.put(morph.normals[j].z);
            //     vertexBuffer.put(0.0f);                
            // }
        }

        if (mesh.getMorphCount() != 0) {
            Morph[] morphs = mesh.getMorphs();
            // Maybe I should optimize this if there aren't many morphs, takes up 16mbs!
            ByteBuffer texture = BufferUtils.createByteBuffer(1024 * 1024 * 0x10);
            for (int i = 0; i < morphs.length; ++i) {
                Morph morph = morphs[i];

                int offset = ((1024 * 512) / 32) * i * 0x10;
                texture.position(offset);
                for (Vector3f position : morph.offsets) {
                    texture.putFloat(position.x);
                    texture.putFloat(position.y);
                    texture.putFloat(position.z);
                    texture.putFloat(0.0f);
                }

                texture.position((1024 * 512 * 0x10) + offset);
                for (int m = 0; m < morph.normals.length; ++m) {
                    Vector3f normal = morph.normals[m];
                    normal.sub(normals[m]);
                    texture.putFloat(normal.x);
                    texture.putFloat(normal.y);
                    texture.putFloat(normal.z);
                    texture.putFloat(0.0f);

                }
            }

            texture.flip();

            glMesh.morphLUT = new Texture(texture, 1024, 1024);
        }
        
        byte[] stream = mesh.getIndexStream();
        for (int j = 0; j < mesh.getNumIndices(); ++j) {
            int offset = (j * 0x2);
            int index = ((stream[offset] & 0xff) << 8) | (stream[offset + 1] & 0xff);
            indexBuffer.put(index);
        }

        vertexBuffer.flip();
        indexBuffer.flip();

        glMesh.VAO = glGenVertexArrays();
        glMesh.VBO = glGenBuffers();
        glMesh.EBO = glGenBuffers();

        glBindVertexArray(glMesh.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, glMesh.VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glMesh.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < attributeCount; ++i) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 0x4, GL_FLOAT, false, stride, 0x10 * i);
        }

        glBindVertexArray(0);

        Primitive[] primitives = mesh.getPrimitives().toArray(Primitive[]::new);
        glMesh.primitives = new MeshPrimitive[primitives.length];
        for (int i = 0; i < glMesh.primitives.length; ++i)
            glMesh.primitives[i] = new MeshPrimitive(primitives[i], mesh.getPrimitiveType());

        Arrays.sort(glMesh.primitives, (a, z) -> a.getAlphaLayer() - z.getAlphaLayer());

        int[] regions = mesh.getRegionIDsToHide();
        if (regions != null) {
            for (int region : regions)
                glMesh.regionIDsToHide.add(region);
        }
        glMesh.costumeCategoriesUsed = mesh.getCostumeCategoriesUsed();
        glMesh.hairMorphs = mesh.getHairMorphs();
        glMesh.types = mesh.getMirrorTypes();

        glMesh.numIndices = mesh.getNumIndices();

        MESHES.put(descriptor, glMesh);

        return glMesh;
    }

    public static Mesh getStaticMesh(ResourceDescriptor descriptor) {
        Mesh glMesh = Mesh.getFromCache(descriptor);
        if (glMesh != null) return glMesh;

        byte[] data = RenderSystem.getSceneGraph().getResourceData(descriptor);
        if (data == null) return null;

        glMesh = new Mesh();
        System.out.println("Linking StaticMesh: " + descriptor);

        RStaticMesh mesh = new RStaticMesh(new Resource(data));

        int[] indices = mesh.getIndices();
        glMesh.primitives = new MeshPrimitive[mesh.getMeshInfo().primitives.length];
        int primIndex = 0;
        for (StaticPrimitive primitive : mesh.getMeshInfo().primitives) {
            glMesh.primitives[primIndex] = new MeshPrimitive(primitive);
            for (int i = primitive.indexStart; i < primitive.indexStart + primitive.numIndices; ++i) {
                if (indices[i] == 65535) indices[i] = -1;
                else
                    indices[i] += primitive.vertexStart;
            }
            primIndex++;
        }

        int attributeCount = 0x6;
        int elementCount = 0x4 * attributeCount;
        int stride = 0x4 * elementCount;

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(elementCount * mesh.getNumVerts());
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);

        Vector3f[] vertices = mesh.getVertices();
        Vector3f[] normals = mesh.getNormals();
        Vector4f[] tangents = mesh.getTangents();
        Vector2f[] UV0 = mesh.getUV0();
        Vector2f[] UV1 = mesh.getUV1();

        for (int j = 0; j < vertices.length; ++j) {
            vertexBuffer.put(vertices[j].x);
            vertexBuffer.put(vertices[j].y);
            vertexBuffer.put(vertices[j].z);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(normals[j].x);
            vertexBuffer.put(normals[j].y);
            vertexBuffer.put(normals[j].z);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(tangents[j].x);
            vertexBuffer.put(tangents[j].y);
            vertexBuffer.put(tangents[j].z);
            vertexBuffer.put(tangents[j].w);

            vertexBuffer.put(UV0[j].x);
            vertexBuffer.put(UV0[j].y);
            vertexBuffer.put(UV1[j].x);
            vertexBuffer.put(UV1[j].y);

            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);

            vertexBuffer.put(1.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
        }

        indexBuffer.put(indices);

        vertexBuffer.flip();
        indexBuffer.flip();

        glMesh.VAO = glGenVertexArrays();
        glMesh.VBO = glGenBuffers();
        glMesh.EBO = glGenBuffers();

        glBindVertexArray(glMesh.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, glMesh.VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glMesh.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < attributeCount; ++i) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 0x4, GL_FLOAT, false, stride, 0x10 * i);
        }

        glBindVertexArray(0);

        MESHES.put(descriptor, glMesh);

        return glMesh;
    }

    public ResourceDescriptor getDescriptor() { return this.descriptor; }
    public Texture getMorphLUT() { return this.morphLUT; }
    public HashSet<Integer> getRegionIDsToHide() { return this.regionIDsToHide; }
    public int getCostumeCategoriesUsed() { return this.costumeCategoriesUsed; }
    public FlipType[] getBoneTypes() { return this.types; }
    public int getVAO() { return this.VAO; }
    public int getNumIndices() { return this.numIndices; }
    public Bone[] getBones() { return this.bones; }
    public MeshPrimitive[] getPrimitives() { return this.primitives; }
    public int getPrimitiveType() { return this.type; }

    public void delete() {
        if (this.VAO != 0)
            glDeleteVertexArrays(this.VAO);
        if (this.VBO != 0)
            glDeleteBuffers(this.VBO);
        if (this.EBO != 0)
            glDeleteBuffers(this.EBO);
        
        this.VAO = 0;
        this.VBO = 0;
        this.EBO = 0;

        MESHES.remove(this.descriptor);
    }
}
