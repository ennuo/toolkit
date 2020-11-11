package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.things.InventoryMetadata;

public class ProfileItem {
    public int GUID = 0;
    public ResourcePtr resource = new ResourcePtr(null, RType.PLAN);
    public InventoryMetadata metadata = new InventoryMetadata();
    public int flags;
}
