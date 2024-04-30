package cwlib.structs.inventory;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class UserCreatedDetails implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public String name = "";
    public String description = "";

    public UserCreatedDetails() { }

    public UserCreatedDetails(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        name = serializer.wstr(name);
        description = serializer.wstr(description);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length() * 2);
        if (this.description != null)
            size += (this.description.length() * 2);
        return size;
    }
}
