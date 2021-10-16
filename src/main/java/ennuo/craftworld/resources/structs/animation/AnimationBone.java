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
        animHash = data.u32f();
        if (data.revision > 0x272) {
            parent = data.i8();
            firstChild = data.i8();
            nextSibling = data.i8();
            flags = data.i8();
        } else {
            parent = data.i32();
            firstChild = data.i32();
            nextSibling = data.i32();
        }
    }
    
    public static AnimationBone[] array(Data data) {
        int count = data.i32();
        AnimationBone[] out = new AnimationBone[count];
        for (int i = 0; i < count; ++i)
            out[i] = new AnimationBone(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.u32f(animHash);
        if (output.revision > 0x272) {
            output.u8(parent);
            output.u8(firstChild);
            output.u8(nextSibling);
            output.u8(flags);
        }
        else {
            output.i32(parent);
            output.i32(firstChild);
            output.i32(nextSibling);
        }
    }
}
