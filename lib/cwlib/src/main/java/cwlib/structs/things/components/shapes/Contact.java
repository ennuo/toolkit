package cwlib.structs.things.components.shapes;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.parts.PShape;

public class Contact implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public PShape shape;
    public byte flags;

    @Override
    public void serialize(Serializer serializer)
    {
        shape = serializer.reference(shape, PShape.class);
        flags = serializer.i8(flags);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = Contact.BASE_ALLOCATION_SIZE;
        if (this.shape != null)
            size += shape.getAllocatedSize();
        return size;
    }
}
