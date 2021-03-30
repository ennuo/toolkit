package ennuo.craftworld.resources.structs.animation;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class AnimationBone {
    long animHash;
    byte parent, firstChild, nextSibling, flags;
    
    public AnimationBone(Data data) {
        animHash = data.uint32f();
        parent = data.int8();
        firstChild = data.int8();
        nextSibling = data.int8();
        flags = data.int8();
    }
    
    public void serialize(Output output) {
        output.uint32f(animHash);
        output.int8(parent);
        output.int8(firstChild);
        output.int8(nextSibling);
        output.int8(flags);
    }
}
