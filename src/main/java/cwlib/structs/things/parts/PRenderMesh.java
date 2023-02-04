package cwlib.structs.things.parts;

import java.util.HashMap;
import java.util.HashSet;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.ShadowType;
import cwlib.enums.VisibilityFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RAnimation;
import cwlib.resources.custom.RSceneGraph;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.mesh.Bone;
import cwlib.structs.things.Thing;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Colors;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import editor.gl.MeshInstance;
import editor.gl.RenderSystem;
import editor.gl.objects.Mesh;

public class PRenderMesh implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;
    public static final HashMap<ResourceDescriptor, RAnimation> ANIMATIONS = new HashMap<>();
    public static final HashSet<ResourceDescriptor> DISABLED_ANIMATIONS = new HashSet<>();

    public ResourceDescriptor mesh;
    public transient MeshInstance instance;
    public Thing[] boneThings = new Thing[0];
    public transient Matrix4f[] boneModels = new Matrix4f[0];
    public ResourceDescriptor anim;
    public float animPos = 0.0f, animSpeed = 1.0f;
    public transient float animPosOld = -1.0f;
    public boolean animLoop = true;
    public float loopStart = 0.0f, loopEnd = 1.0f;
    public int editorColor = -1;
    public ShadowType castShadows = ShadowType.ALWAYS;
    public boolean RTTEnable;
    public byte visibilityFlags = VisibilityFlags.PLAY_MODE | VisibilityFlags.EDIT_MODE;
    public float poppetRenderScale = 1.0f;
    @GsonRevision(min=0x1f6,max=0x34c)
    public float parentDistanceFront, parentDistanceSide;
    public transient boolean isDirty = true;

    public PRenderMesh() {}
    public PRenderMesh(ResourceDescriptor mesh) {
        this.mesh = mesh;
    }

    public static void calculateBoneTransform(RAnimation animation, float position, Matrix4f[] transforms, Bone[] bones, Bone bone, Matrix4f parent) {
        int index = Bone.indexOf(bones, bone.animHash);
        int frame = (int) Math.floor(position * animation.getNumFrames());

        // frame = frameOverride;
        
        Matrix4f local = animation.getFrameMatrix(bone.animHash, frame, position);
        Matrix4f global = parent.mul(local, new Matrix4f());

        Vector3f translation = local.getTranslation(new Vector3f());
        Quaternionf rotation = local.getNormalizedRotation(new Quaternionf());
        Matrix4f childGlobal = new Matrix4f().identity().translationRotateScale(
            new Vector3f(translation.x, translation.y, translation.z),
            rotation,
            new Vector3f(1.0f, 1.0f, 1.0f)
        );
        childGlobal = parent.mul(childGlobal, new Matrix4f());

        Matrix4f inverse = global.mul(bone.invSkinPoseMatrix, new Matrix4f());
        transforms[index] = inverse;

        for (Bone child : bones) {
            if (child.parent == index)
                calculateBoneTransform(animation, position, transforms, bones, child, childGlobal);
        }

    }

    public static void skeletate(RSceneGraph scene, Thing[] boneThings, Bone[] bones, Bone bone, Thing parentOrRoot) {
        Thing root = boneThings[0];

        Thing boneThing = null;

        if (bone.parent != -1) {
            boneThing = scene.addThing();

            boneThing.groupHead = root;
            boneThing.parent = parentOrRoot;
    
            Matrix4f ppos = ((PPos)parentOrRoot.getPart(Part.POS)).getWorldPosition();
            Matrix4f pos = bone.getLocalTransform(bones);
    
            Matrix4f wpos = ppos.mul(pos, new Matrix4f());
    
            boneThing.setPart(Part.POS, new PPos(root, bone.animHash, wpos, pos));
        } else boneThing = parentOrRoot;

        int index = 0;
        for (Bone child : bones) {
            if (child == bone) boneThings[index] = boneThing;
            if (child.parent != -1 && bones[child.parent] == bone)
                skeletate(scene, boneThings, bones, child, boneThing);
            index++;
        }
    }

    public void setupBoneThings(RSceneGraph scene, Thing root, Matrix4f transform, Bone[] bones) {
        Thing[] boneThings = new Thing[bones.length];
        boneThings[0] = root;

        transform = transform.rotate((float) Math.toRadians(-90.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Matrix4f());

        PPos pos = root.getPart(Part.POS);
        pos.animHash = bones[0].animHash;
        pos.worldPosition = transform.mul(bones[0].skinPoseMatrix, new Matrix4f());
        pos.localPosition = new Matrix4f(pos.worldPosition);

        for (int i = 0; i < bones.length; ++i) {
            Bone bone = bones[i];
            if (bone.parent != -1) continue;

            Thing thing = root;
            if (i != 0) {
                Matrix4f wpos = transform.mul(bone.skinPoseMatrix, new Matrix4f());
                thing = scene.addThing();
                thing.setPart(Part.POS, new PPos(root, bone.animHash, wpos, new Matrix4f(wpos)));
                thing.groupHead = root;
                boneThings[i] = thing;
            }

            skeletate(scene, boneThings, bones, bone, thing);
        }

        ((PRenderMesh)root.getPart(Part.RENDER_MESH)).boneThings = boneThings;
    }

    public void update(Thing thing, Matrix4f wpos, int[] regionIDsToHide) {
        if (this.mesh == null) return;

        if (this.instance == null) {
            Mesh glMesh = Mesh.getSkinnedMesh(this.mesh);
            if (glMesh == null) return;
            this.instance = new MeshInstance(glMesh);
        }

        if (this.boneThings == null || this.boneThings.length == 0)
            this.setupBoneThings(RenderSystem.getSceneGraph(), thing, wpos, this.instance.mesh.getBones());
        
        this.recalculateStaticInverses(thing, this.instance.mesh.getBones(), wpos);

        if (this.anim != null) {
            // No need to recalculate if we haven't changed animation position
            if (this.animPos == this.animPosOld || (this.animPos >= 1.0f && !this.animLoop)) {
                this.instance.draw(this.boneModels, Colors.RGBA32.fromARGB(this.editorColor), regionIDsToHide);
                return;
            }

            if (!DISABLED_ANIMATIONS.contains(this.anim)) {
                RAnimation animation = ANIMATIONS.get(this.anim);
                if (animation == null) {
                    byte[] animData = ResourceSystem.extract(this.anim);
                    if (animData != null) {
                        Resource resource = new Resource(animData);
                        if (resource.getRevision().getVersion() < 0x378) {
                            animation = new Resource(animData).loadResource(RAnimation.class);
                            ANIMATIONS.put(this.anim, animation);
                        } else DISABLED_ANIMATIONS.add(this.anim);
                    }
                }
    
                if (animation != null) {
                    this.animPosOld = this.animPos;
                    this.animPos += (((animation.getFPS() * RenderSystem.getDeltaTime()) / animation.getNumFrames()) * this.animSpeed);
                    
                    if (this.animLoop) {
                        float x = this.animPos, y = this.loopEnd;
    
                        // fmod(x, y)
                        float q = x / y, t = (float) (q < 0 ? -Math.floor(-q) : Math.floor(q));
                        this.animPos = (x - t * y);
                    }
    
                    for (Thing boneThing : this.boneThings) {
                        PPos bonePos = boneThing.getPart(Part.POS);
                        Bone bone = Bone.getByHash(this.instance.mesh.getBones(), bonePos.animHash);
                        if (bone == null || bone.parent != -1) continue;
                        Matrix4f pos = bonePos.worldPosition.mul(bone.invSkinPoseMatrix, new Matrix4f());
                        calculateBoneTransform(animation, this.animPos, this.boneModels, this.instance.mesh.getBones(), bone, pos);
                    }
                }
            }
        }

        this.instance.draw(this.boneModels, Colors.RGBA32.fromARGB(this.editorColor), regionIDsToHide);
    }

    private void recalculateStaticInverses(Thing thing, Bone[] bones, Matrix4f wpos) {
        if (!this.isDirty) return;

        this.boneModels = new Matrix4f[bones.length];
        this.boneModels[0] = wpos.mul(bones[0].invSkinPoseMatrix, new Matrix4f());

        // If a bone doesn't exist (usually with preloaded levels)
        // then use the root bone's inverses.
        for (int i = 0; i < this.boneModels.length; ++i)
            this.boneModels[i] = this.boneModels[0];

        for (Thing bone : this.boneThings) {
            if (bone == null || bone == thing || !bone.hasPart(Part.POS)) continue;
            PPos pos = bone.getPart(Part.POS);

            // Can't guarantee that the bone things are in-order,
            // maybe I should re-sort them on level load, just in case?
            int index = Bone.indexOf(bones, pos.animHash);

            if (index == -1) continue;

            this.boneModels[index] = pos.getWorldPosition().mul(bones[index].invSkinPoseMatrix, new Matrix4f());
        }

        this.isDirty = false;
    }
    
    @SuppressWarnings("unchecked")
    @Override public PRenderMesh serialize(Serializer serializer, Serializable structure) {
        PRenderMesh mesh = (structure == null) ? new PRenderMesh() : (PRenderMesh) structure;
        
        int version = serializer.getRevision().getVersion();

        mesh.mesh = serializer.resource(mesh.mesh, ResourceType.MESH);

        mesh.boneThings = serializer.thingarray(mesh.boneThings);
        
        mesh.anim = serializer.resource(mesh.anim, ResourceType.ANIMATION);
        mesh.animPos = serializer.f32(mesh.animPos);
        mesh.animSpeed = serializer.f32(mesh.animSpeed);
        mesh.animLoop = serializer.bool(mesh.animLoop);
        mesh.loopStart = serializer.f32(mesh.loopStart);
        mesh.loopEnd = serializer.f32(mesh.loopEnd);
        
        if (version > 0x31a) mesh.editorColor = serializer.i32(mesh.editorColor);
        else {
            if (serializer.isWriting())
                serializer.getOutput().v4(Colors.RGBA32.fromARGB(mesh.editorColor));
            else {
                Vector4f color = serializer.getInput().v4();
                mesh.editorColor = Colors.RGBA32.getARGB(color);
            }
        }
        
        mesh.castShadows = serializer.enum8(mesh.castShadows);
        mesh.RTTEnable = serializer.bool(mesh.RTTEnable);
        
        if (version > 0x2e2)
            mesh.visibilityFlags = serializer.i8(mesh.visibilityFlags);
        else {
            if (serializer.isWriting())
                serializer.getOutput().bool((mesh.visibilityFlags & VisibilityFlags.PLAY_MODE) != 0);
            else {
                mesh.visibilityFlags = VisibilityFlags.EDIT_MODE;
                if (serializer.getInput().bool())
                    mesh.visibilityFlags |=  VisibilityFlags.PLAY_MODE;
            }
        }
        
        mesh.poppetRenderScale = serializer.f32(mesh.poppetRenderScale);
        
        if (version > 0x1f5 && version < 0x34d) {
            mesh.parentDistanceFront = serializer.f32(mesh.parentDistanceFront);
            mesh.parentDistanceSide = serializer.f32(mesh.parentDistanceFront);
        }

        return mesh;
    }

    @Override public int getAllocatedSize() {
        int size = PRenderMesh.BASE_ALLOCATION_SIZE;
        if (this.boneThings != null) size += (this.boneThings.length * 0x4);
        return size;
    }
}
