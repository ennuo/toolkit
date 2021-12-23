package ennuo.toolkit.functions;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.toolkit.utilities.FileChooser;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;

public class DebugCallbacks {
    public static void CollectDependencies(String extension) {
        if (Globals.currentWorkspace != Globals.WorkspaceType.MAP) {
            System.err.println("Collections can only be used on RFileDB.");
            return;
        }
        
        FileDB database = (FileDB) Toolkit.instance.getCurrentDB();
        StringBuilder builder = new StringBuilder(database.entries.size() * 1024);
        for (FileEntry entry : database.entries) {
            if (entry.path.toLowerCase().contains(extension)) {
                byte[] data = Globals.extractFile(entry.hash);
                if (data == null) continue;
                try {
                    Resource resource = new Resource(data);
                    if (resource.dependencies != null && resource.dependencies.length == 0) continue;
                    builder.append(entry.path + '\n');
                    for (ResourceDescriptor descriptor : resource.dependencies) {
                        String name = String.format(" - (Unresolved Path) (g%d)\n", descriptor.GUID);
                        FileEntry resolved = Globals.findEntry(descriptor);
                        if (entry != null)
                            name = String.format(" - %s (g%d)\n", resolved.path, descriptor.GUID);
                        builder.append(name);
                    }   
                    builder.append('\n');
                } catch (Exception e) { /* Ignoring any errors. */ }
            }
        }
        
        File file = FileChooser.openFile("dependencies.txt", "txt", true);
        if (file == null) return;
        
        FileIO.write(builder.toString().getBytes(), file.getAbsolutePath());
    }
    
}
