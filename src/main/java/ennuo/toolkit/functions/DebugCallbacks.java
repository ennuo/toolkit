package ennuo.toolkit.functions;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.utilities.Bytes;
import ennuo.toolkit.utilities.FileChooser;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DebugCallbacks {
    public static void CollectDependencies(String extension) {
        if (Globals.currentWorkspace != Globals.WorkspaceType.MAP) {
            System.err.println("Collections can only be used on RFileDB.");
            return;
        }
        
        FileDB database = (FileDB) Toolkit.instance.getCurrentDB();
        StringBuilder builder = new StringBuilder(database.entries.size() * 1024);
        for (FileEntry entry : database.entries) {
            if (entry.path.toLowerCase().endsWith(extension)) {
                byte[] data = Globals.extractFile(entry.hash);
                if (data == null) continue;
                try {
                    Resource resource = new Resource(data);
                    if (resource.dependencies == null || (resource.dependencies != null && resource.dependencies.length == 0)) 
                        continue;
                    builder.append(String.format("%s (g%d)\n", entry.path, entry.GUID));
                    for (ResourceDescriptor descriptor : resource.dependencies) {
                        String type = descriptor.type.name();
                        String name = String.format(" - (Unresolved Resource) [%s]", type);
                        if (descriptor.GUID != -1) {
                            name = String.format(" - (Unresolved Path) (g%d) [%s]\n", descriptor.GUID, type);
                            FileEntry resolved = Globals.findEntry(descriptor.GUID);
                            if (resolved != null)
                                name = String.format(" - %s (g%d) [%s]\n", resolved.path, descriptor.GUID, type);
                        } else if (descriptor.hash != null)
                            name = String.format(" - %s [%s]", descriptor.hash.toString(), type);
                        else continue;
                        builder.append(name);
                    }   
                    builder.append('\n');
                } catch (Exception e) { /* Ignore the error */ }
            }
        }
        
        File file = FileChooser.openFile("dependencies.txt", "txt", true);
        if (file == null) return;
        
        FileIO.write(builder.toString().getBytes(), file.getAbsolutePath());
    }
    
}
