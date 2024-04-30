package cwlib.structs.things.components.poppet;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PoppetMode implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x20;

    public int mode, subMode;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        mode = serializer.i32(mode);
        subMode = serializer.i32(subMode);

        if (version < 0x18b) serializer.i32(0);
        if (version > 0x1b7 && version < 0x1ba)
        {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }
    }


    @Override
    public int getAllocatedSize()
    {
        return PoppetMode.BASE_ALLOCATION_SIZE;
    }


}