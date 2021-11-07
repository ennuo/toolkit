package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.types.data.ResourcePtr;

public class ProfileItem {
    public int GUID = 0;
    public ResourcePtr resource;
    public InventoryDetails metadata = new InventoryDetails();
    public int flags = 0;
}
