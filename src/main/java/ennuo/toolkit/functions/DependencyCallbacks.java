package ennuo.toolkit.functions;

import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import java.util.ArrayList;

public class DependencyCallbacks {
    public static void removeDependencies() {
        FileEntry entry = Globals.lastSelected.entry;
        byte[] data = Globals.extractFile(entry.SHA1);

        if (data == null) return;

        Resource resource = new Resource(data);
        Output output = new Output(resource.length);

        output.bytes(resource.bytes(0x8));
        int offset = resource.i32f();
        output.i32f(offset);
        output.bytes(resource.bytes(offset - resource.offset));
        output.i32f(0);
        output.shrink();

        Globals.replaceEntry(entry, output.buffer);
    }

    public static void removeMissingDependencies() {
        FileEntry entry = Globals.lastSelected.entry;
        byte[] data = Globals.extractFile(entry.SHA1);

        if (data == null) return;

        Resource resource = new Resource(data);
        resource.getDependencies(entry);
        resource.seek(0);
        Output output = new Output(resource.length);

        output.bytes(resource.bytes(0x8));
        int offset = resource.i32f();
        output.i32f(offset);
        output.bytes(resource.bytes(offset - resource.offset));

        ArrayList < ResourcePtr > dependencies = new ArrayList < ResourcePtr > (resource.dependencies.length);
        for (int i = 0; i < resource.dependencies.length; ++i) {
            if (resource.dependencies[i] != null) {
                if (Globals.extractFile(resource.dependencies[i].SHA1) != null)
                    dependencies.add(resource.resources[i]);
            }
        }

        output.i32f(dependencies.size());
        for (ResourcePtr ptr: dependencies) {
            output.resource(ptr, true);
            output.i32f(ptr.type.value);
        }

        output.shrink();

        Globals.replaceEntry(entry, output.buffer);
    }
}