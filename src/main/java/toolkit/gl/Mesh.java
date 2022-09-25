package toolkit.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import cwlib.resources.RMesh;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    public static HashMap<ResourceDescriptor, Mesh> MESHES = new HashMap<>();

    public static class MeshPrimitive {
        public int firstIndex, numIndices;
        public ResourceDescriptor shader;

        public MeshPrimitive(Primitive primitive) {
            this.firstIndex = primitive.getFirstIndex();
            this.numIndices = primitive.getNumIndices();
            this.shader = primitive.getMaterial();
            Shader.get(this.shader); // Preload
        }
    }

    public int VAO, VBO, EBO;
    private int type = GL_TRIANGLE_STRIP;
    MeshPrimitive[] primitives;
    public Bone[] bones;

    public static Mesh get(ResourceDescriptor descriptor) {
        if (descriptor == null) return null;
        if (MESHES.containsKey(descriptor))
            return MESHES.get(descriptor);
        
        if (ResourceSystem.extract(descriptor) == null)
            return null;
        
        return new Mesh(descriptor);
    }

    public Mesh(ResourceDescriptor descriptor) {
        System.out.println("Linking Mesh: " + descriptor);

        if (MESHES.containsKey(descriptor))
            throw new RuntimeException("Mesh is already linked!");
        
        byte[] data = ResourceSystem.extract(descriptor);
        if (data == null)
            throw new RuntimeException("Unable to retrieve data for mesh!");
        RMesh mesh = new Resource(data).loadResource(RMesh.class);

        this.type = (mesh.getPrimitiveType().getValue() & 0xff) - 1;
        this.bones = mesh.getBones();

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
        }
        
        byte[] stream = mesh.getIndexStream();
        for (int j = 0; j < mesh.getNumIndices(); ++j) {
            int offset = (j * 0x2);
            int index = ((stream[offset] & 0xff) << 8) | (stream[offset + 1] & 0xff);
            indexBuffer.put(index);
        }

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

        Primitive[] primitives = mesh.getPrimitives().toArray(Primitive[]::new);
        this.primitives = new MeshPrimitive[primitives.length];
        for (int i = 0; i < this.primitives.length; ++i)
            this.primitives[i] = new MeshPrimitive(primitives[i]);

        MESHES.put(descriptor, this);
    }

    public void draw(PLevelSettings lighting, Matrix4f[] matrices) {
        glBindVertexArray(this.VAO);

        for (MeshPrimitive primitive : this.primitives) {
            Shader.get(primitive.shader).bind(lighting, matrices);
            glDrawElements(this.type, primitive.numIndices, GL_UNSIGNED_INT, primitive.firstIndex * 4);
        }

        glUseProgram(0);
        glBindVertexArray(0);
    }
}
