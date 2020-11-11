
package ennuo.craftworld.things;

public class Thing {
    ThingPtr parent;
    ThingPtr group;
    
    short createdBy = -1;
    short changedBy = -1;
    
    int planGUID = 0;
    
    short flags = 0;
    short extraFlags = 0;
    
    PartList parts = new PartList();
}
