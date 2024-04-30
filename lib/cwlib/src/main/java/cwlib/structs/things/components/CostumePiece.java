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

public class CostumePiece implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public ResourceDescriptor mesh;
    public int categoriesUsed;
    public byte[] morphParamRemap;
    public Primitive[] primitives;
    @GsonRevision(min = 0x19a)
    public ResourceDescriptor plan;

    public CostumePiece()
    {
        this.morphParamRemap = new byte[RMesh.MAX_MORPHS];
        for (int i = 0; i < this.morphParamRemap.length; ++i)
            this.morphParamRemap[i] = -1;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        mesh = serializer.resource(mesh, ResourceType.MESH);
        categoriesUsed = serializer.i32(categoriesUsed);

        if (subVersion < 0x105)
        {
            int size = serializer.i32(morphParamRemap != null ?
                morphParamRemap.length : 0);
            if (serializer.isWriting() && size != 0)
            {
                MemoryOutputStream stream = serializer.getOutput();
                for (byte param : morphParamRemap)
                    stream.i32(param);
            }
            else if (!serializer.isWriting())
            {
                morphParamRemap = new byte[size];
                MemoryInputStream stream = serializer.getInput();
                for (int i = 0; i < size; ++i)
                    morphParamRemap[i] = (byte) (stream.i32() & 0xFF);
            }
        }
        else morphParamRemap = serializer.bytearray(morphParamRemap);

        primitives = serializer.array(primitives, Primitive.class);

        if (version >= 0x19a)
            plan = serializer.resource(plan, ResourceType.PLAN, true);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = CostumePiece.BASE_ALLOCATION_SIZE;
        return size;
    }
}
