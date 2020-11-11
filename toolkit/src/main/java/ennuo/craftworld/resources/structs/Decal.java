package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;

public class Decal {
    ResourcePtr texture = new ResourcePtr(null, RType.TEXTURE);
    public float u = 0, v = 1, xvecu = 0, xvecv = 1, yvecu = 1, yvecv = 1;
    int packedCol = 44373;
    int type = 0;
    short metadataIndex = -1, numMetadata, placedBy = -1;
    int scorchMark = 0;
    ResourcePtr plan = new ResourcePtr(null, RType.PLAN);
    
    public Decal(Data data) {
        texture = data.resource(RType.TEXTURE);
        u = data.float32();
        v = data.float32();
        xvecu = data.float32();
        xvecv = data.float32();
        yvecu = data.float32();
        yvecv = data.float32();
        packedCol = data.int32();
        type = data.int32();
        metadataIndex = data.int16();
        if (data.revision <= 0x272)
            numMetadata = data.int16();
        placedBy = data.int16();
        scorchMark = data.int32();
        plan = data.resource(RType.PLAN);
    }
    
    
}
