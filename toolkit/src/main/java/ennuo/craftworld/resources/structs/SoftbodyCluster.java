package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;

public class SoftbodyCluster {
    int clusterCount;
    
    public SoftbodyCluster(Data data) {
        clusterCount = data.int32();
        
        
    }
}
