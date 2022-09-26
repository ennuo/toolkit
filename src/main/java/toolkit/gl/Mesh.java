package toolkit.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import cwlib.resources.RBevel;
import cwlib.resources.RMesh;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.bevel.BevelVertex;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.structs.things.parts.PShape;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;
import earcut4j.Earcut;
import javafx.scene.control.skin.TextInputControlSkin.Direction;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.*;

public class Mesh {
    public static HashMap<ResourceDescriptor, Mesh> MESHES = new HashMap<>();

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
    
    public double calculatePointAngle(Vector3f previous, Vector3f current, Vector3f next) {
        Vector3f previousMidpoint = previous.sub(current, new Vector3f());
        Vector3f nextMidpoint = next.sub(current, new Vector3f());

        double previousAngle = -Math.atan2(previousMidpoint.x, previousMidpoint.y);
        double nextAngle = -Math.atan2(nextMidpoint.x, nextMidpoint.y);
        double angle = 0.0;

        if (nextAngle < 0 && previousAngle < 0) {
            angle = (nextAngle - previousAngle) / 2.0;
            if ((angle < 1 && angle > 0) || (angle > -1 && angle < 0))
                angle = ((angle * 2) - 60) / 2;

        } else if (nextAngle > 0 && previousAngle > 0) {
            angle = (previousAngle - nextAngle) / 2;
            if ((angle < 1 && angle > 0) || (angle > -1 && angle < 0))
                angle = ((angle * 2) + 60) / 2;
        } else if (previousAngle > 0 && nextAngle < 0) {
            angle = (previousAngle + nextAngle) / 2;
        } else if (previousAngle < 0 && nextAngle > 0) {
            angle = (previousAngle + nextAngle) / 2;
            angle = angle - 135;
        } else {
            angle = (previousAngle + nextAngle) / 2;
            angle = angle - 135;
        }

        return angle;
    }

    public double calculatePointLength(Vector3f previous, Vector3f current, Vector3f next) {
        // double px = previous.x, py = previous.y;
        // double cx = current.x, cy = current.y;
        // double nx = next.x, ny = next.y;

        // double pd = Math.sqrt(Math.pow(px - cx, 2) + Math.pow(py - cy, 2));
        // double nd = Math.sqrt(Math.pow(nx - cx, 2) + Math.pow(ny - cy, 2));

        // double d = 0.0;
        // if (pd < nd) d = pd;
        // else if (nd < pd) d = nd;
        // else d = (nd + pd) / 2.0;

        // return 1 - (d / 100.0);

        return 1.0;
    }

    public double[] getPointAngles(Vector3f[] points) {
        double[] angles = new double[points.length];
        int pointsNum = points.length - 1;
        angles[0] = calculatePointAngle(
            points[pointsNum],
            points[0],
            points[1]
        );

        for (int i = 1; i < pointsNum; ++i)
            angles[i] = calculatePointAngle(points[i - 1], points[i], points[i + 1]);

        angles[pointsNum] = calculatePointAngle(points[pointsNum - 1], points[pointsNum], points[0]);

        return angles;
    }

    public double[] getPointLengths(Vector3f[] points) {
        double[] lengths = new double[points.length];
        int pointsNum = points.length - 1;
        lengths[0] = calculatePointLength(
            points[pointsNum],
            points[0],
            points[1]
        );

        for (int i = 1; i < pointsNum; ++i)
            lengths[i] = calculatePointLength(points[i - 1], points[i], points[i + 1]);

        lengths[pointsNum] = calculatePointLength(points[pointsNum - 1], points[pointsNum], points[0]);

        return lengths;
    }


    public Vector3f[] createPoints(Vector3f[] bevelPoints, Vector3f[] points, float depth) {
        ArrayList<Vector3f> vertices = new ArrayList<>();

        double[] angles = getPointAngles(points);
        double[] lengths = getPointLengths(points);

        for (int i = 0; i < points.length; ++i) {
            for (int j = 0; j < bevelPoints.length; ++j) {
                Vector3f point = new Vector3f(bevelPoints[j]);
                if (j == 0) point.z *= depth;
                point.y *= lengths[i];

                double cos = Math.cos(angles[i]);
                double sin = Math.sin(angles[i]);
                point.add(new Vector3f(
                    (float) (cos * point.x - sin * point.y),
                    (float) (sin * point.x + cos * point.y),
                    0.0f
                )).add(points[i]);

                vertices.add(point);
            }
        }

        return vertices.toArray(Vector3f[]::new);
    }

