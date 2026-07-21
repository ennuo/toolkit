package cwlib.external.trianglejoiner;

import org.joml.Vector3f;

public class JoinTriangles 
{
    // This mostly just follows Blender's implementation
    // of a general algorithm for joining triangles
    // into quads.

    public class Edge
    {
        Vector3f v1, v2;
        Loop Loop;
    };

    public class Face
    {
        public Loop Loop;
        public int Length;
    };

    public class Loop
    {
        public Vector3f Vertex;
        public Edge Edge;
        public Face Face;

        public Loop RadialNext;
        public Loop RadialPrev;
        public Loop Next, Prev;
    };



}
