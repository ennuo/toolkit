package ennuo.toolkit.functions;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
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
        
        ArrayList<ResourceDescriptor> dependencies = new ArrayList<>(resource.dependencies.size());
        for (ResourceDescriptor descriptor : dependencies)
            if (Globals.findEntry(descriptor) != null)
                dependencies.add(descriptor);
        resource.dependencies = dependencies;
        
        Globals.replaceEntry(entry, resource.compressToResource());
    }
}