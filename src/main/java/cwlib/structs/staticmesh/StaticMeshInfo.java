package cwlib.structs.staticmesh;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

import org.joml.Vector3f;

public class StaticMeshInfo implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public static class UnknownStruct implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x20;

        public Vector3f min, max;
        public short structIndexA, structIndexB, firstPrimitive, numPrimitives;
        
        @SuppressWarnings("unchecked")
        @Override public UnknownStruct serialize(Serializer serializer, Serializable structure) {
            UnknownStruct struct = (structure == null) ? new UnknownStruct() : (UnknownStruct) structure;
            
            struct.min = serializer.v3(struct.min);
            struct.structIndexA = serializer.i16(struct.structIndexA);
            struct.structIndexB = serializer.i16(struct.structIndexB);

            // If structIndexA/structIndexB is -1
            // then firstPrimitive and numPrimitives is set
            
            // If structIndexA/structIndexB is set
            // then firstPrimitive and numPrimitives is 0

            // Does -1 indicate an instance of a submesh
            // and otherwise a group of submeshes?

            struct.max = serializer.v3(struct.max);
            struct.firstPrimitive = serializer.i16(struct.firstPrimitive);
            struct.numPrimitives = serializer.i16(struct.numPrimitives);
            
            return struct;
        }

        @Override public int getAllocatedSize() { return UnknownStruct.BASE_ALLOCATION_SIZE; }
    }
    
    public ResourceDescriptor lightmap, risemap, fallmap;
    public int primitiveCount, unknownStructCount, indexBufferSize, vertexStreamSize;
    
    public StaticPrimitive[] primitives;
    public UnknownStruct[] unknown;
    
    @SuppressWarnings("unchecked")
    @Override public StaticMeshInfo serialize(Serializer serializer, Serializable structure) {
        StaticMeshInfo info = (structure == null) ? new StaticMeshInfo() : (StaticMeshInfo) structure;
        
        info.lightmap = serializer.resource(info.lightmap, ResourceType.TEXTURE);
        info.risemap = serializer.resource(info.risemap, ResourceType.TEXTURE);
        info.fallmap = serializer.resource(info.fallmap, ResourceType.TEXTURE);
        
        info.primitiveCount = serializer.i32(info.primitiveCount);
        info.unknownStructCount = serializer.i32(info.unknownStructCount);
        info.indexBufferSize = serializer.i32(info.indexBufferSize);
        info.vertexStreamSize = serializer.i32(info.vertexStreamSize);
        
        info.primitives = serializer.array(info.primitives, StaticPrimitive.class);
        info.unknown = serializer.array(info.unknown, UnknownStruct.class);
        
        serializer.i32(0x48454c50); // "HELP", no idea, used as a marker?
        
        return info;
    }

    @Override public int getAllocatedSize() {
        int size = StaticMeshInfo.BASE_ALLOCATION_SIZE;
        if (this.primitives != null)
            size += (this.primitives.length * StaticPrimitive.BASE_ALLOCATION_SIZE);
        if (this.unknown != null)
            size += (this.unknown.length * UnknownStruct.BASE_ALLOCATION_SIZE);
        return size;
    }
}
