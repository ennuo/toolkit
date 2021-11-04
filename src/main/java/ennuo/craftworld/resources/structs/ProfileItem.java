package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.ResourcePtr;

public class ProfileItem {
    public int GUID = 0;
    public ResourcePtr resource;
    public InventoryMetadata metadata = new InventoryMetadata();
    public int flags = 0;
}
