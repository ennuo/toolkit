package cwlib.resources;

import cwlib.structs.staticmesh.StaticMeshInfo;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.types.SerializedResource;
import cwlib.enums.CellGcmPrimitive;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.resources.RMesh.Triangle;
import cwlib.util.BinaryPrimitives;
import cwlib.util.Bytes;
import cwlib.util.FileIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class RStaticMesh
{
    public final static int VERTEX_STRIDE = 0x20;
    public final static int VERTEX_STRIDE_VITA = 0x1c;

    public final static int INDEX_STRIDE = 0x2;

    private StaticMeshInfo info = new StaticMeshInfo();
    
    public byte[] vertexData = {};
    public byte[] indexData = {};

    private int numVerts;
    private int numIndices;

    private Vector3f[] vertices;
    private Vector3f[] normals;
    private Vector2f[] uv0;
    private Vector4f[] tangents;
    private Vector2f[] uv1;
    private Vector3f[] smoothNormals;

    private int[] indices;
    private boolean fetched;

    public RStaticMesh()
    {
        
    }

    public RStaticMesh(SerializedResource resource)
    {
        this.info = resource.getMeshInfo();

        MemoryInputStream stream = resource.getStream();
        this.vertexData = stream.bytes(this.info.vertexStreamSize);
        this.indexData = stream.bytes(this.info.indexBufferSize);
    }

    public void invalidate()
    {
        fetched = false;

        numVerts = 0;
        numIndices = 0;

        vertices = null;
        normals = null;
        uv0 = null;
        tangents = null;
        uv1 = null;
        smoothNormals = null;
        indices = null;
    }

    private void fetchVertexData()
    {
        if (fetched) return;

        numVerts = vertexData.length / VERTEX_STRIDE;
        numIndices = indexData.length / INDEX_STRIDE;

        vertices = new Vector3f[numVerts];
        normals = new Vector3f[numVerts];
        uv0 = new Vector2f[numVerts];
        tangents = new Vector4f[numVerts];
        uv1 = new Vector2f[numVerts];
        smoothNormals = new Vector3f[numVerts];

        indices = new int[numIndices];
        for (int i = 0, offset = 0; i < indices.length; ++i, offset += 2)
            indices[i] = (indexData[offset] & 0xff) << 8 | (indexData[offset + 1] & 0xff);

        MemoryInputStream vertexStream = new MemoryInputStream(vertexData);
        for (int i = 0; i < numVerts; ++i)
        {
            vertices[i] = vertexStream.v3();
            normals[i] = Bytes.unpackNormal32(vertexStream.u32());
            uv0[i] = new Vector2f(vertexStream.f16(), vertexStream.f16());
            tangents[i] = new Vector4f(Bytes.unpackNormal32(vertexStream.u32()), 1.0f);
            uv1[i] = new Vector2f(vertexStream.f16(), vertexStream.f16());
            smoothNormals[i] = Bytes.unpackNormal32(vertexStream.u32());
        }

        fetched = true;
    }

    public StaticMeshInfo getMeshInfo()
    {
        return this.info;
    }

    public int getNumVerts()
    {
        fetchVertexData();
        return numVerts;
    }

    public int getNumIndices()
    {
        fetchVertexData();
        return numIndices;
    }

    public Vector3f[] getVertices()
    {
        fetchVertexData();
        return vertices;
    }

    public Vector3f[] getNormals()
    {
        fetchVertexData();
        return normals;
    }

    public Vector4f[] getTangents()
    {
        fetchVertexData();
        return this.tangents;
    }

    public Vector3f[] getSmoothNormals()
    {
        fetchVertexData();
        return this.smoothNormals;
    }

    public Vector2f[] getUV0()
    {
        fetchVertexData();
        return this.uv0;
    }

    public Vector2f[] getUV1()
    {
        fetchVertexData();
        return this.uv1;
    }

    public int[] getIndices()
    {
        fetchVertexData();
        return indices;
    }

    /**
     * Calculates a triangle list from a given range in the index buffer.
     *
     * @param start First face to include in list
     * @param count Number of faces from start
     * @return Mesh's triangle list
     */
    public Triangle[] getTrianglesRemapped(int start, int count, CellGcmPrimitive type, int vertexStart, int[] remap)
    {
        int[] faces = Arrays.copyOfRange(this.getIndices(), start, start + count);
        for (int i = 0; i < faces.length; ++i)
        {
            if (faces[i] == 65535) continue;
            faces[i] = remap[faces[i] + vertexStart];
        }

        ArrayList<Triangle> triangles;
        if (type == CellGcmPrimitive.TRIANGLES)
        {
            triangles = new ArrayList<>(count / 3);
            for (int i = 0; i < faces.length; i += 3)
            {

                int a = faces[i];
                int b = faces[i + 1];
                int c = faces[i + 2];

                if (a == b || b == c || c == a) continue;

                triangles.add(new Triangle(a, b, c));
            }
        }
        else
        {
            triangles = new ArrayList<>(this.getNumVerts() * 0x3);
            triangles.add(new Triangle(faces[0], faces[1], faces[2]));
            for (int i = 3, j = 1; i < faces.length; ++i, ++j)
            {
                if (faces[i] == 65535)
                {
                    if (i + 3 >= count) break;


                    int a = faces[i + 1], b = faces[i + 2], c = faces[i + 3];
                    if (a != b && b != c && c != a)
                        triangles.add(new Triangle(a, b, c));

                    i += 3;
                    j = 0;
                    continue;
                }

                int a = faces[i - 2], b = faces[i - 1], c = faces[i];
                if (a == b || b == c || c == a) continue;

                if ((j & 1) != 0)
                    triangles.add(new Triangle(a, c, b));
                else triangles.add(new Triangle(a, b, c));
            }
        }

        return new LinkedHashSet<>(triangles).toArray(Triangle[]::new);
    }


    /**
     * Calculates a triangle list from a given range in the index buffer.
     *
     * @param start First face to include in list
     * @param count Number of faces from start
     * @return Mesh's triangle list
     */
    public int[] getTriangles(int start, int count, CellGcmPrimitive type)
    {
        int[] faces = Arrays.copyOfRange(this.getIndices(), start, start + count);
        
        if (type == CellGcmPrimitive.TRIANGLES) return faces;

        ArrayList<Integer> triangles = new ArrayList<>(this.getNumVerts() * 0x3);
        Collections.addAll(triangles, faces[0], faces[1], faces[2]);
        for (int i = 3, j = 1; i < faces.length; ++i, ++j)
        {
            if (faces[i] == 65535)
            {
                if (i + 3 >= count) break;
                Collections.addAll(triangles, faces[i + 1], faces[i + 2], faces[i + 3]);
                i += 3;
                j = 0;
                continue;
            }
            if ((j & 1) != 0)
                Collections.addAll(triangles, faces[i - 2], faces[i], faces[i - 1]);
            else Collections.addAll(triangles, faces[i - 2], faces[i - 1], faces[i]);
        }

        return triangles.stream().mapToInt(Integer::valueOf).toArray();
    }

    public class CachedVertex
    {
        public Vector3f Vertex;
        public Vector3f Normal;
        public Vector2f UV0;

        public CachedVertex(Vector3f v, Vector3f n, Vector2f u)
        {
            Vertex = v;
            Normal = n;
            UV0 = u;
        }

        @Override public int hashCode()
        {
            int hash = (int) (Vertex.hashCode() ^ (Vertex.hashCode() >>> 32));
            hash = 31 * hash + UV0.hashCode();
            return hash;
        }

        @Override public boolean equals(Object other)
        {
            if (!(other instanceof CachedVertex vertex)) return false;
            return vertex.Vertex.equals(Vertex, 0.01f) && vertex.UV0.equals(UV0, 0.01f);
        }
    }

    public byte[] toVita()
    {
        var vertices = getVertices();
        var normals = getNormals();
        var coords = getUV0();
        var cache = new HashMap<CachedVertex, Integer>(vertices.length);
        var remap = new int[vertices.length];

        var vertexList = new ArrayList<CachedVertex>(vertices.length);

        // Minimize all our vertices based on UV0 to remove
        // the lightmap vertices, since these don't exist on Vita.
        for (int i = 0; i < vertices.length; ++i)
        {
            var v = new CachedVertex(vertices[i], normals[i], coords[i]);
            if (cache.containsKey(v))
            {
                remap[i] = cache.get(v);
            }
            else
            {
                remap[i] = vertexList.size();
                cache.put(v, vertexList.size());
                vertexList.add(v);
            }
        }

        numVerts = vertexList.size();

        info.vertexStreamSize = vertexList.size() * VERTEX_STRIDE_VITA;
        var stream = new MemoryOutputStream(info.vertexStreamSize + info.indexBufferSize * 2);
        stream.setLittleEndian(true);
        for (int i = 0; i < vertexList.size(); ++i)
        {
            var v = vertexList.get(i);
            stream.v3(v.Vertex);
            stream.i32(Bytes.packNormal32(v.Normal));
            stream.f16(v.UV0.x);
            stream.f16(v.UV0.y);
            stream.i32(0);
            stream.i32(-1);
        }

        byte[] vertexData = stream.getBuffer();
        int indexOffset = info.vertexStreamSize;
        var indexCache = new HashMap<Integer, Integer>();
        for (var primitive : info.primitives)
        {
            // 1.08
            var triangles = getTrianglesRemapped(primitive.indexStart, primitive.numIndices, primitive.type, primitive.vertexStart, remap);

            int hash = triangles[0].hashCode();
            int min = Math.min(triangles[0].A, Math.min(triangles[0].B, triangles[0].C));
            for (int i = 1; i < triangles.length; ++i)
            {
                hash = 31 * hash + triangles[i].hashCode();

                int local = Math.min(triangles[i].A, Math.min(triangles[i].B, triangles[i].C));
                if (min > local)
                    min = local;
            }
            
            primitive.indexStart = (indexOffset - info.vertexStreamSize) / 2;
            primitive.vertexStart = min;
            primitive.numIndices = triangles.length * 3;
            primitive.type = CellGcmPrimitive.TRIANGLES;

            if (indexCache.containsKey(hash))
            {
                primitive.indexStart = indexCache.get(hash);
            }
            else
            {
                indexCache.put(hash, primitive.indexStart);
                for (var triangle : triangles)
                {
                    BinaryPrimitives.writeInt16LittleEndian(vertexData, (indexOffset += 2) - 2, (short)(triangle.A - primitive.vertexStart));
                    BinaryPrimitives.writeInt16LittleEndian(vertexData, (indexOffset += 2) - 2, (short)(triangle.B - primitive.vertexStart));
                    BinaryPrimitives.writeInt16LittleEndian(vertexData, (indexOffset += 2) - 2, (short)(triangle.C - primitive.vertexStart));
                }
            }
        }

        vertexData = Arrays.copyOf(vertexData, indexOffset);
        info.indexBufferSize = indexOffset - info.vertexStreamSize;

        // Remove the references to the lightmaps
        info.fallmap = null;
        info.lightmap = null;
        info.risemap = null;

        return vertexData;
    }

    public static void main(String[] args) 
    {
        RTexture texture = new RTexture(FileIO.read("C:/users/aidan/desktop/24bbe27c28f1e0a93d7a3b06d9f60f09111dff5d.tex"));
        var image = texture.getImage();


        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int w = image.getWidth();
        int h = image.getHeight();

        int hw = w / 2;
        int hh = h / 2;

        int[] borders = new int[4];
        int ox = 0, oy = 0;
        for (int i = 0; i < 4; ++i)
        {
            int a1 = 0, r1 = 0, g1 = 0, b1 = 0;

            for (int x = 0; x < hw; ++x)
            for (int y = 0; y < hh; ++y)
            {
                int p = pixels[((y + oy) * w) + (ox + x)];
                a1 += (p >>> 24) & 0xff;
                r1 += (p >>> 16) & 0xff;
                g1 += (p >>> 8) & 0xff;
                b1 += (p & 0xff);
            }

            borders[i] = 
                ((int)(a1 / (hw * hh)) << 24) |
                ((int)(r1 / (hw * hh)) << 16) |
                ((int)(g1 / (hw * hh)) << 8) |
                ((int)(b1 / (hw * hh)));

            if (i == 0 || i == 2) ox += hw - 1;
            if (i == 1) oy += hh - 1;
        }
    }
}
