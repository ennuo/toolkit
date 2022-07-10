package workspace;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.CompressionFlags;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.resources.RAnimation;
import cwlib.resources.RMesh;
import cwlib.structs.animation.AnimBone;
import cwlib.types.Resource;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Animation;
import de.javagl.jgltf.impl.v2.AnimationChannel;
import de.javagl.jgltf.impl.v2.AnimationChannelTarget;
import de.javagl.jgltf.impl.v2.AnimationSampler;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.model.io.GltfAsset;
import de.javagl.jgltf.model.io.GltfAssetReader;

public class AnimationImportDemo {
    public static GltfAsset readGLB(String path) {
        GltfAsset asset = null;
        try {
            FileInputStream stream = new FileInputStream(new File("C:/Users/Rueezus/Desktop/3rddegree.glb"));
            asset = new GltfAssetReader().readWithoutReferences(stream);
        }
        catch (Exception ex) { return null; }
        return asset;
    }

    public static byte[] fromList(ArrayList<Integer> list) {
        byte[] arr = new byte[list.size()];
        for (int i = 0; i < list.size(); ++i)
            arr[i] = (byte) (list.get(i) & 0xff);
        return arr;
    }

    public static Vector4f[] getData(Accessor accessor, byte[] data) {
        Vector4f[] elements = new Vector4f[accessor.getCount()];

        if (accessor.getComponentType() != 5126) throw new RuntimeException("expected floats");

        MemoryInputStream stream = new MemoryInputStream(data);
        stream.setLittleEndian(true);
        Integer offset = accessor.getByteOffset();
        stream.seek(offset == null ? 0 : offset, SeekMode.Begin);
        boolean isV4 = (accessor.getType().toUpperCase().equals("VEC4"));
        for (int i = 0; i < elements.length; ++i) {
            if (isV4) elements[i] = stream.v4();
            else elements[i] = new Vector4f(stream.v3(), 1.0f);
        }

        return elements;
    }

