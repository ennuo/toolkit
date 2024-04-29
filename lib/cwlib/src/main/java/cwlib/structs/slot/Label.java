package cwlib.structs.slot;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Represents a label tag that gets
 * assigned to a slot.
 */
public class Label implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x8;

    public int key;
    public int order;

    public Label() { }

    public Label(int key, int order)
    {
        this.key = key;
        this.order = order;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        key = serializer.i32(key);
        order = serializer.i32(order);
    }

    @Override
    public int getAllocatedSize()
    {
        return Label.BASE_ALLOCATION_SIZE;
    }
}
