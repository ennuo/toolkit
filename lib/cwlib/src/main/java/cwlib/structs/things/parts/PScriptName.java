package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Part that just contains the name of the
 * script attached, unknown if it serves any current
 * purpose.
 */
public class PScriptName implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public String name;

    public PScriptName() { }

    public PScriptName(String name)
    {
        this.name = name;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        name = serializer.str(name);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PScriptName.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += this.name.length();
        return size;
    }
}
