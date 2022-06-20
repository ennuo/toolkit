package toolkit.functions;

import cwlib.types.Resource;
import cwlib.types.data.ResourceReference;
import toolkit.utilities.ResourceSystem;
import cwlib.types.databases.FileEntry;

import java.util.ArrayList;

public class DependencyCallbacks {
    public static void removeDependencies() {
        FileEntry entry = ResourceSystem.lastSelected.entry;
        byte[] data = ResourceSystem.extractFile(entry.hash);

        if (data == null) return;

        Resource resource = new Resource(data);
        resource.dependencies.clear();

        ResourceSystem.replaceEntry(entry, resource.compressToResource());
    }

    public static void removeMissingDependencies() {
        FileEntry entry = ResourceSystem.lastSelected.entry;
        byte[] data = ResourceSystem.extractFile(entry.hash);

        if (data == null) return;

        Resource resource = new Resource(data);
        
        ArrayList<ResourceReference> dependencies = new ArrayList<>(resource.dependencies.size());
        for (ResourceReference descriptor : dependencies)
            if (ResourceSystem.findEntry(descriptor) != null)
                dependencies.add(descriptor);
        resource.dependencies = dependencies;
        
        ResourceSystem.replaceEntry(entry, resource.compressToResource());
    }
}