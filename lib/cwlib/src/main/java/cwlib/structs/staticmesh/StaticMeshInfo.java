package cwlib.structs.staticmesh;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

import org.joml.Vector3f;

public class StaticMeshInfo implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public static class UnknownStruct implements Serializable
    {
        public static final int BASE_ALLOCATION_SIZE = 0x20;

        public Vector3f min, max;
        public short structIndexA, structIndexB, firstPrimitive, numPrimitives;

        @Override
        public void serialize(Serializer serializer)
        {
            min = serializer.v3(min);
            structIndexA = serializer.i16(structIndexA);
            structIndexB = serializer.i16(structIndexB);

            // If structIndexA/structIndexB is -1
            // then firstPrimitive and numPrimitives is set

            // If structIndexA/structIndexB is set
            // then firstPrimitive and numPrimitives is 0

            // Does -1 indicate an instance of a submesh
            // and otherwise a group of submeshes?

            max = serializer.v3(max);
            firstPrimitive = serializer.i16(firstPrimitive);
            numPrimitives = serializer.i16(numPrimitives);
        }

        @Override
        public int getAllocatedSize()
        {
            return UnknownStruct.BASE_ALLOCATION_SIZE;
        }
    }

    public ResourceDescriptor lightmap, risemap, fallmap;
    public int primitiveCount, unknownStructCount, indexBufferSize, vertexStreamSize;

    public StaticPrimitive[] primitives;
    public UnknownStruct[] unknown;

    @Override
    public void serialize(Serializer serializer)
    {
        lightmap = serializer.resource(lightmap, ResourceType.TEXTURE);
        risemap = serializer.resource(risemap, ResourceType.TEXTURE);
        fallmap = serializer.resource(fallmap, ResourceType.TEXTURE);

        primitiveCount = serializer.i32(primitiveCount);
        unknownStructCount = serializer.i32(unknownStructCount);
        indexBufferSize = serializer.i32(indexBufferSize);
        vertexStreamSize = serializer.i32(vertexStreamSize);

        primitives = serializer.array(primitives, StaticPrimitive.class);
        unknown = serializer.array(unknown, UnknownStruct.class);

        serializer.i32(0x48454c50); // "HELP", no idea, used as a marker?
    }

    @Override
    public int getAllocatedSize()
    {
        int size = StaticMeshInfo.BASE_ALLOCATION_SIZE;
        if (this.primitives != null)
            size += (this.primitives.length * StaticPrimitive.BASE_ALLOCATION_SIZE);
        if (this.unknown != null)
            size += (this.unknown.length * UnknownStruct.BASE_ALLOCATION_SIZE);
        return size;
    }
}
