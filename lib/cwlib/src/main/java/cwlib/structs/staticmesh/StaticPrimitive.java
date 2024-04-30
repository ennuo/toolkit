package cwlib.structs.staticmesh;

import cwlib.enums.CellGcmPrimitive;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;
import org.joml.Vector4f;

public class StaticPrimitive implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Vector4f min, max;
    public ResourceDescriptor gmat;
    public int vertexStart, indexStart;
    public int numIndices;
    public CellGcmPrimitive type = CellGcmPrimitive.TRIANGLES;

    /* Not actually serialized, just used for exporting */
    public transient int numVerts;

    @Override
    public void serialize(Serializer serializer)
    {
        min = serializer.v4(min);
        max = serializer.v4(max);
        gmat = serializer.resource(gmat, ResourceType.GFX_MATERIAL);
        vertexStart = serializer.i32(vertexStart);
        indexStart = serializer.i32(indexStart);
        numIndices = serializer.i32(numIndices);
        type = serializer.enum8(type);
    }

    @Override
    public int getAllocatedSize()
    {
        return StaticPrimitive.BASE_ALLOCATION_SIZE;
    }
}
