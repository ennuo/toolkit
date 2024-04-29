package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.animation.AnimBone;
import cwlib.structs.animation.Locator;
import cwlib.types.data.Revision;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RAnimation implements Resource
{
    public enum AnimationType
    {
        ROTATION,
        SCALE,
        POSITION
    }

    public static class RPSAnimData
    {
        public Vector4f[] rot;
        public Vector4f[] pos;
        public Vector4f[] scale;
        public float[] morph;

        private RPSAnimData(RAnimation animation)
        {
            this.rot = new Vector4f[animation.bones.length];
            this.pos = new Vector4f[animation.bones.length];
            this.scale = new Vector4f[animation.bones.length];
            this.morph = new float[animation.morphCount];
        }
    }

    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public AnimBone[] bones;

    public short numFrames, fps = 24, loopStart;
    public byte morphCount;

    public byte[] rotBonesAnimated;
    public byte[] posBonesAnimated;
    public byte[] scaledBonesAnimated;
    public byte[] morphsAnimated;

    public Vector4f posOffset = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), posScale =
        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    public boolean fat; // if fat, each element is 4 bytes, otherwise 2 bytes, probably just
    // alignment?

    public Vector4f[] packedRotation;
    public Vector4f[] packedPosition;
    public Vector4f[] packedScale;
    public float[] packedMorph;

    public Locator[] locators;

    private transient RPSAnimData[] cachedFrameData;

    public static int calculateAnimationHash(String value)
    {
        if (value == null) return 0;
        long animHash = 0, offset = 0;

        if (value.contains(" R "))
        {
            offset = 3;
            value = value.replace(" R ", "");
        }
        else if (value.contains(" L "))
        {
            offset = 2;
            value = value.replace(" L ", "");
        }

        for (char c : value.toCharArray())
            animHash = ((long) c) + animHash * 0x1003fL;

        return (int) (((animHash * 4L) & 0xFFFFFFFFL) + offset);
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        boolean isWriting = serializer.isWriting();

        if (version < 0x378)
        {
            bones = serializer.array(bones, AnimBone.class);

            numFrames = (short) serializer.i32(numFrames);
            fps = (short) serializer.i32(fps);
            morphCount = (byte) serializer.i32(morphCount);

            MemoryInputStream input = serializer.getInput();
            MemoryOutputStream output = serializer.getOutput();

            // Should I just throw these into some other function?
            // Pretty repetitive

            if (!isWriting) rotBonesAnimated = new byte[input.i32()];
            else output.i32(rotBonesAnimated.length);
            for (int i = 0; i < rotBonesAnimated.length; ++i)
                rotBonesAnimated[i] = (byte) serializer.i16(rotBonesAnimated[i]);

            if (!isWriting) posBonesAnimated = new byte[input.i32()];
            else output.i32(posBonesAnimated.length);
            for (int i = 0; i < posBonesAnimated.length; ++i)
                posBonesAnimated[i] = (byte) serializer.i16(posBonesAnimated[i]);

            if (!isWriting) scaledBonesAnimated = new byte[input.i32()];
            else output.i32(scaledBonesAnimated.length);
            for (int i = 0; i < scaledBonesAnimated.length; ++i)
                scaledBonesAnimated[i] = (byte) serializer.i16(scaledBonesAnimated[i]);

            if (!isWriting) morphsAnimated = new byte[input.i32()];
            else output.i32(morphsAnimated.length);
            for (int i = 0; i < morphsAnimated.length; ++i)
                morphsAnimated[i] = (byte) serializer.i16(morphsAnimated[i]);

            if (!isWriting) packedRotation = new Vector4f[input.i32()];
            else output.i32(packedRotation.length);
            for (int i = 0; i < packedRotation.length; ++i)
                packedRotation[i] = serializer.v4(packedRotation[i]);

            if (!isWriting) packedPosition = new Vector4f[input.i32()];
            else output.i32(packedPosition.length);
            for (int i = 0; i < packedPosition.length; ++i)
                packedPosition[i] = serializer.v4(packedPosition[i]);

            if (!isWriting) packedScale = new Vector4f[input.i32()];
            else output.i32(packedScale.length);
            for (int i = 0; i < packedScale.length; ++i)
                packedScale[i] = serializer.v4(packedScale[i]);

            if (!isWriting) packedMorph = new float[input.i32()];
            else output.i32(packedMorph.length);
            for (int i = 0; i < packedMorph.length; ++i)
                packedMorph[i] = serializer.f32(packedMorph[i]);

            // TODO: Locators when revision > 0x311

            return;
        }

        numFrames = serializer.i16(numFrames);
        fps = serializer.i16(fps);
        loopStart = serializer.i16(loopStart);
        morphCount = serializer.i8(morphCount);

        if (isWriting)
        {
            MemoryOutputStream stream = new MemoryOutputStream(0x50000);
            for (AnimBone bone : this.bones)
            {
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

            for (Vector4f rotation : this.packedRotation)
            {
                short xrot = (short) Math.round(rotation.x * 0x7fff);
                xrot &= ~1;
                if (rotation.w < 0.0f) xrot |= 1;

                stream.i16(xrot);
                stream.i16((short) (Math.round(rotation.y * 0x7fff)));
                stream.i16((short) (Math.round(rotation.z * 0x7fff)));
            }

            for (Vector4f position : this.packedPosition)
            {
                stream.f16(position.x);
                stream.f16(position.y);
                stream.f16(position.z);
            }

            for (Vector4f scale : this.packedScale)
            {
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

            if (version > 0x38b)
            {
                stream.v4(posOffset);
                stream.v4(posScale);
                if (version > 0x3b1)
                    stream.bool(fat);
            }

            stream.bytearray(animData);

            serializer.array(locators, Locator.class);

            return;
        }

        MemoryInputStream stream = serializer.getInput();

        int boneCount = stream.u8();
        int rotAnims = stream.u8();
        int posAnims = stream.u8();
        int scaleAnims = stream.u8();
        int morphAnims = stream.u8();
        int locatorKeys = stream.u16();

        if (version > 0x38b)
        {
            posOffset = stream.v4();
            posScale = stream.v4();
            if (version > 0x3b1)
                fat = stream.bool();
        }

        byte[] animData = stream.bytearray();

        locators = serializer.array(locators, Locator.class);

        stream = new MemoryInputStream(animData);

        bones = new AnimBone[boneCount];
        for (int i = 0; i < boneCount; ++i)
        {
            bones[i] = new AnimBone(stream.i32(), stream.i8(), stream.i8(), stream.i8());
            stream.i8();
        }

        rotBonesAnimated = new byte[rotAnims];
        posBonesAnimated = new byte[posAnims];
        scaledBonesAnimated = new byte[scaleAnims];
        morphsAnimated = new byte[morphAnims];

        if (locatorKeys != 0) stream.bytes(0x4 * locatorKeys);

        for (int i = 0; i < rotAnims; ++i) rotBonesAnimated[i] = stream.i8();
        for (int i = 0; i < posAnims; ++i) posBonesAnimated[i] = stream.i8();
        for (int i = 0; i < scaleAnims; ++i) scaledBonesAnimated[i] = stream.i8();
        for (int i = 0; i < morphAnims; ++i) morphsAnimated[i] = stream.i8();

        if (stream.getOffset() % 2 != 0) stream.i8(); // Alignment

        packedRotation = new Vector4f[boneCount + (rotAnims * (numFrames - 1))];
        packedPosition = new Vector4f[boneCount + (posAnims * (numFrames - 1))];
        packedScale = new Vector4f[boneCount + (scaleAnims * (numFrames - 1))];
        packedMorph = new float[morphCount + (morphAnims * (numFrames - 1))];

        for (int i = 0; i < packedRotation.length; ++i)
        {
            short xrot = stream.i16();
            boolean flag = (xrot & 1) != 0;
            xrot &= (short) ~1;

            float x = (float) xrot / 0x7FFF;
            float y = ((float) stream.i16()) / 0x7FFF;
            float z = ((float) stream.i16()) / 0x7FFF;
            float w =
                (float) Math.sqrt(1 - ((Math.pow(x, 2)) + (Math.pow(y, 2) + (Math.pow(z
                    , 2)))));

            packedRotation[i] = new Vector4f(x, y, z, flag ? -w : w);
        }

        for (int i = 0; i < packedPosition.length; ++i)
            packedPosition[i] = new Vector4f(stream.f16(), stream.f16(), stream.f16(), 1.0f);

        for (int i = 0; i < packedScale.length; ++i)
            packedScale[i] = new Vector4f(stream.f16(), stream.f16(), stream.f16(), 1.0f);

        for (int i = 0; i < packedMorph.length; ++i)
            packedMorph[i] = stream.f16();

    }

    public int getLoopedFrame(int frame, boolean looped)
    {
        if (looped)
        {
            int nf = (frame - this.loopStart) % (this.numFrames - this.loopStart);
            if (nf < 0)
                nf += (this.numFrames - this.loopStart);
            return this.loopStart + nf;
        }

        if (frame < 0) return 0;
        if (this.numFrames <= frame)
            return this.numFrames - 1;
        return frame;
    }

    private void unpack(Vector4f[] out, Vector4f[] src, int animatedSize, int frame,
                        int boneCount, byte[] animated)
    {
        if (animatedSize == 0) return;
        int offset = animatedSize * (frame - 1) + boneCount;
        for (int bone = 0; bone < animatedSize; ++bone)
            out[animated[bone] & 0xff] = src[offset + bone];
    }

    private RPSAnimData getAnimDataForFrame(int frame)
    {
        RPSAnimData data = new RPSAnimData(this);

        for (int i = 0; i < this.bones.length; ++i)
        {
            data.rot[i] = this.packedRotation[i];
            data.pos[i] = this.packedPosition[i];
            data.scale[i] = this.packedScale[i];
        }

        if (this.morphCount >= 0)
            System.arraycopy(this.packedMorph, 0, data.morph, 0, this.morphCount);

        if (frame > 0)
        {
            this.unpack(data.rot, this.packedRotation, this.rotBonesAnimated.length, frame,
                this.bones.length, this.rotBonesAnimated);
            this.unpack(data.pos, this.packedPosition, this.posBonesAnimated.length, frame,
                this.bones.length, this.posBonesAnimated);
            this.unpack(data.scale, this.packedScale, this.scaledBonesAnimated.length, frame,
                this.bones.length, this.scaledBonesAnimated);
        }

        int offset = this.morphsAnimated.length * (frame - 1) + this.morphCount;
        for (int morph = 0; morph < this.morphsAnimated.length; ++morph)
            data.morph[this.morphsAnimated[morph] & 0xff] = this.packedMorph[offset + morph];

        return data;
    }

    public Vector4f getBasePosition(int boneIndex)
    {
        return this.packedPosition[boneIndex];
    }

    public Vector4f getBaseRotation(int boneIndex)
    {
        return this.packedRotation[boneIndex];
    }

    public Vector4f getBaseScale(int boneIndex)
    {
        return this.packedScale[boneIndex];
    }

    public float getBaseWeight(int morphIndex)
    {
        return this.packedMorph[morphIndex];
    }

    public Matrix4f getBaseTransform(int animHash)
    {
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

    private void cache()
    {
        if (this.cachedFrameData == null)
        {
            this.cachedFrameData = new RPSAnimData[this.numFrames];
            for (int i = 0; i < this.numFrames; ++i)
                this.cachedFrameData[i] = this.getAnimDataForFrame(i);
        }
    }

    public float[] getFrameWeights(int frame, float position, boolean looped)
    {
        this.cache();
        return this.cachedFrameData[this.getLoopedFrame(frame, looped)].morph;
    }

    public float[] getBlendedFrameWeights(int frame, float position, boolean looped)
    {
        this.cache();

        float lastTime = frame * (1.0f / this.numFrames);
        float nextTime = (frame + 1) * (1.0f / this.numFrames);
        float scaleFactor = (position - lastTime) / (nextTime - lastTime);

        RPSAnimData data = this.cachedFrameData[this.getLoopedFrame(frame, looped)];
        RPSAnimData next = this.cachedFrameData[this.getLoopedFrame(frame + 1, looped)];

        float[] weights = new float[data.morph.length];
        for (int i = 0; i < weights.length; i++)
            weights[i] = data.morph[i] + scaleFactor * (next.morph[i] - data.morph[i]);

        return weights;
    }

    public RPSAnimData getFrameData(int frame, float position, boolean looped)
    {
        this.cache();
        return this.cachedFrameData[this.getLoopedFrame(frame, looped)];
    }

    public Matrix4f getBlendedFrameMatrix(int animHash, int frame, float position, boolean looped)
    {
        float lastTime = frame * (1.0f / this.numFrames);
        float nextTime = (frame + 1) * (1.0f / this.numFrames);
        float scaleFactor = (position - lastTime) / (nextTime - lastTime);

        this.cache();

        int index = this.getBoneIndex(animHash);
        if (index == -1) return null;

        RPSAnimData data = this.cachedFrameData[this.getLoopedFrame(frame, looped)];
        RPSAnimData next = this.cachedFrameData[this.getLoopedFrame(frame + 1, looped)];

        Vector4f pos = data.pos[index].lerp(next.pos[index], scaleFactor, new Vector4f());
        Vector4f scale = data.scale[index].lerp(next.scale[index], scaleFactor, new Vector4f());
        Quaternionf rot = new Quaternionf(
            data.rot[index].x, data.rot[index].y, data.rot[index].z, data.rot[index].w
        ).slerp(
            new Quaternionf(
                next.rot[index].x, next.rot[index].y, next.rot[index].z,
                next.rot[index].w
            ),
            scaleFactor,
            new Quaternionf()
        );

        return new Matrix4f().identity().translationRotateScale(
            new Vector3f(pos.x, pos.y, pos.z), rot, new Vector3f(scale.x, scale.y, scale.z)
        );
    }

    public Matrix4f getFrameMatrix(int animHash, int frame, float position, boolean looped)
    {
        this.cache();

        int index = this.getBoneIndex(animHash);
        if (index == -1) return null;


        RPSAnimData data = this.cachedFrameData[this.getLoopedFrame(frame, looped)];

        Vector3f scale = new Vector3f(data.scale[index].x, data.scale[index].y,
            data.scale[index].z);

        return new Matrix4f().identity().translationRotateScale(
            new Vector3f(data.pos[index].x, data.pos[index].y, data.pos[index].z),
            new Quaternionf(data.rot[index].x, data.rot[index].y, data.rot[index].z,
                data.rot[index].w),
            scale
        );
    }

    public Matrix4f getFrameMatrix(int frame, int index)
    {
        this.cache();
        RPSAnimData data = this.cachedFrameData[frame];
        Vector3f scale = new Vector3f(data.scale[index].x, data.scale[index].y,
            data.scale[index].z);
        return new Matrix4f().identity().translationRotateScale(
            new Vector3f(data.pos[index].x, data.pos[index].y, data.pos[index].z),
            new Quaternionf(data.rot[index].x, data.rot[index].y, data.rot[index].z,
                data.rot[index].w),
            scale
        );
    }

    public Matrix4f getWorldPosition(int frame, int index)
    {
        this.cache();
        ArrayList<Matrix4f> sequence = new ArrayList<>();
        sequence.add(this.getFrameMatrix(frame, index));
        index = this.bones[index].parent;
        while (index != -1)
        {
            sequence.add(this.getFrameMatrix(frame, index));
            index = this.bones[index].parent;
        }
        Matrix4f wpos = new Matrix4f();
        for (int i = sequence.size() - 1; i >= 0; i--)
            wpos.mul(sequence.get(i));
        return wpos;
    }

    public void toZUp()
    {
        this.cache();

        Quaternionf f = this.getFrameMatrix(0, 2).getUnnormalizedRotation(new Quaternionf());
        System.out.printf("%f %f %f %f%n", f.x, f.y, f.z, f.w);

        Matrix4f[] matrices = new Matrix4f[this.bones.length];
        for (int i = 0; i < this.bones.length; i++)
            matrices[i] = this.getWorldPosition(0, i);
        // matrices[0].rotateX((float) Math.toRadians(90.0f));
        // matrices[0].mapXZY();

        for (int i = 0; i < this.bones.length; i++)
        {
            Matrix4f frame = matrices[i];
            if (this.bones[i].parent != -1)
            {
                Matrix4f parent = new Matrix4f(matrices[this.bones[i].parent]);
                frame = parent.invert().mul(frame);
            }

            Matrix4f myRot =
                this.getFrameMatrix(0, i).getUnnormalizedRotation(new Quaternionf()).get(new Matrix4f());
            if (this.bones[i].parent != -1)
            {
                Matrix4f p =
                    this.getFrameMatrix(0, this.bones[i].parent).getUnnormalizedRotation(new Quaternionf()).get(new Matrix4f());
                myRot = p.invert().mul(myRot);
            }

            Quaternionf rot = myRot.getNormalizedRotation(new Quaternionf());
            rot.rotateLocalX((float) Math.toRadians(90.0f));

            Vector3f pos = frame.getTranslation(new Vector3f());

            this.packedPosition[i] = new Vector4f(pos, 1.0f);
            this.packedRotation[i] = new Vector4f(rot.x, rot.y, rot.z, rot.w);
            this.packedScale[i] = new Vector4f(frame.getScale(new Vector3f()), 1.0f);
        }

        for (int i = 1; i < this.numFrames; i++)
        {
            for (int b = 0; b < this.bones.length; b++)
                matrices[b] = this.getWorldPosition(i, b);


        }


    }

    public float[] getBaseWeights()
    {
        float[] weights = new float[this.morphCount];
        System.arraycopy(this.packedMorph, 0, weights, 0, this.morphCount);
        return weights;
    }

    public int getBoneIndex(int animHash)
    {
        if (animHash == 0) return 0;
        for (int i = 0; i < this.bones.length; ++i)
        {
            AnimBone bone = this.bones[i];
            if (bone.animHash == animHash)
                return i;
        }
        return -1;
    }

    public boolean isAnimated(int morph)
    {
        for (byte index : this.morphsAnimated)
            if (index == morph)
                return true;
        return false;
    }

    public boolean isAnimatedAtAll(int animHash)
    {
        byte index = (byte) this.getBoneIndex(animHash);
        for (byte animated : this.posBonesAnimated)
            if (animated == index) return true;
        for (byte animated : this.rotBonesAnimated)
            if (animated == index) return true;
        for (byte animated : this.scaledBonesAnimated)
            if (animated == index) return true;
        return false;
    }

    public boolean isAnimated(AnimBone bone, AnimationType type)
    {
        if (bone == null) return false;
        return this.isAnimated(bone.animHash, type);
    }

    public boolean isAnimated(int animHash, AnimationType type)
    {
        return this.getAnimationIndex(animHash, type) != -1;
    }

    public int getAnimationIndex(int animHash, AnimationType type)
    {
        byte[] indices = null;
        switch (type)
        {
            case ROTATION:
                indices = this.rotBonesAnimated;
                break;
            case POSITION:
                indices = this.posBonesAnimated;
                break;
            case SCALE:
                indices = this.scaledBonesAnimated;
                break;
        }

        if (indices == null) return -1;
        int boneIndex = this.getBoneIndex(animHash);
        if (boneIndex == -1) return -1;

        for (int i = 0; i < indices.length; ++i)
        {
            int animBoneIndex = indices[i] & 0xff;
            if (animBoneIndex == boneIndex)
                return i;
        }

        return -1;
    }

    public Vector3f getTranslationFrame(int animHash, int frame)
    {
        int index = this.getBoneIndex(animHash);
        if (index == -1) return new Vector3f();
        Vector4f translation = null;
        int animIndex = this.getAnimationIndex(animHash, AnimationType.POSITION);
        if (frame == 0 || animIndex == -1) translation = this.packedPosition[index];
        else
            translation =
                this.packedPosition[this.bones.length + ((frame - 1) * this.posBonesAnimated.length) + animIndex];

        return new Vector3f(translation.x, translation.y, translation.z);
    }

    public Quaternionf getRotationFrame(int animHash, int frame)
    {
        int index = this.getBoneIndex(animHash);
        if (index == -1) return new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
        Vector4f rotation = null;
        int animIndex = this.getAnimationIndex(animHash, AnimationType.ROTATION);
        if (frame == 0 || animIndex == -1)
            rotation = this.packedRotation[index];
        else
            rotation =
                this.packedRotation[this.bones.length + ((frame - 1) * this.rotBonesAnimated.length) + animIndex];

        return new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    public Vector3f getScaleFrame(int animHash, int frame)
    {
        int index = this.getBoneIndex(animHash);
        if (index == -1) return new Vector3f(1.0f, 1.0f, 1.0f);
        Vector4f scale = null;
        int animIndex = this.getAnimationIndex(animHash, AnimationType.SCALE);
        if (frame == 0 || animIndex == -1)
            scale = this.packedScale[index];
        else
            scale =
                this.packedScale[this.bones.length + ((frame - 1) * this.scaledBonesAnimated.length) + animIndex];

        return new Vector3f(scale.x, scale.y, scale.z);
    }

    private Vector4f[] getFrames(int animHash, AnimationType type)
    {
        int animIndex = this.getAnimationIndex(animHash, type);
        if (animIndex == -1) return null;

        int boneIndex = this.getBoneIndex(animHash);
        if (boneIndex == -1) return null;

        Vector4f[] pack = null;
        int animated = 0;
        switch (type)
        {
            case ROTATION:
            {
                pack = this.packedRotation;
                animated = this.rotBonesAnimated.length;
                break;
            }
            case POSITION:
            {
                pack = this.packedPosition;
                animated = this.posBonesAnimated.length;
                break;
            }
            case SCALE:
            {
                pack = this.packedScale;
                animated = this.scaledBonesAnimated.length;
                break;
            }
            default:
                return null;
        }

        Vector4f[] frames = new Vector4f[this.numFrames];
        frames[0] = pack[boneIndex];
        for (int i = 1; i < this.numFrames; ++i)
            frames[i] = pack[this.bones.length + ((i - 1) * animated) + animIndex];

        return frames;
    }

    public Vector4f[] getRotationFrames(AnimBone bone)
    {
        return this.getRotationFrames(bone.animHash);
    }

    public Vector4f[] getRotationFrames(int animHash)
    {
        return this.getFrames(animHash, AnimationType.ROTATION);
    }

    public Vector4f[] getPositionFrames(AnimBone bone)
    {
        return this.getPositionFrames(bone.animHash);
    }

    public Vector4f[] getPositionFrames(int animHash)
    {
        return this.getFrames(animHash, AnimationType.POSITION);
    }

    public Vector4f[] getScaleFrames(AnimBone bone)
    {
        return this.getScaleFrames(bone.animHash);
    }

    public Vector4f[] getScaleFrames(int animHash)
    {
        return this.getFrames(animHash, AnimationType.SCALE);
    }

    public float[] getMorphFrames(int index)
    {
        if (index < 0 || index >= this.morphCount || !this.isAnimated(index)) return null;
        float[] frames = new float[this.numFrames];
        frames[0] = this.packedMorph[index];
        for (int i = 1; i < this.numFrames; ++i)
            frames[i] =
                this.packedMorph[this.morphCount + ((i - 1) * this.morphsAnimated.length) + index];
        return frames;

    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
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

    @Override
    public int getAllocatedSize()
    {
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

    public AnimBone[] getBones()
    {
        return this.bones;
    }

    public int getBoneCount()
    {
        return this.bones.length;
    }

    public int getMorphCount()
    {
        return this.morphCount;
    }

    public int getFPS()
    {
        return this.fps;
    }

    public int getNumFrames()
    {
        return this.numFrames;
    }

    public byte[] getRotAnimated()
    {
        return this.rotBonesAnimated;
    }

    public byte[] getPosAnimated()
    {
        return this.posBonesAnimated;
    }

    public byte[] getScaleAnimated()
    {
        return this.scaledBonesAnimated;
    }

    public byte[] getMorphsAnimated()
    {
        return this.morphsAnimated;
    }
}
