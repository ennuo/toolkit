package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.animation.AnimBone;
import cwlib.structs.animation.Locator;
import cwlib.types.data.Revision;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RAnimation implements Serializable, Compressable {
    public static enum AnimationType {
        ROTATION,
        SCALE,
        POSITION
    }

    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public AnimBone[] bones;

    public short numFrames, fps = 24, loopStart;
    public byte morphCount;

    public byte[] rotBonesAnimated;
    public byte[] posBonesAnimated;
    public byte[] scaledBonesAnimated;
    public byte[] morphsAnimated;

    public Vector4f posOffset = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), posScale = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    public boolean fat; // if fat, each element is 4 bytes, otherwise 2 bytes, probably just alignment?

    public Vector4f[] packedRotation;
    public Vector4f[] packedPosition;
    public Vector4f[] packedScale;
    public float[] packedMorph;

    public Locator[] locators;

    public static int calculateAnimationHash(String value) {
        if (value == null) return 0;
        long animHash = 0, offset = 0;

        if (value.contains(" R ")) {
            offset = 3;
            value = value.replace(" R ", "");
        } else if (value.contains(" L ")) {
            offset = 2;
            value = value.replace(" L ", "");
        }

        for (char c : value.toCharArray())
            animHash = ((long)c) + animHash * 0x1003fl;
        
        return (int) (((animHash * 4l) & 0xFFFFFFFFl) + offset);
    }
    
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
            MemoryOutputStream stream = new MemoryOutputStream(0x50000);
            for (AnimBone bone : this.bones) {
                stream.i32(bone.animHash);
                stream.u8(bone.parent);
                stream.u8(bone.firstChild);
                stream.u8(bone.nextSibling);
                stream.u8(0); // Padding
            }

            for (byte bone : this.rotBonesAnimated) stream.i8(bone);
            for (byte bone : this.posBonesAnimated) stream.i8(bone);
            for (byte bone : this.scaledBonesAnimated) stream.i8(bone);
            for (byte bone : this.morphsAnimated) stream.i8(bone);

            if ((stream.getOffset() % 2) != 0) stream.u8(0);

            for (Vector4f rotation : this.packedRotation) {
                stream.i16((short) (Math.round(rotation.x * 0x7fff)));
                stream.i16((short) (Math.round(rotation.y * 0x7fff)));
                stream.i16((short) (Math.round(rotation.z * 0x7fff)));
            }

            for (Vector4f position : this.packedPosition) {
                stream.f16(position.x);
                stream.f16(position.y);
                stream.f16(position.z);
            }

            for (Vector4f scale : this.packedScale) {
                stream.f16(scale.x);
                stream.f16(scale.y);
                stream.f16(scale.z);
            }

            for (float morph : this.packedMorph) stream.f16(morph);

            while (stream.getOffset() % 16 != 0) stream.u8(0);

            stream.shrink();

            byte[] animData = stream.getBuffer();
            stream = serializer.getOutput();

            stream.u8(this.bones.length);
            stream.u8(this.rotBonesAnimated.length);
            stream.u8(this.posBonesAnimated.length);
            stream.u8(this.scaledBonesAnimated.length);
            stream.u8(this.morphsAnimated.length);
            stream.u16(0); // locatorKeys

            if (version > 0x38b) {
                stream.v4(anim.posOffset);
                stream.v4(anim.posScale);
                if (version > 0x3b1)
                    stream.bool(anim.fat);
            }

            stream.bytearray(animData);

            serializer.array(anim.locators, Locator.class);

            return anim;
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

        //System.out.println("Length: " + animData.length);

        anim.locators = serializer.array(anim.locators, Locator.class);

        stream = new MemoryInputStream(animData);

        anim.bones = new AnimBone[boneCount];
        for (int i = 0; i < boneCount; ++i) {
            anim.bones[i] = new AnimBone(stream.i32(), stream.i8(), stream.i8(), stream.i8());
            stream.i8(); 
        }

        anim.rotBonesAnimated = new byte[rotAnims];
        anim.posBonesAnimated = new byte[posAnims];
        anim.scaledBonesAnimated = new byte[scaleAnims];
        anim.morphsAnimated = new byte[morphAnims];

        // System.out.println("locator keys: " + stream.getOffset());
        if (locatorKeys != 0) stream.bytes(0x4 * locatorKeys);

        for (int i = 0; i < rotAnims; ++i) anim.rotBonesAnimated[i] = stream.i8();
        for (int i = 0; i < posAnims; ++i) anim.posBonesAnimated[i] = stream.i8();
        for (int i = 0; i < scaleAnims; ++i) anim.scaledBonesAnimated[i] = stream.i8();
        for (int i = 0; i < morphAnims; ++i) anim.morphsAnimated[i] = stream.i8();

        if (stream.getOffset() % 2 != 0) stream.i8(); // Alignment

        // System.out.println("Data: " + stream.getOffset());

        anim.packedRotation = new Vector4f[boneCount + (rotAnims * (anim.numFrames - 1))];
        anim.packedPosition = new Vector4f[boneCount + (posAnims * (anim.numFrames - 1))];
        anim.packedScale = new Vector4f[boneCount + (scaleAnims * (anim.numFrames - 1))];
        anim.packedMorph = new float[morphCount + (morphAnims * (anim.numFrames - 1))];

        // System.out.println("Rotation Size: " + anim.packedRotation.length * 0x6);
        // System.out.println("Position Size: " + anim.packedPosition.length * 0x6);
        // System.out.println("Scale Size: " + anim.packedScale.length * 0x6);

        // locatorKeys is 0x6???
        // 2 locator array 0x1e bytes?


        //System.out.println("Rotation Buffer Start: " + stream.getOffset());

        for (int i = 0; i < anim.packedRotation.length; ++i) {
            float x = ((float) stream.i16()) / 0x7FFF;
            float y = ((float) stream.i16()) / 0x7FFF;
            float z = ((float) stream.i16()) / 0x7FFF;
            float w = (float) Math.sqrt(1 - ((Math.pow(x, 2)) + (Math.pow(y, 2) + (Math.pow(z, 2)))));

            anim.packedRotation[i] = new Vector4f(x, y, z, w);
        }

        //System.out.println("Position Buffer Start: " + stream.getOffset());

        for (int i = 0; i < anim.packedPosition.length; ++i)
            anim.packedPosition[i] = new Vector4f(stream.f16(), stream.f16(), stream.f16(), 1.0f);

        //System.out.println("Scale Buffer Start: " + stream.getOffset());

        for (int i = 0; i < anim.packedScale.length; ++i)
            anim.packedScale[i] = new Vector4f(stream.f16(), stream.f16(), stream.f16(), 1.0f);

        //System.out.println("Morph Start: " + stream.getOffset());
        for (int i = 0; i < anim.packedMorph.length; ++i)
            anim.packedMorph[i] = stream.f16();

        //System.out.println(stream.getOffset());

        return anim;
    }

    public Vector4f getBasePosition(int boneIndex) { return this.packedPosition[boneIndex]; }
    public Vector4f getBaseRotation(int boneIndex) { return this.packedRotation[boneIndex]; }
    public Vector4f getBaseScale(int boneIndex) { return this.packedScale[boneIndex]; }
    public float getBaseWeight(int morphIndex) { return this.packedMorph[morphIndex]; }
    public Matrix4f getBaseTransform(int animHash) {
        int index = this.getBoneIndex(animHash);
        if (index == -1) return new Matrix4f().identity();
        Vector4f pos = this.packedPosition[index];
        Vector4f rot = this.packedRotation[index];
        Vector4f sx = this.packedScale[index];

        Vector3f translation = new Vector3f(pos.x, pos.y, pos.z);
        Quaternionf quaternion = new Quaternionf(rot.x, rot.y, rot.z, rot.w);
        Vector3f scale = new Vector3f(sx.x, sx.y, sx.y);

        return new Matrix4f().identity().translationRotateScale(
            translation,
            quaternion,
            scale
        );
    }

    public Matrix4f getFrameMatrix(int animHash, int frame, float position) {
        int index = this.getBoneIndex(animHash);

        // float lastTime = frame * ((float) (1.0f / this.numFrames));
        // float nextTime = (frame + 1) * ((float) (1.0f / this.numFrames));
        // float scaleFactor = (position - lastTime) / (nextTime - lastTime);

        int translationIndex = -1, scaleIndex = -1, rotationIndex = -1;

        Vector4f translation = this.packedPosition[index];
        Vector4f rotation =  this.packedRotation[index];
        Vector4f scale = this.packedScale[index];

        Quaternionf quaternion = new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w);

        for (int i = 0; i < this.rotBonesAnimated.length; ++i) {
            int bone = this.rotBonesAnimated[i] & 0xff;
            if (bone == index) {
                rotationIndex = i;
                break;
            }
        }

        for (int i = 0; i < this.scaledBonesAnimated.length; ++i) {
            int bone = this.scaledBonesAnimated[i] & 0xff;
            if (bone == index) {
                scaleIndex = i;
                break;
            }
        }

        for (int i = 0; i < this.posBonesAnimated.length; ++i) {
            int bone = this.posBonesAnimated[i] & 0xff;
            if (bone == index) {
                translationIndex = i;
                break;
            }
        }

        if (rotationIndex != -1 && frame < this.numFrames) {
            if (frame != 0) {
                rotation = this.packedRotation[this.bones.length + ((frame - 1) * this.rotBonesAnimated.length) + rotationIndex];
                quaternion = new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w);
            }

            // if (frame + 1 != this.numFrames) {
            //     rotation = this.packedRotation[this.bones.length + (frame * this.rotBonesAnimated.length) + rotationIndex];
            //     quaternion = quaternion.slerp(new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w), scaleFactor);
            // }
        }

        if (translationIndex != -1 && frame < this.numFrames) {
            if (frame != 0)
                translation = this.packedPosition[this.bones.length + ((frame - 1) * this.posBonesAnimated.length) + translationIndex];
            
            // if (frame + 1 != this.numFrames) {
            //     Vector4f nextFrame = this.packedPosition[this.bones.length + ((frame) * this.posBonesAnimated.length) + translationIndex];
            //     translation = translation.mul(1 - scaleFactor).add(nextFrame.mul(scaleFactor));
            // }
        }

        if (scaleIndex != -1 && frame < this.numFrames) {
            if (frame != 0)
                scale = this.packedScale[this.bones.length + ((frame - 1) * this.scaledBonesAnimated.length) + scaleIndex];
            
            // if (frame + 1 != this.numFrames) {
            //     Vector4f nextFrame = this.packedScale[this.bones.length + ((frame) * this.scaledBonesAnimated.length) + scaleIndex];
            //     scale = scale.mul(1 - scaleFactor).add(nextFrame.mul(scaleFactor));
            // }
        }

        return new Matrix4f().identity().translationRotateScale(
            new Vector3f(translation.x, translation.y, translation.z),
            quaternion,
            new Vector3f(scale.x, scale.y, scale.z)
        );
    }




    public float[] getBaseWeights() {
        float[] weights = new float[this.morphCount];
        for (int i = 0; i < this.morphCount; ++i)
            weights[i] = this.packedMorph[i];
        return weights;
    }

    public int getBoneIndex(int animHash) {
        if (animHash == 0) return 0;
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

    public boolean isAnimatedAtAll(int animHash) {
        byte index = (byte) this.getBoneIndex(animHash);
        for (byte animated : this.posBonesAnimated)
            if (animated == index) return true;
        for (byte animated : this.rotBonesAnimated)
            if (animated == index) return true;
        for (byte animated : this.scaledBonesAnimated)
            if (animated == index) return true;
        return false;
    }

    public boolean isAnimated(AnimBone bone, AnimationType type) {
        if (bone == null) return false;
        return this.isAnimated(bone.animHash, type);
    }

    public boolean isAnimated(int animHash, AnimationType type) {
        return this.getAnimationIndex(animHash, type) != -1;
    }

    public int getAnimationIndex(int animHash, AnimationType type) {
        byte[] indices = null;
        switch (type) {
            case ROTATION: indices = this.rotBonesAnimated; break;
            case POSITION: indices = this.posBonesAnimated; break;
            case SCALE: indices = this.scaledBonesAnimated; break;
        }

        if (indices == null) return -1;
        int boneIndex = this.getBoneIndex(animHash);
        if (boneIndex == -1) return -1;

        for (int i = 0; i < indices.length; ++i) {
            int animBoneIndex = indices[i] & 0xff;
            if (animBoneIndex == boneIndex)
                return i;
        }
        
        return -1;
    }

    public Vector3f getTranslationFrame(int animHash, int frame) {
        int index = this.getBoneIndex(animHash);
        if (index == -1) return new Vector3f();
        Vector4f translation = null;
        int animIndex = this.getAnimationIndex(animHash, AnimationType.POSITION);
        if (frame == 0 || animIndex == -1) translation = this.packedPosition[index];
        else 
            translation = this.packedPosition[this.bones.length + ((frame - 1) * this.posBonesAnimated.length) + animIndex];
        
        return new Vector3f(translation.x, translation.y, translation.z);
    }

    public Quaternionf getRotationFrame(int animHash, int frame) {
        int index = this.getBoneIndex(animHash);
        if (index == -1) return new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
        Vector4f rotation = null;
        int animIndex = this.getAnimationIndex(animHash, AnimationType.ROTATION);
        if (frame == 0 || animIndex == -1) 
            rotation = this.packedRotation[index];
        else
            rotation = this.packedRotation[this.bones.length + ((frame - 1) * this.rotBonesAnimated.length) + animIndex];

        return new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    public Vector3f getScaleFrame(int animHash, int frame) {
        int index = this.getBoneIndex(animHash);
        if (index == -1) return new Vector3f(1.0f, 1.0f, 1.0f);
        Vector4f scale = null;
        int animIndex = this.getAnimationIndex(animHash, AnimationType.SCALE);
        if (frame == 0 || animIndex == -1) 
            scale = this.packedScale[index];
        else
            scale = this.packedScale[this.bones.length + ((frame - 1) * this.scaledBonesAnimated.length) + animIndex];
        
        return new Vector3f(scale.x, scale.y, scale.z);
    }
    
    private Vector4f[] getFrames(int animHash, AnimationType type) {
        int animIndex = this.getAnimationIndex(animHash, type);
        if (animIndex == -1) return null;

        int boneIndex = this.getBoneIndex(animHash);
        if (boneIndex == -1) return null;

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
        frames[0] = pack[boneIndex];
        for (int i = 1; i < this.numFrames; ++i)
            frames[i] = pack[this.bones.length + ((i - 1) * animated) + animIndex];
        
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
