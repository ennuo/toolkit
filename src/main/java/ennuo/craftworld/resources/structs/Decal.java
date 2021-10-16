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
        u = data.f32();
        v = data.f32();
        xvecu = data.f32();
        xvecv = data.f32();
        yvecu = data.f32();
        yvecv = data.f32();
        packedCol = data.i32();
        type = data.i32();
        metadataIndex = data.i16();
        if (data.revision <= 0x272)
            numMetadata = data.i16();
        placedBy = data.i16();
        scorchMark = data.i32();
        plan = data.resource(RType.PLAN);
    }
    
    
}
