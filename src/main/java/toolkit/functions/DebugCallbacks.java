package toolkit.functions;

import cwlib.enums.DatabaseType;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.util.FileIO;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileEntry;
import cwlib.types.data.ResourceDescriptor;
import toolkit.utilities.FileChooser;

import java.io.File;

public class DebugCallbacks {
    public static void CollectDependencies(String extension) {
        if (ResourceSystem.getDatabaseType() != DatabaseType.FILE_DATABASE) {
            System.err.println("Collections can only be used on RFileDB.");
            return;
        }
        
        FileDB database = ResourceSystem.getSelectedDatabase();
        StringBuilder builder = new StringBuilder(database.getEntryCount() * 1024);
        for (FileEntry entry : database) {
            if (entry.getPath().toLowerCase().endsWith(extension)) {
                byte[] data = ResourceSystem.extract(entry);
                if (data == null) continue;
                try {
                    Resource resource = new Resource(data);
                    ResourceDescriptor[] dependencies = resource.getDependencies();
                    if (dependencies.length == 0)  continue;
                    builder.append(String.format("%s (%s)\n", entry.getPath(), entry.getKey()));
                    for (ResourceDescriptor descriptor : resource.getDependencies()) {
                        String type = descriptor.getType().name();
                        String name = String.format(" - (Unresolved Resource) [%s]", type);
                        if (descriptor.isGUID()) {
                            name = String.format(" - (Unresolved Path) (%s) [%s]\n", descriptor.getGUID(), type);
                            FileEntry resolved = ResourceSystem.get(descriptor.getGUID());
                            if (resolved != null)
                                name = String.format(" - %s (%s) [%s]\n", resolved.getPath(), descriptor.getGUID(), type);
                        } else if (descriptor.isHash())
                            name = String.format(" - %s [%s]", descriptor.getSHA1().toString(), type);
                        else continue;
                        builder.append(name);
                    }   
                    builder.append('\n');
                } catch (Exception e) { /* Ignore the error */  }
            }
        }
        
        File file = FileChooser.openFile("dependencies.txt", "txt", true);
        if (file == null) return;
        
        FileIO.write(builder.toString().getBytes(), file.getAbsolutePath());
    }
    
}
