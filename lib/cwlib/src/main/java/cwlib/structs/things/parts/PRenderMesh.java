package cwlib.structs.things.parts;

import java.util.HashMap;
import java.util.HashSet;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.enums.FlipType;
import cwlib.enums.ResourceType;
import cwlib.enums.ShadowType;
import cwlib.enums.VisibilityFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RAnimation;
import cwlib.structs.mesh.Bone;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Colors;

public class PRenderMesh implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;
    
    public ResourceDescriptor mesh;
    public Thing[] boneThings = new Thing[0];
    public ResourceDescriptor anim;
    public float animPos = 0.0f, animSpeed = 1.0f;
    public boolean animLoop = true;
    public float loopStart = 0.0f, loopEnd = 1.0f;
    public int editorColor = -1;
    public ShadowType castShadows = ShadowType.ALWAYS;
    public boolean RTTEnable;
    public byte visibilityFlags = VisibilityFlags.PLAY_MODE | VisibilityFlags.EDIT_MODE;
    public float poppetRenderScale = 1.0f;
    @GsonRevision(min = 0x1f6, max = 0x34c)
    public float parentDistanceFront, parentDistanceSide;

    public PRenderMesh() { }

    public PRenderMesh(ResourceDescriptor mesh, Thing[] bones)
    {
        this.mesh = mesh;
        this.boneThings = bones;
    }

    public PRenderMesh(ResourceDescriptor mesh)
    {
        this.mesh = mesh;
    }

    public static void calculateBoneTransform(RAnimation animation, float position,
                                              Matrix4f[] transforms, Bone[] bones, Bone bone,
                                              FlipType[] types, Matrix4f parent)
    {
        int index = Bone.indexOf(bones, bone.animHash);
        if (index == -1) return;
        // FlipType type = types[index];

        int frame = (int) Math.floor(position * animation.getNumFrames());
        // frame = 0;

        Matrix4f local = animation.getBlendedFrameMatrix(bone.animHash, frame, position, true);
        if (local == null) return;

        Matrix4f global = new Matrix4f(parent);
        if (bone.parent != -1) global.normalize3x3();
        global.mul(local);

        Matrix4f inverse = global.mul(bone.invSkinPoseMatrix, new Matrix4f());
        transforms[index] = inverse;

        for (Bone child : bones)
        {
            if (child.parent == index)
                calculateBoneTransform(animation, position, transforms, bones, child, types,
                    global);
        }

    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        mesh = serializer.resource(mesh, ResourceType.MESH);

        boneThings = serializer.thingarray(boneThings);

        anim = serializer.resource(anim, ResourceType.ANIMATION);
        animPos = serializer.f32(animPos);
        animSpeed = serializer.f32(animSpeed);
        animLoop = serializer.bool(animLoop);
        loopStart = serializer.f32(loopStart);
        loopEnd = serializer.f32(loopEnd);

        if (version > 0x31a) editorColor = serializer.i32(editorColor);
        else
        {
            if (serializer.isWriting())
                serializer.getOutput().v4(Colors.RGBA32.fromARGB(editorColor));
            else
            {
                Vector4f color = serializer.getInput().v4();
                editorColor = Colors.RGBA32.getARGB(color);
            }
        }

        castShadows = serializer.enum8(castShadows);
        RTTEnable = serializer.bool(RTTEnable);

        if (version < 0x2e3)
        {
            // I'm fairly sure that while this is technically "play mode visibility" in LBP2 onward,
            // in LBP1, it's visibility in general.
            boolean isVisible = serializer.bool(visibilityFlags != VisibilityFlags.NONE);
            if (!serializer.isWriting())
            {
                visibilityFlags = VisibilityFlags.EDIT_MODE;
                if (isVisible)
                    visibilityFlags |= VisibilityFlags.PLAY_MODE;
            }
        }
        else visibilityFlags = serializer.i8(visibilityFlags);

        poppetRenderScale = serializer.f32(poppetRenderScale);

        if (version > 0x1f5 && version < 0x34d)
        {
            parentDistanceFront = serializer.f32(parentDistanceFront);
            parentDistanceSide = serializer.f32(parentDistanceFront);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PRenderMesh.BASE_ALLOCATION_SIZE;
        if (this.boneThings != null) size += (this.boneThings.length * 0x4);
        return size;
    }
}
