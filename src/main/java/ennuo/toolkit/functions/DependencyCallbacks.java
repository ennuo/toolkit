package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import java.util.ArrayList;

public class DependencyCallbacks {
    public static void removeDependencies() {
        FileEntry entry = Globals.lastSelected.entry;
        byte[] data = Globals.extractFile(entry.hash);

        if (data == null) return;

        Resource resource = new Resource(data);
        Output output = new Output(resource.length);

        output.bytes(resource.bytes(0x8));
        int offset = resource.int32f();
        output.int32f(offset);
        output.bytes(resource.bytes(offset - resource.offset));
        output.int32f(0);
        output.shrinkToFit();

        Globals.replaceEntry(entry, output.buffer);
    }

    public static void removeMissingDependencies() {
        FileEntry entry = Globals.lastSelected.entry;
        byte[] data = Globals.extractFile(entry.hash);

        if (data == null) return;

        Resource resource = new Resource(data);
        resource.getDependencies(entry);
        resource.seek(0);
        Output output = new Output(resource.length);

        output.bytes(resource.bytes(0x8));
        int offset = resource.int32f();
        output.int32f(offset);
        output.bytes(resource.bytes(offset - resource.offset));

        ArrayList < ResourcePtr > dependencies = new ArrayList < ResourcePtr > (resource.dependencies.length);
        for (int i = 0; i < resource.dependencies.length; ++i) {
            if (resource.dependencies[i] != null) {
                if (Globals.extractFile(resource.dependencies[i].hash) != null)
                    dependencies.add(resource.resources[i]);
            }
        }

        output.int32f(dependencies.size());
        for (ResourcePtr ptr: dependencies) {
            output.resource(ptr, true);
            output.int32f(ptr.type.value);
        }

        output.shrinkToFit();

        Globals.replaceEntry(entry, output.buffer);
    }
}