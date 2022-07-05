package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.animation.AnimBone;
import cwlib.structs.animation.Locator;
import cwlib.types.data.Revision;

import org.joml.Vector4f;

public class RAnimation implements Serializable, Compressable {
    public static enum AnimationType {
        ROTATION,
        SCALE,
        POSITION
    }

    public static final int BASE_ALLOCATION_SIZE = 0x100;

    private AnimBone[] bones;

    private short numFrames, fps, loopStart;
    private byte morphCount;

    private byte[] rotBonesAnimated;
    private byte[] posBonesAnimated;
    private byte[] scaledBonesAnimated;
    private byte[] morphsAnimated;

    private Vector4f posOffset, posScale;
    private boolean fat;

    private Vector4f[] packedRotation;
    private Vector4f[] packedPosition;
    private Vector4f[] packedScale;
    private float[] packedMorph;

    private Locator[] locators;

    @SuppressWarnings("unchecked")
    @Override public RAnimation serialize(Serializer serializer, Serializable structure) {
        RAnimation anim = (structure == null) ? new RAnimation() : (RAnimation) structure;

        int version = serializer.getRevision().getVersion();
        boolean isWriting = serializer.isWriting();

        if (version < 0x378) {
            anim.bones = serializer.array(anim.bones, AnimBone.class);

            anim.numFrames = (short) serializer.i32(anim.numFrames);
            anim.fps = (short) serializer.i32(anim.fps);
            anim.morphCount = (byte) serializer.i32(anim.morphCount);

            MemoryInputStream input = serializer.getInput();
            MemoryOutputStream output = serializer.getOutput();

            // Should I just throw these into some other function?
            // Pretty repetitive

            if (!isWriting) anim.rotBonesAnimated = new byte[input.i32()];
            else output.i32(anim.rotBonesAnimated.length);
            for (int i = 0; i < anim.rotBonesAnimated.length; ++i) 
                anim.rotBonesAnimated[i] = (byte) serializer.i16(anim.rotBonesAnimated[i]);

            if (!isWriting) anim.posBonesAnimated = new byte[input.i32()];
            else output.i32(anim.posBonesAnimated.length);
            for (int i = 0; i < anim.posBonesAnimated.length; ++i) 
                anim.posBonesAnimated[i] = (byte) serializer.i16(anim.posBonesAnimated[i]);

            if (!isWriting) anim.scaledBonesAnimated = new byte[input.i32()];
            else output.i32(anim.scaledBonesAnimated.length);
            for (int i = 0; i < anim.scaledBonesAnimated.length; ++i) 
                anim.scaledBonesAnimated[i] = (byte) serializer.i16(anim.scaledBonesAnimated[i]);

            if (!isWriting) anim.morphsAnimated = new byte[input.i32()];
            else output.i32(anim.morphsAnimated.length);
            for (int i = 0; i < anim.morphsAnimated.length; ++i) 
                anim.morphsAnimated[i] = (byte) serializer.i16(anim.morphsAnimated[i]);

            if (!isWriting) anim.packedRotation = new Vector4f[input.i32()];
            else output.i32(anim.packedRotation.length);
            for (int i = 0; i < anim.packedRotation.length; ++i) 
                anim.packedRotation[i] = serializer.v4(anim.packedRotation[i]);

            if (!isWriting) anim.packedPosition = new Vector4f[input.i32()];
            else output.i32(anim.packedPosition.length);
            for (int i = 0; i < anim.packedPosition.length; ++i)
                anim.packedPosition[i] = serializer.v4(anim.packedPosition[i]);

            if (!isWriting) anim.packedScale = new Vector4f[input.i32()];
            else output.i32(anim.packedScale.length);
            for (int i = 0; i < anim.packedScale.length; ++i)
                anim.packedScale[i] = serializer.v4(anim.packedScale[i]);

            if (!isWriting) anim.packedMorph = new float[input.i32()];
            else output.i32(anim.packedMorph.length);
            for (int i = 0; i < anim.packedMorph.length; ++i) 
                anim.packedMorph[i] = serializer.f32(anim.packedMorph[i]);
            
            // TODO: Locators when revision > 0x311

            return anim;
        }

        anim.numFrames = serializer.i16(anim.numFrames);
        anim.fps = serializer.i16(anim.fps);
        anim.loopStart = serializer.i16(anim.loopStart);
        anim.morphCount = serializer.i8(anim.morphCount);

        if (isWriting) {
            // TODO: Generate anim data buffer
            throw new SerializationException("RAnimation >=0x378 writing not supported!");
        }

        MemoryInputStream stream = serializer.getInput();

        int boneCount = stream.u8();
        int rotAnims = stream.u8();
        int posAnims = stream.u8();
        int scaleAnims = stream.u8();
        int morphAnims = stream.u8();
        int locatorKeys = stream.u16();

        if (version > 0x38b) {
            anim.posOffset = stream.v4();
            anim.posScale = stream.v4();
            if (version > 0x3b1)
                anim.fat = stream.bool();
        }

        byte[] animData = stream.bytearray();

        anim.locators = serializer.array(anim.locators, Locator.class);

        stream = new MemoryInputStream(animData);

        anim.bones = new AnimBone[boneCount];
        for (int i = 0; i < boneCount; ++i)
            anim.bones[i] = new AnimBone(stream.i32(), stream.u8(), stream.u8(), stream.u8());

        anim.rotBonesAnimated = new byte[rotAnims];
        anim.posBonesAnimated = new byte[posAnims];
        anim.scaledBonesAnimated = new byte[scaleAnims];
        anim.morphsAnimated = new byte[morphAnims];

        for (int i = 0; i < rotAnims; ++i) anim.rotBonesAnimated[i] = stream.i8();
        for (int i = 0; i < posAnims; ++i) anim.posBonesAnimated[i] = stream.i8();
        for (int i = 0; i < scaleAnims; ++i) anim.scaledBonesAnimated[i] = stream.i8();
        for (int i = 0; i < morphAnims; ++i) anim.morphsAnimated[i] = stream.i8();

        if (stream.getOffset() % 2 != 0) stream.i8(); // Alignment

        anim.packedRotation = new Vector4f[boneCount + (rotAnims * (anim.numFrames - 1))];
        anim.packedPosition = new Vector4f[boneCount + (posAnims * (anim.numFrames - 1))];
        anim.packedScale = new Vector4f[boneCount + (scaleAnims * (anim.numFrames - 1))];
        anim.packedMorph = new float[morphCount + (morphAnims * (anim.numFrames - 1))];

        for (int i = 0; i < anim.packedRotation.length; ++i) {
            float x = ((float) stream.i16()) / 0x7FFF;
            float y = ((float) stream.i16()) / 0x7FFF;
            float z = ((float) stream.i16()) / 0x7FFF;
            float w = (float) Math.sqrt(1 - ((Math.pow(x, 2)) + (Math.pow(y, 2) + (Math.pow(z, 2)))));

            anim.packedRotation[i] = new Vector4f(x, y, z, w);
        }

        for (int i = 0; i < anim.packedPosition.length; ++i)
            anim.packedPosition[i] = new Vector4f(stream.f16(), stream.f16(), stream.f16(), 1.0f);

        for (int i = 0; i < anim.packedScale.length; ++i)
            anim.packedScale[i] = new Vector4f(stream.f16(), stream.f16(), stream.f16(), 1.0f);

        for (int i = 0; i < anim.packedMorph.length; ++i)
            anim.packedMorph[i] = stream.f16();

        // TODO: What do locator keys look like, are they just another array of locators?

        return anim;
    }

