package ennuo.toolkit.functions;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Skin;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Compressor;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.memory.Vector3f;
import ennuo.craftworld.resources.enums.Crater;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.ProfileItem;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.resources.structs.mesh.Bone;
import ennuo.craftworld.resources.structs.mesh.ImplicitEllipsoid;
import ennuo.craftworld.resources.structs.mesh.ImplicitPlane;
import ennuo.craftworld.resources.structs.mesh.SoftbodyCluster;
import ennuo.craftworld.resources.structs.mesh.SoftbodySpring;
import ennuo.craftworld.resources.structs.mesh.SoftbodyVertEquivalence;
import ennuo.craftworld.things.InventoryItem;
import ennuo.craftworld.things.Serializer;
import ennuo.craftworld.types.BigProfile;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Matrix4x4;
import ennuo.craftworld.types.Mod;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.utilities.Globals.WorkspaceType;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class DebugCallbacks {
    
    public static double[] convertFloatsToDoubles(float[] input)
    {
    if (input == null)
    {
        return null; // Or throw an exception - your choice
    }
    double[] output = new double[input.length];
    for (int i = 0; i < input.length; i++)
    {
        output[i] = input[i];
    }
    return output;
    }
    
    public static float[] convertDoublesToFloats(double[] input)
    {
    if (input == null)
    {
        return null; // Or throw an exception - your choice
    }
    float[] output = new float[input.length];
    for (int i = 0; i < input.length; i++)
    {
        output[i] = (float) input[i];
    }
    return output;
    }
    
    
    public static float magnitude4(float[] vector) {
        return vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2] + vector[3] * vector[3];
    }
    
    public static float[] getScale(Matrix4x4 matrix) {
        float[] scale = new float[3];
        float[] mat = convertDoublesToFloats(matrix.get());
        
        scale[0] = magnitude4(new float[] { mat[0], mat[1], mat[2], mat[3] } );
        scale[1] = magnitude4(new float[] { mat[4], mat[5], mat[6], mat[7] } );
        scale[2] = magnitude4(new float[] { mat[8], mat[9], mat[10], mat[11] });
        
        return scale;
    }
    
    public static float[] getQuaternion(Matrix4x4 matrix) {
        
        double[] mat = matrix.get();
        
        double az, ay, ax;
        double ai, aj, ak;
        double si, sj, sk;
        double ci, cj, ck;
        double cy, cc, cs, sc, ss;
   
    
        cy = Math.sqrt( mat[0] * mat[0] + mat[5] * mat[5] );

        if( cy > (1E-5) )
        {
            ax = Math.atan2( mat[9], mat[10] );
            ay = Math.atan2( -mat[8], cy );
            az = Math.atan2( mat[4], mat[0] );
        }
        else
        {
            ax = Math.atan2( -mat[6], mat[5] );
            ay = Math.atan2( -mat[8], cy );
            az = 0.0;
        }

        ai = ax / 2.0;
        aj = ay / 2.0;
        ak = az / 2.0;

        ci = Math.cos( ai );
        si = Math.sin( ai );
        cj = Math.cos( aj );
        sj = Math.sin( aj );
        ck = Math.cos( ak );
        sk = Math.sin( ak );
        cc = ci * ck;
        cs = ci * sk;
        sc = si * ck;
        ss = si * sk;

        float[] quat = new float[4];

        quat[0] = (float)( cj * sc - sj * cs);
        quat[1] = (float)( cj * ss + sj * cc);
        quat[2] = (float)( cj * cs - sj * sc);
        quat[3] = (float) ( cj * cc + sj * ss);

        return quat;
    }
    
    public static void toLocal(List<Node> nodes, Node node) {
        Matrix4x4 parentMatrix = Matrix4x4.inverse(new Matrix4x4(convertFloatsToDoubles(node.getMatrix())));
        List<Integer> children = node.getChildren();
        if (children == null) return;
        for (Integer child : children) {
            Node childNode = nodes.get(child);
            toLocal(nodes, childNode);
            Matrix4x4 childMatrix = new Matrix4x4(convertFloatsToDoubles(childNode.getMatrix()));
            childMatrix.multiply(parentMatrix);
            double[] mat = childMatrix.get();
            childNode.setMatrix(convertDoublesToFloats(mat));
        }
    }
    
    public static void exportGLTF(ennuo.craftworld.resources.Mesh mesh) {
        
        try {
            File file = Toolkit.instance.fileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + ".glb",
            "glb",
            "glTF Binary (.GLB)",
            true
        );

        if (file == null) return;
            
        
        short[] triangles = mesh.triangulate();
        int morphSize = (mesh.morphs == null) ? 0 : mesh.morphs.length;
        Output output = new Output( (mesh.vertices.length * 0x24) + (triangles.length * 2) + (mesh.attributeCount * mesh.uvCount * 8) + (morphSize * mesh.vertices.length * 0xC) + (mesh.bones.length * 0x40));
        for (Vector3f vertex : mesh.vertices) {
            output.float32le(vertex.x);
            output.float32le(vertex.y);
            output.float32le(vertex.z);
        }
        for (short triangle : triangles)
            output.int16le(triangle);
        int uvStart = output.offset;
        for (int i = 0; i < mesh.attributeCount; ++i) {
            for (int j = 0; j < mesh.uvCount; ++j) {
                output.float32le(mesh.attributes[j][i].x);
                output.float32le(mesh.attributes[j][i].y);
            }
        }
        int morphStart = output.offset;
        if (mesh.morphs != null) {
            for (int i = 0; i < mesh.morphs.length; ++i) {
                for (int j = 0; j < mesh.vertices.length; ++j) {
                    output.float32le(mesh.morphs[i].vertices[j].x);
                    output.float32le(mesh.morphs[i].vertices[j].y);
                    output.float32le(mesh.morphs[i].vertices[j].z);
                }
            }
        }
        
        int matrixStart = output.offset;
        for (int i = 0; i < mesh.bones.length; ++i) {
            for (int j = 0; j < 15; ++j)
                output.float32le(mesh.bones[i].invSkinPoseMatrix[j]);      
        }
        
        int jointStart = output.offset;
        for (int i = 0; i < mesh.weights.length; ++i) {
            output.int8(mesh.weights[i].boneIndex);
            output.int8(0); output.int8(0);
            output.int8(0);
        }
        
        int weightStart = output.offset;
        for (int i = 0; i < mesh.weights.length; ++i) {
            output.int8(mesh.weights[i].weight);
            output.int8(0); output.int8(0);
            output.int8(0);
        }
        output.shrinkToFit();
        
        Asset asset = new Asset();
        asset.setGenerator("CRAFTWORLD");
        asset.setVersion("2.0");
        
        
        Material material = new Material();
        material.setName("DIFFUSE");
        MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
        pbr.setBaseColorFactor(new float[] { 1.0f, 1.0f, 1.0f, 1.0f });
        material.setPbrMetallicRoughness(pbr);
        
        
        
        GlTF gltf = new GlTF();
        
        gltf.setAsset(asset);
        gltf.addMaterials(material);
        
        Buffer buffer = new Buffer();
        buffer.setByteLength(output.buffer.length);
        gltf.addBuffers(buffer);
        
        BufferView vertexView = new BufferView();
        vertexView.setBuffer(0);
        vertexView.setByteOffset(0);
        vertexView.setByteLength(mesh.vertices.length * 0xC);
        
        BufferView faceView = new BufferView();
        faceView.setBuffer(0);
        faceView.setByteOffset(mesh.vertices.length * 0xC);
        faceView.setByteLength(triangles.length * 2);
        
        gltf.addBufferViews(vertexView); gltf.addBufferViews(faceView);
        
        if (mesh.attributeCount != 0) {
            BufferView uvView = new BufferView();
            uvView.setBuffer(0);
            uvView.setByteOffset(uvStart);
            uvView.setByteLength(mesh.attributeCount * 0x8 * mesh.uvCount);
            gltf.addBufferViews(uvView);
        }
        
        if (mesh.morphs != null && mesh.morphs.length != 0) {
            BufferView morphView = new BufferView();
            morphView.setBuffer(0);
            morphView.setByteOffset(morphStart);
            morphView.setByteLength((mesh.morphs.length * mesh.vertices.length * 0xC));
            gltf.addBufferViews(morphView);
        }
        
        Accessor vertexAccessor = new Accessor();
        vertexAccessor.setBufferView(0);
        vertexAccessor.setByteOffset(0);
        vertexAccessor.setComponentType(5126);
        vertexAccessor.setType("VEC3");
        vertexAccessor.setCount(mesh.vertices.length);
        
        Accessor faceAccessor = new Accessor();
        faceAccessor.setBufferView(1);
        faceAccessor.setByteOffset(0);
        faceAccessor.setComponentType(5123);
        faceAccessor.setType("SCALAR");
        faceAccessor.setCount(triangles.length);
        
        gltf.addAccessors(vertexAccessor); gltf.addAccessors(faceAccessor);
        
        Mesh gltfMesh = new Mesh();
        gltfMesh.setName("Mesh");
        
        MeshPrimitive primitive = new MeshPrimitive();
        
        primitive.addAttributes("POSITION", 0);
        
        int currentView = 2;
        
        for (int i = 0; i < mesh.attributeCount; ++i, currentView++) {
            Accessor accessor = new Accessor();
            accessor.setBufferView(2);
            accessor.setByteOffset((0x8 * mesh.uvCount) * i);
            accessor.setComponentType(5126);
            accessor.setType("VEC2");
            accessor.setCount(mesh.uvCount);

            gltf.addAccessors(accessor);
            
            primitive.addAttributes("TEXCOORD_" + i, currentView);
        }
        
        if (mesh.morphs != null) {
            for (int i = 0; i < mesh.morphs.length; ++i, currentView++) {
                Accessor accessor = new Accessor();
                accessor.setBufferView(3);
                accessor.setByteOffset((0xC * mesh.vertices.length) * i);
                accessor.setComponentType(5126);
                accessor.setType("VEC3");
                accessor.setCount(mesh.vertices.length);

                gltf.addAccessors(accessor);

                HashMap<String, Integer> target = new HashMap<String, Integer>();
                target.put("POSITION", currentView);
                primitive.addTargets(target);
            }
        }
        
        primitive.setIndices(1);
        primitive.setMaterial(0);
        primitive.setMode(4);
        
        Node root = new Node();
        root.setName("Armature");
        
        
        Node meshNode = new Node();
        meshNode.setName("Mesh");
        meshNode.setMesh(0);
        meshNode.setSkin(0);
        
        root.addChildren(1);
        root.addChildren(2);
        
        gltf.addNodes(root);
        gltf.addNodes(meshNode);
        
        for (int i = 0; i < mesh.bones.length; ++i) {
            Node bone = new Node();
            bone.setName(mesh.bones[i].name);
            System.out.println(i + ": " + mesh.bones[i].name);
            bone.setMatrix(mesh.bones[i].skinPoseMatrix);
            gltf.addNodes(bone);
        }
        
        for (int i = 0; i < mesh.bones.length; ++i) {
            Bone bone = mesh.bones[i];
            if (bone.parent >= 0) {
                if (bone.parent == i) continue;
                gltf.getNodes().get(bone.parent + 2).addChildren(i + 2);
            }
        }
        
        Skin skin = new Skin();
        for (int i = 0; i < mesh.bones.length; ++i)
            skin.addJoints(i + 2);
        
        int matrixBuffer = 3;
        if (mesh.morphs != null && mesh.morphs.length != 0)
            matrixBuffer++;
        int matrixView = currentView;
        int jointBuffer = matrixBuffer + 1;
        int jointView = matrixView + 1;
        int weightBuffer = matrixBuffer + 2;
        int weightView = matrixView + 2;
        
        
        BufferView matrixBufferView = new BufferView();
        matrixBufferView.setBuffer(0);
        matrixBufferView.setByteOffset(matrixStart);
        matrixBufferView.setByteLength(mesh.bones.length * 0x40);
        
        Accessor matrixAccessor = new Accessor();
        matrixAccessor.setBufferView(matrixBuffer);
        matrixAccessor.setByteOffset(0);
        matrixAccessor.setComponentType(5126);
        matrixAccessor.setType("MAT4");
        matrixAccessor.setCount(mesh.bones.length);

        gltf.addAccessors(matrixAccessor);
        
        gltf.addBufferViews(matrixBufferView);
        
        BufferView jointBufferView = new BufferView();
        jointBufferView.setBuffer(0);
        jointBufferView.setByteOffset(jointStart);
        jointBufferView.setByteLength(mesh.vertices.length * 0x4);
        
        Accessor jointAccessor = new Accessor();
        jointAccessor.setBufferView(jointBuffer);
        jointAccessor.setByteOffset(0);
        jointAccessor.setComponentType(5121);
        jointAccessor.setType("VEC4");
        jointAccessor.setCount(mesh.vertices.length);

        gltf.addAccessors(jointAccessor);
        
        gltf.addBufferViews(jointBufferView);
        
        BufferView weightBufferView = new BufferView();
        weightBufferView.setBuffer(0);
        weightBufferView.setByteOffset(weightStart);
        weightBufferView.setByteLength(mesh.vertices.length * 0x4);
        
        Accessor weightAccessor = new Accessor();
        weightAccessor.setBufferView(weightBuffer);
        weightAccessor.setByteOffset(0);
        weightAccessor.setComponentType(5121);
        weightAccessor.setType("VEC4");
        weightAccessor.setCount(mesh.vertices.length);

        gltf.addAccessors(weightAccessor);
        
        gltf.addBufferViews(weightBufferView);
        
        //skin.setInverseBindMatrices(matrixView);
        skin.setSkeleton(1);
        gltf.addSkins(skin);
        
        primitive.addAttributes("JOINTS_0", jointView);
        primitive.addAttributes("WEIGHTS_0", weightView);
        
        gltfMesh.addPrimitives(primitive);
        
        gltf.addMeshes(gltfMesh);
        
        
        List<Node> nodes = gltf.getNodes();
        Node dummyNode = nodes.get(2);
        Matrix4x4 matrix = new Matrix4x4(convertFloatsToDoubles(dummyNode.getMatrix()));
        float[] mat = dummyNode.getMatrix();
        dummyNode.setTranslation(new float[] { (float) mat[12], (float) mat[13], (float) mat[14]});
        dummyNode.setRotation(getQuaternion(matrix));
        dummyNode.setScale(getScale(matrix));
        toLocal(nodes, dummyNode);
        
        GltfAssetV2 gltfAssetV2 = new GltfAssetV2(gltf, ByteBuffer.wrap(output.buffer));
        GltfAssetWriterV2 writer = new GltfAssetWriterV2();

        
        FileOutputStream stream = new FileOutputStream(file.getAbsolutePath());
        writer.writeBinary(gltfAssetV2, stream);
        stream.close();
        } catch (Exception ex) {
            Logger.getLogger(DebugCallbacks.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public static void recompressAllSlots() {
        if (Globals.currentWorkspace != WorkspaceType.PROFILE) return;
        
        BigProfile profile = (BigProfile) Toolkit.instance.getCurrentDB();
        
        for (Slot slot : profile.slots) {
            byte[] data = Globals.extractFile(slot.root);
            if (data != null) {
                Resource res = new Resource(data);
                res.getDependencies(Globals.findEntry(slot.root), false);
                byte[] decompressed = res.decompress();
                byte[] compressed = Compressor.Compress(decompressed, "LVLb", res.revision, res.resources);
                
                profile.add(compressed, false);
                
                slot.title = slot.translationKey;
                
                slot.root = new ResourcePtr(Bytes.SHA1(compressed), RType.LEVEL);
            }
        }
    }
    
    public static void addSlots() {
                if (Globals.currentWorkspace != WorkspaceType.PROFILE) return;
        
        BigProfile profile = (BigProfile) Toolkit.instance.getCurrentDB();
        
        Data res = new Data(FileIO.read("C:/Users/Aidan/Desktop/lbp2storymode.slt"));
        res.revision = 0x3e2;
        
        int count = res.int32();
        Slot[] slots = new Slot[count];
        SlotID[] slotIDs = new SlotID[count];
        
        for (int i = 0; i < count; ++i) {
            slots[i] =  new Slot(res, true, false);
            slotIDs[i] = slots[i].slot;
            
            SlotID id = new SlotID(SlotType.USER_CREATED_STORED_LOCAL, i);
            
            slots[i].group = id;
            slots[i].slot = id;
            
            SlotID nil = new SlotID(SlotType.DEVELOPER, 0);
            
            slots[i].primaryLinkGroup = nil;
            slots[i].primaryLinkLevel = nil;
            
            slots[i].title = slots[i].translationKey;
            
            slots[i].isLocked = false;
            
            
            if (i == 82) return;
            
            slots[i].location = Crater.valueOf("SLOT_" + i + "_LBP1").value;
            
            profile.slots.add(slots[i]);
            
        }
        
        profile.shouldSave = true;
        
        Toolkit.instance.updateWorkspace();
    }
    
    public static void jokerTest() {
        if (Globals.currentWorkspace != WorkspaceType.PROFILE) return;
        
        
        BigProfile profile = (BigProfile) Toolkit.instance.getCurrentDB();
        
        for (int i = 0; i < profile.inventoryCollection.size(); ++i) {
            
            FileEntry entry = new FileEntry("l");
            
            ProfileItem pi = profile.inventoryCollection.get(i);
            Resource inSave = new Resource(profile.extract(pi.resource.hash).clone());
            Resource desk = new Resource(FileIO.read("C:/Users/Aidan/Desktop/test.plan"));
            
            inSave.getDependencies(entry);
            desk.getDependencies(entry);
            
            desk.replaceDependency(20, inSave.resources[20], true);
            
            FileIO.write(desk.data, "C:/Users/Aidan/Desktop/test/" + String.valueOf(i) + ".plan");
        }
    }
    
    
    public static void addAllPlansToInventoryTable() {
        if (Globals.currentWorkspace != WorkspaceType.MOD) return;

        Mod mod = (Mod) Toolkit.instance.getCurrentDB();
        for (FileEntry entry: mod.entries) {
            if (entry.path.contains(".plan")) {
                Resource resource = new Resource(entry.data);
                resource.decompress(true);

                Serializer serializer = new Serializer(resource);
                InventoryItem item = serializer.DeserializeItem();

                if (item != null && item.metadata != null) {
                    item.metadata.resource = new ResourcePtr(entry.hash, RType.PLAN);
                    mod.items.add(item.metadata);
                }
            }
        }
    }

    public static void convertAllToGUID() {
        if (Globals.currentWorkspace != WorkspaceType.MOD) return;

        Map < String, Long > map = new HashMap < String, Long > ();

        Mod mod = (Mod) Toolkit.instance.getCurrentDB();

        for (FileEntry entry: mod.entries)
            map.put(Bytes.toHex(entry.hash), entry.GUID);

        for (FileEntry entry: mod.entries) {
            Resource resource = new Resource(entry.data);
            resource.getDependencies(entry);
            if (resource.resources != null) {
                resource.decompress(true);

                for (int i = 0; i < resource.resources.length; ++i) {
                    ResourcePtr res = resource.resources[i];
                    String SHA1 = Bytes.toHex(res.hash);
                    if (!map.containsKey(SHA1)) continue;
                    ResourcePtr newRes = new ResourcePtr(map.get(SHA1), res.type);
                    resource.replaceDependency(i, newRes, false);
                }

                mod.replace(entry, Compressor.Compress(resource.data, resource.magic, resource.revision, resource.resources));
            }
        }
    }

    public static void reserializeCurrentMesh() {
        String path = Paths.get(System.getProperty("user.home"), "/Desktop/", "test.mol").toAbsolutePath().toString();

        if (Globals.lastSelected == null || Globals.lastSelected.entry == null) return;

        ennuo.craftworld.resources.Mesh mesh = Globals.lastSelected.entry.mesh;
        if (mesh == null) return;

        String number = JOptionPane.showInputDialog(Toolkit.instance, "Revision", "0x" + Bytes.toHex(0x272));
        if (number == null) return;

        long integer;
        if (number.toLowerCase().startsWith("0x"))
            integer = Long.parseLong(number.substring(2), 16);
        else if (number.startsWith("g"))
            integer = Long.parseLong(number.substring(1));
        else
            integer = Long.parseLong(number);
        
        mesh.clusterImplicitEllipsoids = new float[0][];
        mesh.bevelVertexCount = 0;
        mesh.hairMorphs = 0;
        mesh.implicitBevelSprings = false;
        mesh.implicitEllipsoids = new ImplicitEllipsoid[0];
        mesh.implicitPlanes = new ImplicitPlane[0];
        mesh.insideImplicitEllipsoids = new ImplicitEllipsoid[0];
        mesh.maxSpringVert = 0;
        mesh.minSpringVert = 0;
        mesh.minUnalignedSpringVert = 0;
        mesh.mirrorMorphs = new short[0];
        mesh.softbodyCluster = new SoftbodyCluster();
        mesh.softbodyEquivs = new SoftbodyVertEquivalence[0];
        mesh.softbodySprings = new SoftbodySpring[0];
        mesh.springTrisStripped = 0;
        mesh.springyTriIndices = new short[0];

        byte[] data = mesh.serialize((int) integer);

        FileIO.write(data, path);

        System.out.println("serialized.");
    }
}
