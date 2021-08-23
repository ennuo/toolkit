package ennuo.craftworld.resources.structs.animation;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector4f;

public class AnimationBone {
    public long animHash;
    public int parent, firstChild, nextSibling, flags;
    
    public Vector4f initialRotation;
    public Vector4f initialPosition;
    public Vector4f initialScale;
    
    public Vector4f[] positions;
    public Vector4f[] rotations;
    public Vector4f[] scales;
    
    
    public void setTransformFromKeyframeIndex(int index) {
        if (index < positions.length && positions[index] != null)
            this.initialPosition = this.positions[index];
        if (index < rotations.length && rotations[index] != null)
            this.initialRotation = this.rotations[index];
        if (index < scales.length && scales[index] != null)
            this.initialScale = this.scales[index];
    }
    
    public AnimationBone(Data data) {
        animHash = data.uint32f();
        if (data.revision > 0x272) {
            parent = data.int8();
            firstChild = data.int8();
            nextSibling = data.int8();
            flags = data.int8();
        } else {
            parent = data.int32();
            firstChild = data.int32();
            nextSibling = data.int32();
        }
    }
    
    public static AnimationBone[] array(Data data) {
        int count = data.int32();
        AnimationBone[] out = new AnimationBone[count];
        for (int i = 0; i < count; ++i)
            out[i] = new AnimationBone(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.uint32f(animHash);
        if (output.revision > 0x272) {
            output.int8(parent);
            output.int8(firstChild);
            output.int8(nextSibling);
            output.int8(flags);
        }
        else {
            output.int32(parent);
            output.int32(firstChild);
            output.int32(nextSibling);
        }
    }
}
