package toolkit.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import cwlib.resources.RStaticMesh;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.*;

public class StaticMesh {
    public static HashMap<ResourceDescriptor, StaticMesh> MESHES = new HashMap<>();
    
    public int VAO, VBO, EBO;
    private MeshPrimitive[] primitives;

    public static StaticMesh get(ResourceDescriptor descriptor) {
        if (descriptor == null) return null;
        if (MESHES.containsKey(descriptor))
            return MESHES.get(descriptor);
        
        if (ResourceSystem.extract(descriptor) == null)
            return null;
        
        return new StaticMesh(descriptor);
    }

    public StaticMesh(ResourceDescriptor descriptor) {
        System.out.println("Linking Mesh: " + descriptor);

        if (MESHES.containsKey(descriptor))
            throw new RuntimeException("Mesh is already linked!");
        
        byte[] data = ResourceSystem.extract(descriptor);
        if (data == null)
            throw new RuntimeException("Unable to retrieve data for mesh!");
        RStaticMesh mesh = new RStaticMesh(new Resource(data));

        int[] indices = mesh.getIndices();
        this.primitives = new MeshPrimitive[mesh.getMeshInfo().primitives.length];
        int primIndex = 0;
        for (StaticPrimitive primitive : mesh.getMeshInfo().primitives) {
            this.primitives[primIndex] = new MeshPrimitive(primitive);
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

        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();
        this.EBO = glGenBuffers();

        glBindVertexArray(this.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < attributeCount; ++i) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 0x4, GL_FLOAT, false, stride, 0x10 * i);
        }

        glBindVertexArray(0);

        MESHES.put(descriptor, this);
    }

    public void draw(PLevelSettings lighting) {
        glBindVertexArray(this.VAO);
        glPrimitiveRestartIndex(0xFFFFFFFF);

        Matrix4f[] matrices = new Matrix4f[] { new Matrix4f().identity() };
        for (MeshPrimitive primitive : this.primitives) {
            Shader.get(primitive.shader).bind(lighting, matrices, null);
            glDrawElements(primitive.type, primitive.numIndices, GL_UNSIGNED_INT, primitive.firstIndex * 4);
        }

        glUseProgram(0);
        glBindVertexArray(0);
    }
}