    public int[] createFaces(int bevelVertexCount, int shapeVertexCount) {
        ArrayList<Integer> quads = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < shapeVertexCount; ++i) {

            for (int j = count + 1; j < count + bevelVertexCount; ++j) {
                if (i == shapeVertexCount - 1) {
                    quads.add(j);
                    quads.add(j - (bevelVertexCount * (shapeVertexCount - 1)));
                    quads.add(j - (bevelVertexCount * (shapeVertexCount - 1)) + 1);
                    quads.add(j + 1);
                } else {
                    quads.add(j);
                    quads.add(j + bevelVertexCount);
                    quads.add(j + bevelVertexCount + 1);
                    quads.add(j + 1);
                }
            }

            count += bevelVertexCount;
        }
        
        // Would probably be better to actually generate these as tris as
        // we go, but this is all WIP anyway

        for (int i = 0; i < shapeVertexCount; ++i)
            quads.add((i + 1) * bevelVertexCount);

        int quadCount = quads.size() / 4;
        int[] triangles = new int[quadCount * 6];
        for (int i = 0; i < quadCount; ++i) {
            int quad_base = i * 4;
            int tri_base = i * 6;

            triangles[tri_base + 0] = quads.get(quad_base) - 1;
            triangles[tri_base + 1] = quads.get(quad_base + 1) - 1;
            triangles[tri_base + 2] = quads.get(quad_base + 2) - 1;

            triangles[tri_base + 3] = quads.get(quad_base + 0) - 1;
            triangles[tri_base + 4] = quads.get(quad_base + 2) - 1;
            triangles[tri_base + 5] = quads.get(quad_base + 3) - 1;
        }
        
        return triangles;
    }
    
    /**
     * Bevel + Mesh triangulation algorithm by DokkeFyxen!
     * TODO: UVS
     */
    public Mesh(PGeneratedMesh parameters, PShape shape) {
        this.type = GL_TRIANGLES;
        this.bones = new Bone[] { new Bone("Shape01") };

        float cutoff = 45.0f;
        float scale = 1.0f;

        Vector3f[] bevelPoints = new Vector3f[] {
            new Vector3f(0.0f, -0.5166667f, -1.0f),
            new Vector3f(0.0f, -0.16666667f, -0.8358108f),
            new Vector3f(0.0f, 0.0f, -0.49324325f),
            new Vector3f(0.0f, 0.0f, 0.5f),
            new Vector3f(0.0f, -0.16666666f, 0.8472973f),
            new Vector3f(0.0f, -0.5f, 1.0f)
        };

        for (Vector3f vertex : bevelPoints) {
            vertex.y *= 10.0f;
            vertex.z *= 10.0f;
        }

        if (parameters.bevel != null) {
            byte[] data = ResourceSystem.extract(parameters.bevel);
            if (data != null) {
                RBevel bevel = new Resource(data).loadResource(RBevel.class);
                cutoff = bevel.autoSmoothCutoffAngle;
                bevelPoints = new Vector3f[bevel.vertices.size()];
                for (int i = 0; i < bevelPoints.length; ++i) {
                    bevelPoints[i] = new Vector3f(
                        0.0f, 
                        bevel.vertices.get(i).y * bevel.fixedBevelSize,
                        bevel.vertices.get(i).z * bevel.fixedBevelSize
                    );
                }
            }
        }

        Vector3f[] shapePoints = shape.polygon.vertices;

        Vector3f[] vertices = createPoints(bevelPoints, shapePoints, shape.thickness);
        int[] triangles = createFaces(bevelPoints.length, shapePoints.length);

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

            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(-1.0f);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(1.0f);

            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);
            vertexBuffer.put(0.0f);

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

        this.type = GL_TRIANGLES;
        this.primitives = new MeshPrimitive[] { new MeshPrimitive(null, triangles.length) };
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

    public void draw(PLevelSettings lighting, Matrix4f[] matrices, Vector4f color) {
        glBindVertexArray(this.VAO);
        glPrimitiveRestartIndex(0xFFFF);

        for (MeshPrimitive primitive : this.primitives) {
            Shader.get(primitive.shader).bind(lighting, matrices, color);
            glDrawElements(this.type, primitive.numIndices, GL_UNSIGNED_INT, primitive.firstIndex * 4);
        }

        glUseProgram(0);
        glBindVertexArray(0);
    }
}
