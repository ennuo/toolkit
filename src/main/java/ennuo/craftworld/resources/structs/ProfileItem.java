package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class ProfileItem {
    public int GUID = 0;
    public ResourceDescriptor resource;
    public InventoryDetails metadata = new InventoryDetails();
    public int flags = 0;
}
