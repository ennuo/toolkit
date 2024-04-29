package cwlib.structs.things.components.shapes;

import org.joml.Vector2f;
import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

/**
 * Collection of vertices that make up
 * a collision shape.
 */
public class Polygon implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    /**
     * Vertices that make up this polygon.
     */
    public Vector3f[] vertices = {
        new Vector3f(-100, -100, 0),
        new Vector3f(-100, 100, 0),
        new Vector3f(100, 100, 0),
        new Vector3f(100, -100, 0)
    };

    /**
     * Whether or not this polygon needs the Z vertex.
     */
    @GsonRevision(min = 0x341)
    public boolean requiresZ = true;

    /**
     * Controls which parts of the polygon are "loops",
     * if two loops intersect with each other, it counts as a cut.
     */
    public int[] loops = { 4 };

    @Override
    public void serialize(Serializer serializer)
    {
        if (serializer.getRevision().getVersion() < 0x341)
        {
            requiresZ = true;
            if (serializer.isWriting())
            {
                if (vertices == null)
                    serializer.getOutput().i32(0);
                else serializer.i32(vertices.length);
            }
            else vertices = new Vector3f[serializer.getInput().i32()];
            if (vertices != null)
                for (int i = 0; i < vertices.length; ++i)
                    vertices[i] = serializer.v3(vertices[i]);
            loops = serializer.intvector(loops);
            return;
        }

        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            if (vertices != null && vertices.length != 0)
            {
                stream.i32(vertices.length);
                stream.bool(requiresZ);
                if (requiresZ)
                    for (Vector3f vertex : vertices)
                        stream.v3(vertex);
                else
                    for (Vector3f vertex : vertices)
                        stream.v2(new Vector2f(vertex.x, vertex.y));
            }
            else
            {
                stream.i32(0);
                stream.bool(false);
            }
            loops = serializer.intvector(loops);
            return;
        }

        MemoryInputStream stream = serializer.getInput();
        vertices = new Vector3f[stream.i32()];
        requiresZ = stream.bool();
        if (vertices.length != 0)
        {
            for (int i = 0; i < vertices.length; ++i)
            {
                Vector3f vertex = null;
                if (requiresZ)
                    vertex = stream.v3();
                else
                    vertex = new Vector3f(stream.f32(), stream.f32(), 0.0f);
                vertices[i] = vertex;
            }
        }

        loops = serializer.intvector(loops);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.vertices != null)
            size += (this.vertices.length * 0xC);
        if (this.loops != null)
            size += (this.loops.length * 0x4);
        return size;
    }
}