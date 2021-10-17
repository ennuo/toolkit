package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Compressor;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.Crater;
import ennuo.craftworld.resources.enums.ItemType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.ProfileItem;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.resources.structs.UserCreatedDetails;
import ennuo.craftworld.resources.structs.mesh.ImplicitEllipsoid;
import ennuo.craftworld.resources.structs.mesh.ImplicitPlane;
import ennuo.craftworld.resources.structs.mesh.SoftbodyCluster;
import ennuo.craftworld.resources.structs.mesh.SoftbodySpring;
import ennuo.craftworld.resources.structs.mesh.SoftbodyVertEquivalence;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.things.InventoryItem;
import ennuo.craftworld.things.InventoryMetadata;
import ennuo.craftworld.things.Serializer;
import ennuo.craftworld.types.BigProfile;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.utilities.Globals.WorkspaceType;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class DebugCallbacks {
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
        
        int count = res.i32();
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
                    item.metadata.resource = new ResourcePtr(entry.SHA1, RType.PLAN);
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
            map.put(Bytes.toHex(entry.SHA1), entry.GUID);

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

                mod.edit(entry, Compressor.Compress(resource.data, resource.magic, resource.revision, resource.resources));
            }
        }
    }
    
    public static void emittionTendency() {
        BigProfile profile = null;
        FileDB db = null;
        
       
        for (FileData database : Globals.databases) {
            if (database.type.equals("FileDB"))
                db = (FileDB) database;
            else if (database.type.equals("Big Profile"))
                profile = (BigProfile)database;
        }
        
        if (db == null) {
            System.err.println("Could not find a FileDB.");
            return;
        }
        
        if (profile == null) {
            System.err.println("Could not find a Big Profile");
            return;
        }
        
        for (FileEntry entry : db.entries) {
            if (entry.path.toLowerCase().contains("levels") && entry.path.toLowerCase().contains(".plan")) {
                InventoryMetadata metadata = new InventoryMetadata();
                metadata.userCreatedDetails = new UserCreatedDetails();
                metadata.userCreatedDetails.title = new File(entry.path).getName();
                metadata.userCreatedDetails.description = entry.path;
                metadata.icon = new ResourcePtr(68376, RType.TEXTURE);
                
                metadata.translatedCategory = "Emitted Plans";
                metadata.translatedLocation = "Emitted plans";
                
                metadata.resource = new ResourcePtr(entry.GUID, RType.PLAN);
                
                metadata.type = ItemType.OBJECTS;
                
                profile.addItem(new ResourcePtr(entry.GUID, RType.PLAN), metadata);
               
            }
        }
        
        profile.shouldSave = true;
        
        
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