    public Vector4f getBasePosition(int boneIndex) { return this.packedPosition[boneIndex]; }
    public Vector4f getBaseRotation(int boneIndex) { return this.packedRotation[boneIndex]; }
    public Vector4f getBaseScale(int boneIndex) { return this.packedScale[boneIndex]; }
    public float getBaseWeight(int morphIndex) { return this.packedMorph[morphIndex]; }
    public float[] getBaseWeights() {
        float[] weights = new float[this.morphCount];
        for (int i = 0; i < this.morphCount; ++i)
            weights[i] = this.packedMorph[i];
        return weights;
    }

    private int getBoneIndex(int animHash) {
        for (int i = 0; i < this.bones.length; ++i) {
            AnimBone bone = this.bones[i];
            if (bone.animHash == animHash)
                return i;
        }
        return -1;
    }

    public boolean isAnimated(int morph) {
        for (byte index : this.morphsAnimated)
            if (index == morph)
                return true;
        return false;
    }

    public boolean isAnimated(AnimBone bone, AnimationType type) {
        if (bone == null) return false;
        return this.isAnimated(bone.animHash, type);
    }

    public boolean isAnimated(int animHash, AnimationType type) {
        byte[] indices = null;
        switch (type) {
            case ROTATION: indices = this.rotBonesAnimated; break;
            case POSITION: indices = this.posBonesAnimated; break;
            case SCALE: indices = this.scaledBonesAnimated; break;
        }
        if (indices == null) return false;
        int index = this.getBoneIndex(animHash);
        if (index == -1) return false;
        for (byte animated : indices)
            if (animated == index)
                return true;
        return false;
    }
    
