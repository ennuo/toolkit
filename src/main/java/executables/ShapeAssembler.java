package executables;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.InventoryObjectType;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.resources.RMesh;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.MeshShapeInfo;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PBody;
import cwlib.structs.things.parts.PGroup;
import cwlib.structs.things.parts.PJoint;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PShape;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;

public class ShapeAssembler {
    static int UID = 0;

    public static void skeletate(Thing[] boneThings, Bone[] bones, Bone bone, Thing parentOrRoot) {
        Thing root = boneThings[0];

        Thing boneThing = null;

        if (bone.parent != -1) {
            boneThing = new Thing(++UID);

            boneThing.groupHead = root;
            boneThing.parent = parentOrRoot;
    
            Matrix4f ppos = ((PPos)parentOrRoot.getPart(Part.POS)).getWorldPosition();
            Matrix4f pos = bone.getLocalTransform(bones);

            Matrix4f wpos;
            if (bones[bone.parent].flags == 2)
                wpos = pos;
            else
                wpos = ppos.mul(pos, new Matrix4f());
    
            boneThing.setPart(Part.POS, new PPos(root, bone.animHash, wpos, pos));
        } else boneThing = parentOrRoot;

        if (bone.shapeVerts.length != 0) {
            MeshShapeInfo info = bone.shapeInfos[0];
            Vector3f[] vertices = new Vector3f[info.numVerts];
            for (int v = 0; v < vertices.length; ++v) {
                Vector4f vert = bone.shapeVerts[v].localPos;
                vertices[v] = new Vector3f(vert.x, vert.y, vert.z);
            }

            PShape shape = new PShape(vertices);
            shape.massDepth = 
                (float) (Math.round(((Math.abs(bone.shapeMaxZ) + Math.abs(bone.shapeMinZ)) / 100.0) * 10.0) / 10.0);
            shape.thickness = (float) (Math.round((Math.abs(bone.shapeMaxZ) + Math.abs(bone.shapeMinZ)) / 2.0));
            
            boneThing.setPart(Part.SHAPE, shape);
        }

        int index = 0;
        for (Bone child : bones) {
            if (child == bone) boneThings[index] = boneThing;
            if (child.parent != -1 && bones[child.parent] == bone)
                skeletate(boneThings, bones, child, boneThing);
            index++;
        }
    }


    public static Thing[] computeBoneThings(Thing root, Matrix4f transform, Bone[] bones) {
        Thing[] boneThings = new Thing[bones.length];
        boneThings[0] = root;

        transform = transform.rotate((float) Math.toRadians(-90.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Matrix4f());

        PPos pos = root.getPart(Part.POS);
        pos.animHash = bones[0].animHash;
        pos.worldPosition = transform.mul(bones[0].skinPoseMatrix, new Matrix4f());
        pos.localPosition = pos.worldPosition;

        for (int i = 0; i < bones.length; ++i) {
            Bone bone = bones[i];
            if (bone.parent != -1) continue;

            Thing thing = root;
            if (i != 0) {
                Matrix4f wpos = transform.mul(bone.skinPoseMatrix, new Matrix4f());
                // Matrix4f localPos = bone.skinPoseMatrix.mul(bones[0].invSkinPoseMatrix, new Matrix4f());
                thing = new Thing(++UID);

                thing.setPart(Part.POS, new PPos(root, bone.animHash, wpos, wpos));
                // thing.setPart(Part.BODY, new PBody());
                // thing.setPart(Part.GROUP, new PGroup());

                thing.groupHead = root;
                thing.parent = null;

                boneThings[i] = thing;
            }

            skeletate(boneThings, bones, bone, thing);
        }

        ((PRenderMesh)root.getPart(Part.RENDER_MESH)).boneThings = boneThings;

        return boneThings;
    }


    public static void main(String[] args) {
        ResourceSystem.DISABLE_LOGS = true;

        // ResourceSystem.DISABLE_LOGS = true;
        if (args.length < 3) {
            System.out.println("java -jar sass.jar <model> <descriptor> <output>");
            return;
        }

        if (!new File(args[0]).exists()) {
            System.err.println("Model file doesn't exist!");
            return;
        }

        Resource resource = null;
        RMesh mesh = null;
        try {
            resource = new Resource(args[0]);
            mesh = resource.loadResource(RMesh.class);
        } catch (Exception ex) {
            System.out.println("There was an error processing this resource!");
            System.out.println(ex.getMessage());
        }

        int rootCount = 0;
        for (Bone bone : mesh.getBones()) {
            if (bone.parent == -1 && bone.flags != 2)
                rootCount++;
        }
        
        System.out.println(rootCount);

        ResourceDescriptor descriptor = new ResourceDescriptor(args[1], ResourceType.MESH);

        Thing root = new Thing(++UID);
        root.setPart(Part.POS, new PPos(root, 0, new Matrix4f().identity()));
        root.setPart(Part.BODY, new PBody());
        root.setPart(Part.GROUP, new PGroup());
        root.setPart(Part.RENDER_MESH, new PRenderMesh(descriptor));


        Bone[] bones = mesh.getBones();
        Thing[] boneThings = computeBoneThings(root, new Matrix4f().identity(), bones);

        RPlan plan = new RPlan();
        plan.inventoryData = new InventoryItemDetails();
        plan.inventoryData.type = EnumSet.of(InventoryObjectType.READYMADE);
        plan.inventoryData.userCreatedDetails = new UserCreatedDetails("Generated Model", "Maybe you should replace this descirption");
        
        plan.revision = new Revision(0x272, 0x4c44, 0x0008);
        plan.compressionFlags = 0x0;

        ArrayList<Thing> things = new ArrayList<>();
        for (Thing thing : boneThings)
            things.add(thing);

        for (int i = 1; i < boneThings.length; ++i) {
            Thing jointThing = new Thing(++UID);
            PJoint joint = new PJoint(boneThings[bones[i].parent], boneThings[i]);
            jointThing.setPart(Part.JOINT, joint);
            jointThing.groupHead = boneThings[0];
            things.add(jointThing);
        }


        plan.setThings(things.toArray(Thing[]::new));

        // plan.setThings(boneThings);

        FileIO.write(Resource.compress(plan.build()), args[2]);
    }
}
