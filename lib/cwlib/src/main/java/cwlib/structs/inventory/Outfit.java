package cwlib.structs.inventory;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

public class Outfit implements Serializable 
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ArrayList<GUID> components = new ArrayList<>();
    public ResourceDescriptor outfit;

    @Override
    public void serialize(Serializer serializer) 
    {
        components = serializer.guidlist(components);
        outfit = serializer.resource(outfit, ResourceType.PLAN, true);
    }

    @Override
    public int getAllocatedSize() 
    {
        return BASE_ALLOCATION_SIZE + (4 * components.size());
    }
}
