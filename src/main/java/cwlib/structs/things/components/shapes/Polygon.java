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
public class Polygon implements Serializable {
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
    @GsonRevision(min=0x341)
    public boolean requiresZ = true;

    /**
     * Controls which parts of the polygon are "loops",
     * if two loops intersect with each other, it counts as a cut.
     */
    public int[] loops = { 4 };

    @SuppressWarnings("unchecked")
    @Override public Polygon serialize(Serializer serializer, Serializable structure) {
        Polygon polygon = (structure == null) ? new Polygon() : (Polygon) structure;

        if (serializer.getRevision().getVersion() < 0x341) {
            polygon.requiresZ = true;
            if (serializer.isWriting()) {
                if (polygon.vertices == null) 
                    serializer.getOutput().i32(0);
                else serializer.i32(polygon.vertices.length);
            } else polygon.vertices = new Vector3f[serializer.getInput().i32()];
            if (polygon.vertices != null)
                for (int i = 0; i < polygon.vertices.length; ++i)
                    polygon.vertices[i] = serializer.v3(polygon.vertices[i]);
            polygon.loops = serializer.intvector(polygon.loops);
            return polygon;
        }

        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            if (polygon.vertices != null && polygon.vertices.length != 0) {
                stream.i32(polygon.vertices.length);
                stream.bool(polygon.requiresZ);
                if (polygon.requiresZ)
                    for (Vector3f vertex : polygon.vertices)
                        stream.v3(vertex);
                else
                    for (Vector3f vertex : polygon.vertices)
                        stream.v2(new Vector2f(vertex.x, vertex.y));
            }
            else {
                stream.i32(0);
                stream.bool(false);
            }
            polygon.loops = serializer.intvector(polygon.loops);
            return polygon;
        }

        MemoryInputStream stream = serializer.getInput();
        polygon.vertices = new Vector3f[stream.i32()];
        polygon.requiresZ = stream.bool();
        if (polygon.vertices.length != 0) {
            for (int i = 0; i < polygon.vertices.length; ++i) {
                Vector3f vertex = null;
                if (polygon.requiresZ)
                    vertex = stream.v3();
                else 
                    vertex = new Vector3f(stream.f32(), stream.f32(), 0.0f);
                polygon.vertices[i] = vertex;
            }
        }

        polygon.loops = serializer.intvector(polygon.loops);

        return polygon;
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.vertices != null)
            size += (this.vertices.length * 0xC);
        if (this.loops != null)
            size += (this.loops.length * 0x4);
        return size;
    }
}