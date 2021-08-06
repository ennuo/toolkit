package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.structs.animation.AnimationBone;
import ennuo.craftworld.resources.structs.animation.Locator;
import org.joml.Vector4f;

public class Animation {
    short numFrames, FPS, loopStart;
    byte morphCount, boneCount, rotBonesAnimatedCount, posBonesAnimatedCount,
    scaledBonesAnimatedCount, morphsAnimatedCount;
    short locatorKeysCount;
    Vector4f posOffset, posScale;
    boolean fat;
    
    AnimationBone[] bones;
    
    
    
    Locator[] locators;
    
    
    
    public Animation(Data data) {
        numFrames = data.int16();
        FPS = data.int16();
        loopStart = data.int16();
        
        System.out.println(String.format("numFrames = %d, FPS = %d, loopStart = %ds", numFrames, FPS, loopStart));
        
        morphCount = data.int8();
        boneCount = data.int8();
        rotBonesAnimatedCount = data.int8();
        posBonesAnimatedCount = data.int8();
        scaledBonesAnimatedCount = data.int8();
        morphsAnimatedCount = data.int8();
        locatorKeysCount = data.int16();
        
        System.out.println("counts: ");
        System.out.println("\tmorphs = " + morphCount);
        System.out.println("\tbones = " + boneCount);
        System.out.println("\trotBonesAnimated = " + rotBonesAnimatedCount);
        System.out.println("\tposBonesAnimated = " + posBonesAnimatedCount);
        System.out.println("\tscaledBonesAnimated = " + scaledBonesAnimatedCount);
        System.out.println("\tmorphsAnimated = " + morphsAnimatedCount);
        System.out.println("\tlocatorKeys = " + locatorKeysCount);
        
        if (data.revision >= 0x3d9) {
            posOffset = data.v4();
            posScale = data.v4();
        
            fat = data.bool();
        }
        
        byte[] animData = data.bytes(data.int32());
        
        processAnimationData(animData);
        
        System.out.println("dataSize = 0x" + Bytes.toHex(animData.length));
        
        locators = Locator.array(data);
    }
    
    private void processAnimationData(byte[] buffer) {
        Data data = new Data(buffer);
        
        if (true) return;
        
        
        
        bones = new AnimationBone[boneCount];
        for (int i = 0; i < boneCount; ++i)
            bones[i] = new AnimationBone(data);
        
        AnimationBone[] rotation;
        AnimationBone[] position;
        AnimationBone[] scaled;
        
        rotation = new AnimationBone[rotBonesAnimatedCount];
        for (int i = 0; i < rotBonesAnimatedCount; ++i)
            rotation[i] = bones[data.int8()];
        
        position = new AnimationBone[posBonesAnimatedCount];
        for (int i = 0; i < posBonesAnimatedCount; ++i)
            position[i] = bones[data.int8()];
        
        scaled = new AnimationBone[scaledBonesAnimatedCount];
        for (int i = 0; i < scaledBonesAnimatedCount; ++i)
            scaled[i] = bones[data.int8()];
        
        
        
        
        
    }
    
    
    
}
