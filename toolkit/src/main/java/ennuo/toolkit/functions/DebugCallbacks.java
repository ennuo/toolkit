package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Compressor;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.things.InventoryItem;
import ennuo.craftworld.things.Serializer;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.utilities.Globals.WorkspaceType;
import ennuo.toolkit.windows.Toolkit;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class DebugCallbacks {
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

        Mesh mesh = Globals.lastSelected.entry.mesh;
        if (mesh == null) return;

        String number = JOptionPane.showInputDialog(Toolkit.instance, "Revision", "0x" + Bytes.toHex(mesh.revision));
        if (number == null) return;

        long integer;
        if (number.toLowerCase().startsWith("0x"))
            integer = Long.parseLong(number.substring(2), 16);
        else if (number.startsWith("g"))
            integer = Long.parseLong(number.substring(1));
        else
            integer = Long.parseLong(number);

        byte[] data = mesh.serialize((int) integer);

        FileIO.write(data, path);

        System.out.println("serialized.");
    }
}