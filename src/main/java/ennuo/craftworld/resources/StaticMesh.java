package ennuo.craftworld.resources;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.utilities.Bytes;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class StaticMesh {
    public int numVerts;
    public int numIndices;
    
    public StaticMeshInfo info;
    
    public Vector3f[] vertices;
    public Vector3f[] normals;
    
    public Vector2f[] uv0;
    public Vector2f[] uv1;
    
    public byte[] indices;
    
    public StaticMesh(Resource resource) {
        this.info = resource.meshInfo;
        Data data = resource.handle;
        byte[] vertexBuffer = data.bytes(this.info.vertexStreamSize);
        this.indices = data.bytes(this.info.indexBufferSize);
        
        int vertexCount = vertexBuffer.length / 0x20;
        
        this.numVerts = vertexCount;
        this.numIndices = this.indices.length / 0x2;
        
        this.vertices = new Vector3f[vertexCount];
        this.normals = new Vector3f[vertexCount];
        this.uv0 = new Vector2f[vertexCount];
        this.uv1 = new Vector2f[vertexCount];
        
        Data vertexStream = new Data(vertexBuffer);
        for (int i = 0; i < vertexCount; ++i) {
            this.vertices[i] = vertexStream.v3();
            
            this.normals[i] = Bytes.decodeI32(vertexStream.u32());
            
            this.uv0[i] = new Vector2f(vertexStream.f16(), vertexStream.f16());
            
            long tangent = vertexStream.u32f();
            
            this.uv1[i] = new Vector2f(vertexStream.f16(), vertexStream.f16());
            
            long smoothNormal = vertexStream.u32f();
        }
    }
}
