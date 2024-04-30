package cwlib.structs.animation;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Vector3f;

public class Locator implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x12;

    public Vector3f position;
    public String name;
    public byte looping, type;

    @Override
    public void serialize(Serializer serializer)
    {
        position = serializer.v3(position);
        name = serializer.str(name);
        looping = serializer.i8(looping);
        type = serializer.i8(type);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length());
        return size;
    }
}
