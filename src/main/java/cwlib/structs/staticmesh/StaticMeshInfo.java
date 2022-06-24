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

        public Vector3f v1, v2;
        public short s1, s2, s3, s4;
        
        @SuppressWarnings("unchecked")
        @Override public UnknownStruct serialize(Serializer serializer, Serializable structure) {
            UnknownStruct struct = (structure == null) ? new UnknownStruct() : (UnknownStruct) structure;
            
            struct.v1 = serializer.v3(struct.v1);
            struct.s1 = serializer.i16(struct.s1);
            struct.s2 = serializer.i16(struct.s2);
            struct.v2 = serializer.v3(struct.v2);
            struct.s3 = serializer.i16(struct.s3);
            struct.s4 = serializer.i16(struct.s4);
            
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
