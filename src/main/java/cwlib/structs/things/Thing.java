package cwlib.structs.things;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.Branch;
import cwlib.enums.Part;
import cwlib.enums.PartHistory;
import cwlib.enums.Revisions;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.ThingSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RAnimation;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.mesh.Bone;
import cwlib.structs.things.parts.PGeneratedMesh;
import cwlib.structs.things.parts.PJoint;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PShape;
import cwlib.types.Resource;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.Bytes;
import cwlib.util.Colors;
import toolkit.gl.CraftworldRenderer;
import toolkit.gl.Mesh;
import toolkit.gl.StaticMesh;
import toolkit.windows.Toolkit;

/**
 * Represents an object in the game world.
 */
@JsonAdapter(ThingSerializer.class)
public class Thing implements Serializable {
    public static boolean SERIALIZE_WORLD_THING = true;
    public static int MAX_PARTS_REVISION = PartHistory.STREAMING_HINT;

    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public int UID = 1;
    public Thing world;
    public Thing parent;
    public Thing groupHead;
    public Thing oldEmitter;

    public short createdBy = -1, changedBy = -1;
    public boolean isStamping;
    public GUID planGUID;
    public boolean hidden;
    public short flags;
    public byte extraFlags;

    public boolean isDirty = true;
    public Matrix4f[] matrices;


    private Serializable[] parts = new Serializable[0x3f];

    public Thing(){};
    public Thing(int UID) { this.UID = UID; }
    
    @SuppressWarnings("unchecked")
    @Override public Thing serialize(Serializer serializer, Serializable structure) {
        Thing thing = (structure == null) ? new Thing() : (Thing) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        // Test serialization marker.
        if (version >= Revisions.THING_TEST_MARKER || revision.has(Branch.LEERDAMMER, Revisions.LD_TEST_MARKER)) {
            serializer.log("TEST_SERIALISATION_MARKER");
            serializer.u8(0xAA);
        }

        if (version < 0x1fd) {
            if (serializer.isWriting())
                serializer.reference(SERIALIZE_WORLD_THING ? thing.world : null, Thing.class);
            else
                thing.world = serializer.reference(thing.world, Thing.class);
        }
        if (version < 0x27f) {
            thing.parent = serializer.reference(thing.parent, Thing.class);
            thing.UID = serializer.i32(thing.UID);
        } else {
            thing.UID = serializer.i32(thing.UID);
            thing.parent = serializer.reference(thing.parent, Thing.class);
        }

        thing.groupHead = serializer.reference(thing.groupHead, Thing.class);

        if (version >= 0x1c7)
            thing.oldEmitter = serializer.reference(thing.oldEmitter, Thing.class);

        if (version >= 0x1a6 && version < 0x1bc)
            serializer.array(null, PJoint.class, true);
        
        if (version >= 0x214) {
            thing.createdBy = serializer.i16(thing.createdBy);
            thing.changedBy = serializer.i16(thing.changedBy);
        }

        if (version < 0x341) {
            if (version > 0x21a) 
                thing.isStamping = serializer.bool(thing.isStamping);
            if (version >= 0x254)
                thing.planGUID = serializer.guid(thing.planGUID);
            if (version >= 0x2f2)
                thing.hidden = serializer.bool(thing.hidden);
        } else {
            if (version >= 0x254)
                thing.planGUID = serializer.guid(thing.planGUID);
            if (version >= 0x341) {
                if (revision.has(Branch.DOUBLE11, 0x62))
                    thing.flags = serializer.i16(thing.flags);
                else
                    thing.flags = serializer.i8((byte) thing.flags);
            }
            if (subVersion >= 0x110)
                thing.extraFlags = serializer.i8(thing.extraFlags);
        }

        boolean isCompressed = (version >= 0x297 || revision.has(Branch.LEERDAMMER, Revisions.LD_RESOURCES));
        
        int partsRevision = PartHistory.STREAMING_HINT;
        long flags = -1;

        if (serializer.isWriting()) {
            serializer.log("GENERATING FLAGS");
            Part lastPart = null;
            if (isCompressed) flags = 0;
            for (Part part : Part.values()) {
                int index = part.getIndex();
                if (version >= 0x13c && (index >= 0x36 && index <= 0x3c)) continue;
                if (version >= 0x18c && index == 0x3d) continue;
                if (subVersion >= 0x107 && index == 0x3e) continue;
                else if (index == 0x3e) {
                    if (thing.parts[index] != null) {
                        flags |= (1l << 0x29);
                        lastPart = part;
                    }
                    continue;
                }

                if (thing.parts[index] != null) {
                    // Offset due to PCreatorAnim
                    if (subVersion < 0x107 && index > 0x28) index++; 

                    flags |= (1l << index);

                    lastPart = part;
                }
            }
            partsRevision = (lastPart == null) ? 0 : lastPart.getVersion();
        }

        if (serializer.isWriting()) {
            if (partsRevision > Thing.MAX_PARTS_REVISION)
                partsRevision = Thing.MAX_PARTS_REVISION;
        }
        
        partsRevision = serializer.s32(partsRevision);
        if (isCompressed) {
            // serializer.log("FLAGS");
            flags = serializer.i64(flags);
        }

        // I have no idea why they did this
        if (version == 0x13c) partsRevision += 7;
        
        Part[] parts = Part.fromFlags(revision.getHead(), flags, partsRevision);
        if (!ResourceSystem.DISABLE_LOGS)
            serializer.log(Arrays.toString(parts));

        for (Part part : parts) {
            serializer.log(part.name() + " [START]");
            if (!part.serialize(thing.parts, partsRevision, flags, serializer)) {
                serializer.log(part.name() + " FAILED");
                throw new SerializationException(part.name() + " failed to serialize!");
            }
            serializer.log(part.name() + " [END]");
        }

        serializer.log("THING " + Bytes.toHex(thing.UID) + " [END]");
        
        return thing;
    }

