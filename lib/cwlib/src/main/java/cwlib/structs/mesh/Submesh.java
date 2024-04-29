package cwlib.structs.mesh;

import org.joml.Matrix4f;

public class Submesh
{
    public Matrix4f transform;
    public int locator;
    public boolean skinned;
    public Primitive[] primitives;
}
