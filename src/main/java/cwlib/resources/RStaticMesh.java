package cwlib.resources;

import cwlib.structs.staticmesh.StaticMeshInfo;
import cwlib.types.Resource;
import cwlib.io.streams.MemoryInputStream;
import cwlib.util.Bytes;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RStaticMesh {
    private int numVerts;
    private int numIndices;
    
    private StaticMeshInfo info;
    
    private Vector3f[] vertices;

    private Vector3f[] normals;
    private Vector4f[] tangents;
    private Vector3f[] smoothNormals;

    private Vector2f[] uv0;
    private Vector2f[] uv1;
    
    private int[] indices;
    
    public RStaticMesh(Resource resource) {
        this.info = resource.getMeshInfo();

        MemoryInputStream stream = resource.getStream();
        byte[] vertexBuffer = stream.bytes(this.info.vertexStreamSize);

        int vertexCount = vertexBuffer.length / 0x20;
        int indexCount = (stream.getLength() - stream.getOffset()) / 0x2;

        this.indices = new int[indexCount];
        for (int i = 0; i < indexCount; ++i)
            this.indices[i] = stream.u16();
        
        this.numVerts = vertexCount;
        this.numIndices = indexCount;
        
        this.vertices = new Vector3f[vertexCount];

        this.normals = new Vector3f[vertexCount];
        this.tangents = new Vector4f[vertexCount];
        this.smoothNormals = new Vector3f[vertexCount];

        this.uv0 = new Vector2f[vertexCount];
        this.uv1 = new Vector2f[vertexCount];
        
        MemoryInputStream vertexStream = new MemoryInputStream(vertexBuffer);
        for (int i = 0; i < vertexCount; ++i) {
            this.vertices[i] = vertexStream.v3();
            this.normals[i] = Bytes.unpackNormal32(vertexStream.u32());
            this.uv0[i] = new Vector2f(vertexStream.f16(), vertexStream.f16());
            this.tangents[i] = new Vector4f(Bytes.unpackNormal32(vertexStream.u32()), 1.0f);
            this.uv1[i] = new Vector2f(vertexStream.f16(), vertexStream.f16());
            this.smoothNormals[i] = Bytes.unpackNormal32(vertexStream.u32());
        }
    }

    public int getNumVerts() { return this.numVerts; }
    public int getNumIndices() { return this.numIndices; }

    public StaticMeshInfo getMeshInfo() { return this.info; }

    public Vector3f[] getVertices() { return this.vertices; }
    public Vector3f[] getNormals() { return this.normals; }
    public Vector4f[] getTangents() { return this.tangents; }
    public Vector3f[] getSmoothNormals() { return this.smoothNormals; }
    
    public Vector2f[] getUV0() { return this.uv0; }
    public Vector2f[] getUV1() { return this.uv1; }

    public int[] getIndices() { return this.indices; }
}
