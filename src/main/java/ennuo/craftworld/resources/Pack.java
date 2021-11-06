package ennuo.craftworld.resources;

import ennuo.craftworld.utilities.Compressor;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.structs.PackItem;
import java.util.ArrayList;

public class Pack {
    public ArrayList<PackItem> packs;

    public Pack(Data data) {
        int count = data.i32();
        packs = new ArrayList<PackItem>(count);
        for (int i = 0; i < count; ++i)
            packs.add(new PackItem(data));

    }

    public byte[] serialize(int revision, boolean compressed) {
        int count = packs.size();
        Output output = new Output(0x5 + (PackItem.MAX_SIZE * count), revision);
        output.i32(packs.size());
        for (PackItem item: packs)
            item.serialize(output);
        output.shrink();
        if (compressed) {
            ResourcePtr[] dependencies = new ResourcePtr[output.dependencies.size()];
            dependencies = output.dependencies.toArray(dependencies);
            return Compressor.Compress(output.buffer, "PCKb", revision, dependencies);
        } else return output.buffer;
    }
}