    private Vector4f[] getFrames(int animHash, AnimationType type) {
        if (!this.isAnimated(animHash, type)) return null;
        int index = this.getBoneIndex(animHash);
        if (index == -1) return null;

        Vector4f[] pack = null;
        int animated = 0;
        switch (type) {
            case ROTATION: {
                pack = this.packedRotation;
                animated = this.rotBonesAnimated.length;
                break;
            }
            case POSITION: {
                pack = this.packedPosition;
                animated = this.posBonesAnimated.length;
                break;
            }
            case SCALE: {
                pack = this.packedScale;
                animated = this.scaledBonesAnimated.length;
                break;
            }
            default: return null;
        }

        Vector4f[] frames = new Vector4f[this.numFrames];
        frames[0] = pack[index];
        for (int i = 1; i < this.numFrames; ++i)
            frames[i] = pack[this.bones.length + ((i - 1) * animated) + index];
        
        return frames;
    }

    public Vector4f[] getRotationFrames(AnimBone bone) { return  this.getRotationFrames(bone.animHash); }
    public Vector4f[] getRotationFrames(int animHash) { return this.getFrames(animHash, AnimationType.ROTATION); }

    public Vector4f[] getPositionFrames(AnimBone bone) { return  this.getPositionFrames(bone.animHash); }
    public Vector4f[] getPositionFrames(int animHash) { return this.getFrames(animHash, AnimationType.POSITION); }

    public Vector4f[] getScaleFrames(AnimBone bone) { return  this.getScaleFrames(bone.animHash); }
    public Vector4f[] getScaleFrames(int animHash) { return this.getFrames(animHash, AnimationType.SCALE); }

    public float[] getMorphFrames(int index) {
        if (index < 0 || index >= this.morphCount || !this.isAnimated(index)) return null;
        float[] frames = new float[this.numFrames];
        frames[0] = this.packedMorph[index];
        for (int i = 1; i < this.numFrames; ++i)
            frames[i] = this.packedMorph[this.morphCount + ((i - 1) * this.morphsAnimated.length) + index];
        return frames;

    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RAnimation.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.ANIMATION,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.bones != null) size += (this.bones.length * AnimBone.BASE_ALLOCATION_SIZE);
        if (this.rotBonesAnimated != null) size += (this.rotBonesAnimated.length * 2);
        if (this.posBonesAnimated != null) size += (this.posBonesAnimated.length * 2);
        if (this.scaledBonesAnimated != null) size += (this.scaledBonesAnimated.length * 2);
        if (this.morphsAnimated != null) size += (this.morphsAnimated.length * 2);
        if (this.packedRotation != null) size += (this.packedRotation.length * 0x10);
        if (this.packedPosition != null) size += (this.packedPosition.length * 0x10);
        if (this.packedScale != null) size += (this.packedScale.length * 0x10);
        if (this.packedMorph != null) size += (this.packedMorph.length * 0x4);
        if (this.locators != null)
            for (Locator locator : this.locators)
                size += (locator.getAllocatedSize());
        return size;
    }

    public AnimBone[] getBones() { return this.bones; }
    public int getBoneCount() { return this.bones.length; }
    public int getMorphCount() { return this.morphCount; }
    public int getFPS() { return this.fps; }
    public int getNumFrames() { return this.numFrames; }

    public byte[] getRotAnimated() { return this.rotBonesAnimated; }
    public byte[] getPosAnimated() { return this.posBonesAnimated; }
    public byte[] getScaleAnimated() { return this.scaledBonesAnimated; }
    public byte[] getMorphsAnimated() { return this.morphsAnimated; }
}