    public static void main(String[] args) {
        GltfAsset asset = readGLB("C:/Users/Rueezus/Desktop/3rddegree.glb");
        
        
        byte[] buffer = null;
        {
            ByteBuffer bb = asset.getBinaryData();
            buffer = Arrays.copyOfRange(bb.array(), bb.position() + bb.arrayOffset(), bb.limit() + bb.arrayOffset());
        }
        
        GlTF gltf = (GlTF) asset.getGltf();
        Animation animation = gltf.getAnimations().get(0);
        System.out.println(String.format("Processing animation (%s)", animation.getName()));
        AnimationSampler[] samplers = animation.getSamplers().toArray(AnimationSampler[]::new);

        RAnimation lbpAnim = new RAnimation();
        lbpAnim.bones = new Resource("C:/Users/Rueezus/Desktop/stand.anim").loadResource(RAnimation.class).getBones();
        lbpAnim.morphCount = 29;
        lbpAnim.fps = 24;

        ArrayList<Integer> rotAnims = new ArrayList<>();
        ArrayList<Integer> posAnims = new ArrayList<>();
        ArrayList<Integer> scaleAnims = new ArrayList<>();
        ArrayList<Integer> morphAnims = new ArrayList<>();

        ArrayList<Vector4f> packedRot = new ArrayList<>();
        ArrayList<Vector4f> packedPos = new ArrayList<>();
        ArrayList<Vector4f> packedScale = new ArrayList<>();
        ArrayList<Float> packedMorphs = new ArrayList<>();
        for (int i = 0; i < lbpAnim.morphCount; ++i)
            packedMorphs.add(0.0f);

        ArrayList<Vector4f[]> rotations = new ArrayList<>();
        ArrayList<Vector4f[]> translations = new ArrayList<>();
        ArrayList<Vector4f[]> scales = new ArrayList<>();


        for (AnimBone bone : lbpAnim.bones) {
            Node node = null;
            for (Node search : gltf.getNodes()) {
                if (RAnimation.calculateAnimationHash(search.getName()) == bone.animHash) {
                    node = search;
                    break;
                }
            }
            if (node == null) throw new RuntimeException("Couldn't find bone!");

            float[] translation = node.getTranslation();
            float[] rotation = node.getRotation();
            float[] scale = node.getScale();

            packedPos.add(translation != null ? new Vector4f(translation[0], translation[1], translation[2], 1.0f) : new Vector4f());
            packedRot.add(rotation != null ? new Vector4f(rotation[0], rotation[1], rotation[2], rotation[3]) : new Vector4f());
            packedPos.add(scale != null ? new Vector4f(scale[0], scale[1], scale[2], 1.0f) : new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }

        // for (AnimationChannel channel : animation.getChannels()) {
        //     AnimationSampler sampler = samplers[channel.getSampler()];
        //     AnimationChannelTarget target = channel.getTarget();

        //     String name = gltf.getNodes().get(target.getNode()).getName();
        //     int animHash = RAnimation.calculateAnimationHash(name);
        //     int index = lbpAnim.getBoneIndex(animHash);
        //     if (index == -1) throw new RuntimeException("fuck you hunter, you asshole");
        //     AnimBone bone = lbpAnim.bones[index];


        //     Accessor accessor = gltf.getAccessors().get(sampler.getOutput());
        //     Vector4f[] elements = getData(accessor, buffer);
        //     short numFrames = (short) (elements.length + 1);
        //     if (numFrames > lbpAnim.numFrames)
        //         lbpAnim.numFrames = numFrames;
        //     System.out.println(elements.length);
        //     switch (target.getPath().toUpperCase()) {
        //         case "ROTATION": {
        //             rotAnims.add(index);
        //             rotations.add(elements);
        //             break;
        //         }
        //         case "TRANSLATION": {
        //             posAnims.add(index);
        //             translations.add(elements);
        //             break;
        //         }
        //         case "SCALE": {
        //             scaleAnims.add(index);
        //             scales.add(elements);
        //             break;
        //         }
        //         default: throw new RuntimeException("Unhandled target!");
        //     }
        //     // System.out.println(GsonUtils.toJSON(target));
        //     // System.out.println(GsonUtils.toJSON(sampler));
        // }

        lbpAnim.rotBonesAnimated = fromList(rotAnims);
        lbpAnim.posBonesAnimated = fromList(posAnims);
        lbpAnim.scaledBonesAnimated = fromList(scaleAnims);
        lbpAnim.morphsAnimated = fromList(morphAnims);

        // for (int i = 0; i < lbpAnim.numFrames - 1; ++i) {
        //     for (int j = 0; j < lbpAnim.rotBonesAnimated.length; ++j) {
        //         Vector4f[] frames = rotations.get(j);
        //         Vector4f frame = null;

        //         if (i >= frames.length) frame = frames[frames.length - 1];
        //         else frame = frames[i];

        //         packedRot.add(frame);
        //     }

        //     for (int j = 0; j < lbpAnim.posBonesAnimated.length; ++j) {
        //         Vector4f[] frames = translations.get(j);
        //         Vector4f frame = null;

        //         if (i >= frames.length) frame = frames[frames.length - 1];
        //         else frame = frames[i];

        //         packedPos.add(frame);
        //     }

        //     for (int j = 0; j < lbpAnim.scaledBonesAnimated.length; ++j) {
        //         Vector4f[] frames = scales.get(j);
        //         Vector4f frame = null;

        //         if (i >= frames.length) frame = frames[frames.length - 1];
        //         else frame = frames[i];

        //         packedScale.add(frame);
        //     }
        // }

        lbpAnim.packedRotation = packedRot.toArray(Vector4f[]::new);
        lbpAnim.packedPosition = packedPos.toArray(Vector4f[]::new);
        lbpAnim.packedScale = packedScale.toArray(Vector4f[]::new);
        lbpAnim.packedMorph = new float[packedMorphs.size()];
        for (int i = 0; i < packedMorphs.size(); ++i)
            lbpAnim.packedMorph[i] = packedMorphs.get(i);
        


        FileIO.write(GsonUtils.toJSON(new Resource("C:/Users/Rueezus/Desktop/stand.anim").loadResource(RAnimation.class)).getBytes(), "C:/Users/Rueezus/Desktop/stand.json");
        FileIO.write(Resource.compress(lbpAnim.build(new Revision(0x000703e7), CompressionFlags.USE_NO_COMPRESSION)), "E:/ps3/dev_hdd0/game/lbp3debug/usrdir/content_library/mods/htr/animations/3rddegree.anim");
    }
}
