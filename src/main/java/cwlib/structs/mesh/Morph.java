package cwlib.structs.mesh;

import org.joml.Vector3f;

/**
 * Represents mesh deformations.
 */
public class Morph {
    /**
     * Relative offset of each vertex.
     */
    private Vector3f[] offsets;

    /**
     * New normals for each vertex.
     */
    private Vector3f[] normals;

    /**
     * Creates a morph from offset and normal data.
     * @param offsets Relative offsets of each vertex
     * @param normals New normals for each vertex
     */
    public Morph(Vector3f[] offsets, Vector3f[] normals) {
        this.offsets = offsets;
        this.normals = normals;
    }

    public Vector3f[] getOffsets() { return this.offsets; }
    public Vector3f[] getNormals() { return this.normals; }
}
