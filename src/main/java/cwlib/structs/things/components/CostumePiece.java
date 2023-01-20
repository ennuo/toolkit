package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RMesh;
import cwlib.structs.mesh.Primitive;
import cwlib.types.data.ResourceDescriptor;
import editor.gl.MeshInstance;

public class CostumePiece implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public ResourceDescriptor mesh;
    public int categoriesUsed;
    public byte[] morphParamRemap;
    public Primitive[] primitives;
    @GsonRevision(min=0x19a) public ResourceDescriptor plan;

    public transient MeshInstance instance;

    public CostumePiece() {
        this.morphParamRemap = new byte[RMesh.MAX_MORPHS];
        for (int i = 0; i < this.morphParamRemap.length; ++i)
            this.morphParamRemap[i] = -1;
    }
    
    @SuppressWarnings("unchecked")
    @Override public CostumePiece serialize(Serializer serializer, Serializable structure) {
        CostumePiece costume = (structure == null) ? new CostumePiece() : (CostumePiece) structure;

        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        costume.mesh = serializer.resource(costume.mesh, ResourceType.MESH);
        costume.categoriesUsed = serializer.i32(costume.categoriesUsed);

        if (subVersion < 0x105) {
            int size = serializer.i32(costume.morphParamRemap != null ? costume.morphParamRemap.length : 0);
            if (serializer.isWriting() && size != 0) {
                MemoryOutputStream stream = serializer.getOutput();
                for (byte param : costume.morphParamRemap)
                    stream.i32(param);
            }
            else if (!serializer.isWriting()) {
                costume.morphParamRemap = new byte[size];
                MemoryInputStream stream = serializer.getInput();
                for (int i = 0; i < size; ++i)
                    costume.morphParamRemap[i] = (byte) (stream.i32() & 0xFF);
            }
        } else costume.morphParamRemap = serializer.bytearray(costume.morphParamRemap);  

        costume.primitives = serializer.array(costume.primitives, Primitive.class);

        if (version >= 0x19a)
            costume.plan = serializer.resource(costume.plan, ResourceType.PLAN, true);
        
        return costume;
    }

    @Override public int getAllocatedSize() { 
        int size = CostumePiece.BASE_ALLOCATION_SIZE;
        return size;
    }
}