    public static void calculateBoneTransform(RAnimation animation, float position, Matrix4f[] transforms, Bone[] bones, Bone bone, Matrix4f parent) {
        int index = Bone.indexOf(bones, bone.animHash);
        int frame = (int) Math.floor(position * animation.getNumFrames());

        Matrix4f local = animation.getFrameMatrix(bone.animHash, frame, position);
        Matrix4f global = parent.mul(local, new Matrix4f());

        Matrix4f inverse = global.mul(bone.invSkinPoseMatrix, new Matrix4f());
        transforms[index] = inverse;

        for (Bone child : bones) {
            if (child.parent == index)
                calculateBoneTransform(animation, position, transforms, bones, child, global);
        }

    }

    public static void skeletate(Thing[] boneThings, Bone[] bones, Bone bone, Thing parentOrRoot) {
        Thing root = boneThings[0];

        Thing boneThing = null;

        if (bone.parent != -1) {
            boneThing = new Thing(++Toolkit.renderer.getWorld().thingUIDCounter);

            boneThing.groupHead = root;
            boneThing.parent = parentOrRoot;
    
            Matrix4f ppos = ((PPos)parentOrRoot.getPart(Part.POS)).getWorldPosition();
            Matrix4f pos = bone.getLocalTransform(bones);
    
            Matrix4f wpos = ppos.mul(pos, new Matrix4f());
    
            boneThing.setPart(Part.POS, new PPos(root, bone.animHash, wpos));
        } else boneThing = parentOrRoot;

        int index = 0;
        for (Bone child : bones) {
            if (child == bone) boneThings[index] = boneThing;
            if (child.parent != -1 && bones[child.parent] == bone)
                skeletate(boneThings, bones, child, boneThing);
            index++;
        }
    }

