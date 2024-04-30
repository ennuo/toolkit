package cwlib.io.imports;

import cwlib.resources.RAnimation;
import cwlib.structs.animation.AnimBone;
import cwlib.types.SerializedResource;
import cwlib.util.FileIO;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.AnimationModel.Channel;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.joml.Vector4f;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnimationImporter
{
    private static final RAnimation SACKBOY =
        new SerializedResource(FileIO.getResourceFile("/binary/template.anim")).loadResource(RAnimation.class);

    private final GltfModel gltf;
    private AnimationModel animation;
    private final RAnimation resource;
    private final SkinModel skin;
    HashMap<NodeModel, Integer> nodeToIndex = new HashMap<>();

    public AnimationImporter(String glbSourcePath)
    throws IOException
    {
        this.gltf = new GltfModelReader().read(Path.of(glbSourcePath).toUri());
        this.resource = new RAnimation();
        if (this.gltf.getAnimationModels() != null && this.gltf.getAnimationModels().size() != 0)
            this.animation = this.gltf.getAnimationModels().get(0);
        this.skin = this.gltf.getSkinModels().get(0);
    }

    private void getSackboyAnimBones()
    {
        AnimBone[] bones = SACKBOY.bones;
        this.resource.bones = bones;
        for (NodeModel joint : this.skin.getJoints())
        {
            int hash = RAnimation.calculateAnimationHash(joint.getName());
            for (int i = 0; i < bones.length; ++i)
            {
                if (bones[i].animHash == hash)
                {
                    nodeToIndex.put(joint, i);
                    break;
                }
            }
        }
    }

    private void getAnimBones()
    {
        List<NodeModel> joints = this.skin.getJoints();
        AnimBone[] bones = new AnimBone[this.skin.getJoints().size()];

        // First pass to create the bones
        for (int i = 0; i < bones.length; ++i)
        {
            NodeModel joint = joints.get(i);

            bones[i] = new AnimBone(
                RAnimation.calculateAnimationHash(joint.getName()),
                -1,
                -1,
                -1
            );

            nodeToIndex.put(joint, i);
        }

        // Second pass to set fields of bones
        for (int i = 0; i < bones.length; ++i)
        {
            NodeModel joint = joints.get(i);
            List<NodeModel> children = joint.getChildren();
            if (children == null || children.size() == 0) continue;

            bones[i].firstChild = nodeToIndex.get(children.get(0));
            for (int j = 0; j < children.size(); ++j)
                bones[nodeToIndex.get(children.get(j))].parent = i;
            for (int j = 1; j < children.size(); ++j)
                bones[nodeToIndex.get(children.get(j - 1))].nextSibling =
                    nodeToIndex.get(children.get(j));
        }

        this.resource.bones = bones;
    }

    private Vector4f[] getChannelData(Channel channel)
    {
        AccessorModel accessor = channel.getSampler().getOutput();
        FloatBuffer buffer = accessor.getAccessorData().createByteBuffer().asFloatBuffer();
        boolean isRotation = channel.getPath().equals("rotation");
        Vector4f[] data = new Vector4f[accessor.getCount()];
        for (int i = 0; i < data.length; ++i)
        {
            Vector4f v = new Vector4f(buffer.get(), buffer.get(), buffer.get(), 1.0f);
            if (isRotation)
                v.w = buffer.get();
            data[i] = v;
        }
        return data;
    }

    private String getAnimationName()
    {
        return this.animation.getName();
    }

    public RAnimation getAnimation()
    {
        this.getAnimBones();
        //this.getSackboyAnimBones();

        ArrayList<Integer> posBonesAnimated = new ArrayList<>();
        ArrayList<Integer> rotBonesAnimated = new ArrayList<>();
        ArrayList<Integer> scaleBonesAnimated = new ArrayList<>();

        int frames = 0;

        if (this.animation != null)
        {
            for (Channel channel : this.animation.getChannels())
            {
                int index = this.nodeToIndex.get(channel.getNodeModel());

                Vector4f[] channelData = this.getChannelData(channel);
                boolean isAnimated = false;
                for (int i = 1; i < channelData.length; ++i)
                {
                    if (!channelData[i - 1].equals(channelData[i], 0.0001f))
                    {
                        isAnimated = true;
                        break;
                    }
                }

                // if (!isAnimated) continue;

                switch (channel.getPath())
                {
                    case "translation":
                        posBonesAnimated.add(index);
                        break;
                    case "rotation":
                        rotBonesAnimated.add(index);
                        break;
                    case "scale":
                        scaleBonesAnimated.add(index);
                        break;
                }

                int localFrameCount = channel.getSampler().getOutput().getCount();
                if (localFrameCount > frames)
                    frames = localFrameCount;
            }
        }

        AnimBone[] bones = this.resource.bones;

        Vector4f[] packedRotation =
            new Vector4f[bones.length + (rotBonesAnimated.size() * frames)];
        Vector4f[] packedPosition =
            new Vector4f[bones.length + (posBonesAnimated.size() * frames)];
        Vector4f[] packedScale =
            new Vector4f[bones.length + (scaleBonesAnimated.size() * frames)];

        for (NodeModel joint : this.skin.getJoints())
        {
            int index = nodeToIndex.get(joint);

            float[] translation = joint.getTranslation();
            float[] rotation = joint.getRotation();
            float[] scale = joint.getScale();

            if (translation == null) translation = new float[] { 0.0f, 0.0f, 0.0f };
            if (rotation == null) rotation = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
            if (scale == null) scale = new float[] { 1.0f, 1.0f, 1.0f };

            packedRotation[index] = new Vector4f(rotation);
            packedPosition[index] = new Vector4f(translation[0], translation[1],
                translation[2],
                1.0f);
            packedScale[index] = new Vector4f(scale[0], scale[1], scale[2], 1.0f);
        }

        this.resource.numFrames = (short) (frames + 1);
        this.resource.packedRotation = packedRotation;
        this.resource.packedPosition = packedPosition;
        this.resource.packedScale = packedScale;

        if (this.animation != null)
        {
            for (Channel channel : this.animation.getChannels())
            {
                int boneIndex = this.nodeToIndex.get(channel.getNodeModel());

                int numAnimated = -1;
                int animBoneIndex = -1;
                Vector4f[] packedData = null;
                switch (channel.getPath())
                {
                    case "translation":
                        animBoneIndex = posBonesAnimated.indexOf(boneIndex);
                        packedData = packedPosition;
                        numAnimated = posBonesAnimated.size();
                        break;
                    case "rotation":
                        animBoneIndex = rotBonesAnimated.indexOf(boneIndex);
                        packedData = packedRotation;
                        numAnimated = rotBonesAnimated.size();
                        break;
                    case "scale":
                        animBoneIndex = scaleBonesAnimated.indexOf(boneIndex);
                        packedData = packedScale;
                        numAnimated = scaleBonesAnimated.size();
                        break;
                }

                // It was optimized out
                // if (animBoneIndex == -1) continue;

                AccessorModel accessor = channel.getSampler().getOutput();
                FloatBuffer buffer =
                    accessor.getAccessorData().createByteBuffer().asFloatBuffer();
                boolean isRotation = channel.getPath().equals("rotation");
                for (int i = 0; i < frames; ++i)
                {
                    Vector4f data = new Vector4f(buffer.get(), buffer.get(),
                        buffer.get(), 1.0f);
                    if (isRotation)
                        data.w = buffer.get();
                    packedData[bones.length + (i * numAnimated) + animBoneIndex] = data;
                }
            }
        }

        this.resource.rotBonesAnimated = new byte[rotBonesAnimated.size()];
        this.resource.posBonesAnimated = new byte[posBonesAnimated.size()];
        this.resource.scaledBonesAnimated = new byte[scaleBonesAnimated.size()];

        for (int i = 0; i < rotBonesAnimated.size(); ++i)
            this.resource.rotBonesAnimated[i] = rotBonesAnimated.get(i).byteValue();
        for (int i = 0; i < posBonesAnimated.size(); ++i)
            this.resource.posBonesAnimated[i] = posBonesAnimated.get(i).byteValue();
        for (int i = 0; i < scaleBonesAnimated.size(); ++i)
            this.resource.scaledBonesAnimated[i] = scaleBonesAnimated.get(i).byteValue();

        this.resource.morphsAnimated = new byte[0];
        this.resource.packedMorph = new float[0];
        this.resource.morphCount = 0;

        return this.resource;
    }
}
