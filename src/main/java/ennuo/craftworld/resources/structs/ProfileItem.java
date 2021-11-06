package ennuo.craftworld.resources.structs;

import ennuo.craftworld.types.data.ResourcePtr;

public class ProfileItem {
    public int GUID = 0;
    public ResourcePtr resource;
    public InventoryMetadata metadata = new InventoryMetadata();
    public int flags = 0;
}
