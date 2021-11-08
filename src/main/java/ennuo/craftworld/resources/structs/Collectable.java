package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ResourceType;

public class Collectable {
    public static int MAX_SIZE = 0x15;
    
    
    public ResourceDescriptor item = new ResourceDescriptor();
    public int count = 0;
    
    public Collectable() {}
    public Collectable(Data data) {
        item = data.resource(ResourceType.PLAN, true);
        count = data.i32();
    }
    
    public void serialize(Output output) {
        output.resource(item, true);
        output.i32(count);
    }
    
}