    public void computeBoneThings(Thing root, Matrix4f transform, Bone[] bones) {
        Thing[] boneThings = new Thing[bones.length];
        boneThings[0] = root;

        transform = transform.rotate((float) Math.toRadians(-90.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Matrix4f());

        PPos pos = root.getPart(Part.POS);
        pos.animHash = bones[0].animHash;
        pos.worldPosition = transform.mul(bones[0].skinPoseMatrix, new Matrix4f());

        for (int i = 0; i < bones.length; ++i) {
            Bone bone = bones[i];
            if (bone.parent != -1) continue;

            Thing thing = root;
            if (i != 0) {
                Matrix4f wpos = transform.mul(bone.skinPoseMatrix, new Matrix4f());
                thing = new Thing(++Toolkit.renderer.getWorld().thingUIDCounter);
                thing.setPart(Part.POS, new PPos(root, bone.animHash, wpos));
                thing.groupHead = root;
                boneThings[i] = thing;
            }

            skeletate(boneThings, bones, bone, thing);
        }

        ((PRenderMesh)root.getPart(Part.RENDER_MESH)).boneThings = boneThings;
    }

    static HashMap<ResourceDescriptor, RAnimation> ANIMATIONS = new HashMap<>();

    float truncate(float x) {
        return (float) (x < 0 ? -Math.floor(-x) : Math.floor(x));
    }

    float fmod(float x, float y) {
        return x - truncate(x / y) * y;
    }

    public void render(PLevelSettings lighting) {
        PPos pos = this.getPart(Part.POS);
        if (pos == null) return;


        PRenderMesh mesh = this.getPart(Part.RENDER_MESH);

        if (mesh != null && mesh.mesh != null) {
            Mesh glMesh = Mesh.get(mesh.mesh);
            if (glMesh == null) return;
    
            if (this.isDirty) {
                Matrix4f[] joints = new Matrix4f[glMesh.bones.length];
                joints[0] = pos.getWorldPosition().mul(glMesh.bones[0].invSkinPoseMatrix, new Matrix4f());
                for (int i = 0; i < joints.length; ++i) joints[i] = joints[0];    

                if (mesh.boneThings != null) {
                    if (mesh.boneThings.length == 0) {
                        computeBoneThings(this, pos.worldPosition, glMesh.bones);

                        // Reset default inverses to match new calculated world position
                        joints[0] = pos.getWorldPosition().mul(glMesh.bones[0].invSkinPoseMatrix, new Matrix4f());
                        for (int i = 0; i < joints.length; ++i) joints[i] = joints[0];    
                    }

                    for (Thing thing : mesh.boneThings) {
                        if (thing == null || thing == this || !thing.hasPart(Part.POS)) continue;
        
                        PPos bonePos = thing.getPart(Part.POS);
                        int index = Bone.indexOf(glMesh.bones, bonePos.animHash);
                        if (index == -1) continue;
        
                        joints[index] = bonePos.getWorldPosition().mul(glMesh.bones[index].invSkinPoseMatrix, new Matrix4f());
                    }
                }
                
                this.matrices = joints;
                this.isDirty = false;
            }

            if (mesh.anim != null) {

                RAnimation animation = ANIMATIONS.get(mesh.anim);
                if (animation == null) {
                    byte[] animData = ResourceSystem.extract(mesh.anim);
                    if (animData != null) {
                        animation = new Resource(animData).loadResource(RAnimation.class);
                        if (animation != null && animation.locators != null && animation.locators.length != 0)
                            System.out.println("[LOCATOR] Loading " + mesh.anim.toString());
                        ANIMATIONS.put(mesh.anim, animation);
                    }
                }
                
                if (animation != null) {
                    mesh.animPos += (((animation.getFPS() * CraftworldRenderer.DELTA_TIME) / animation.getNumFrames()) * mesh.animSpeed);
                    mesh.animPos = fmod(mesh.animPos, 1.0f);

                    Matrix4f[] transforms = new Matrix4f[glMesh.bones.length];
                    for (Thing thing : mesh.boneThings) {
                        PPos bonePos = thing.getPart(Part.POS);
                        Bone bone = Bone.getByHash(glMesh.bones, bonePos.animHash);
                        if (bone.parent != -1) continue;
                        Matrix4f wpos = bonePos.worldPosition.mul(bone.invSkinPoseMatrix, new Matrix4f());
                        calculateBoneTransform(animation, mesh.animPos, transforms, glMesh.bones, bone, wpos);
                    }

                    this.matrices = transforms;
                }
            }
    
            glMesh.draw(lighting, this.matrices, Colors.RGBA32.fromARGB(mesh.editorColor));
        }

        PShape shape = this.getPart(Part.SHAPE);
        PGeneratedMesh generatedMesh = this.getPart(Part.GENERATED_MESH);
        if (shape != null && generatedMesh != null) {
            if (shape.glMesh == null)
                shape.glMesh = new Mesh(generatedMesh, shape);
            shape.glMesh.draw(lighting, new Matrix4f[] { pos.getWorldPosition() }, Colors.RGBA32.fromARGB(shape.color));
        }

        PLevelSettings settings = this.getPart(Part.LEVEL_SETTINGS);
        if (settings != null && settings.backdropMesh != null) {
            StaticMesh glMesh = StaticMesh.get(settings.backdropMesh);
            if (glMesh != null)
                glMesh.draw(settings);
        }
        
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> T getPart(Part part) { return (T) this.parts[part.getIndex()]; }
    public <T extends Serializable> void setPart(Part part, T value) { this.parts[part.getIndex()] = value; }
    public boolean hasPart(Part part) { return this.parts[part.getIndex()] != null; }

    @Override public int getAllocatedSize() {  return BASE_ALLOCATION_SIZE;  }
}
