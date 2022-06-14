package toolkit.functions;

import cwlib.types.Resource;
import cwlib.types.data.ResourceReference;
import toolkit.utilities.Globals;
import cwlib.types.FileEntry;

import java.util.ArrayList;

public class DependencyCallbacks {
    public static void removeDependencies() {
        FileEntry entry = Globals.lastSelected.entry;
        byte[] data = Globals.extractFile(entry.hash);

        if (data == null) return;

        Resource resource = new Resource(data);
        resource.dependencies.clear();

        Globals.replaceEntry(entry, resource.compressToResource());
    }

    public static void removeMissingDependencies() {
        FileEntry entry = Globals.lastSelected.entry;
        byte[] data = Globals.extractFile(entry.hash);

        if (data == null) return;

        Resource resource = new Resource(data);
        
        ArrayList<ResourceReference> dependencies = new ArrayList<>(resource.dependencies.size());
        for (ResourceReference descriptor : dependencies)
            if (Globals.findEntry(descriptor) != null)
                dependencies.add(descriptor);
        resource.dependencies = dependencies;
        
        Globals.replaceEntry(entry, resource.compressToResource());
    }
}