package ennuo.craftworld.resources.things;

import ennuo.craftworld.resources.things.parts.PartList;

public class Thing {
    public ThingPtr parent;
    public ThingPtr group;
    
    public short createdBy = -1;
    public short changedBy = -1;
    
    public int planGUID = 0;
    
    public short flags = 0;
    public short extraFlags = 0;
    
    public PartList parts = new PartList();
}
