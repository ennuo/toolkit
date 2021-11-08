package ennuo.craftworld.resources;

import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.resources.enums.Metadata;
import ennuo.craftworld.resources.enums.Metadata.CompressionType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.types.mods.Mod;
import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.Compressor;
import ennuo.toolkit.utilities.Globals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;

public class Resource extends Data {

    public boolean isStreamingChunk = false;

    public String magic;
    public CompressionType type;

    public ResourceDescriptor[] resources = null;
    public FileEntry[] dependencies = null;

    public Resource(byte[] data) {
        super(data);
        if (data != null) {
            this.magic = this.str(4);
            if (this.magic.length() == 4 && this.magic.charAt(3) == 'b')
                this.revision = this.i32f();
            this.type = Metadata.getType(this.magic, this.revision);   
            this.seek(0);
        }
    }


    // TODO: Actually finish this function, I can serialize the metadata, yes, but //
    // I also need to make sure no dependencies are duplicated in the dependency table, //
    // but I also need to make sure none are removed if they do exist elsewhere. //
    public void replaceMetadata(InventoryDetails data, boolean compressed) {
        return;
    }

    public void removePlanDescriptors(long GUID, boolean compressed) {
        if (!magic.equals("PLNb")) return;
        if (compressed)
            decompress(true);


        if (peek() == 1 || peek() == 0) bool();
        i32();

        int start = offset;
        seek(0);

        byte[] left = bytes(start);

        int size = i32();

        Data thingData = new Data(bytes(size), revision);

        byte[] right = bytes(length - offset);

        Output output = new Output(0x8, revision);
        output.u32(GUID);
        output.shrink();

        Bytes.ReplaceAll(thingData, Bytes.createResourceReference(new ResourceDescriptor(GUID, RType.PLAN), revision), new byte[] { 00 });
        Bytes.ReplaceAll(thingData, output.buffer, new byte[] { 00 });

        Output sb = new Output(6, revision);
        sb.i32(thingData.data.length);
        sb.shrink();

        setData(Bytes.Combine(
            left,
            sb.buffer,
            thingData.data,
            right
        ));

        if (compressed)
            setData(Compressor.Compress(data, magic, revision, resources));
    }

    public void replaceDependency(int index, ResourceDescriptor replacement, boolean compressed) {
        ResourceDescriptor dependency = resources[index];
        if (dependency == null || (dependency.GUID == -1 && dependency.hash == null) || dependencies.length == 0) return;

        int tRevision = revision;
        if (magic.equals("SMHb")) tRevision = 0x271;

        byte[] oldRes = Bytes.createResourceReference(dependency, tRevision);
        byte[] newRes = Bytes.createResourceReference(replacement, tRevision);

        if (Arrays.equals(oldRes, newRes)) return;


        if (compressed)
            decompress(true);

        Data data = this;

        if (magic.equals("PLNb")) {

            if (data.peek() == 1 || data.peek() == 0 || isStreamingChunk) data.bool();
            data.i32();

            int start = data.offset;

            data.seek(0);

            byte[] left = data.bytes(start);

            int size = data.i32();

            Data thingData = new Data(data.bytes(size), revision);

            byte[] right = data.bytes(data.length - data.offset);


            Bytes.ReplaceAll(thingData, oldRes, newRes);

            Output output = new Output(6, revision);
            output.i32(thingData.data.length);
            output.shrink();


            setData(Bytes.Combine(
                left,
                output.buffer,
                thingData.data,
                right
            ));

        }

        Bytes.ReplaceAll(data, oldRes, newRes);

        resources[index] = replacement;

        if (compressed)
            setData(Compressor.Compress(data.data, magic, revision, resources));
    }
    
    public Mod recurse(FileEntry entry) {
        Mod mod = new Mod();
        Bytes.recurse(mod, this, entry);
        return mod;
    }

    public Mod hashinate(FileEntry entry) {
        Mod mod = new Mod();
        Bytes.hashinate(mod, this, entry);
        return mod;
    }

    public int getDependencies(FileEntry entry) {
        return getDependencies(entry, true);
    }
    public int getDependencies(FileEntry entry, boolean recursive) {
        if (type != CompressionType.CUSTOM_COMPRESSION && type != CompressionType.CUSTOM_COMPRESSION_LEGACY && type != CompressionType.STATIC_MESH)
            return 0;

        ResourceDescriptor self = new ResourceDescriptor();
        if (entry.GUID != -1) self.GUID = entry.GUID;
        else self.hash = entry.SHA1;

        entry.canReplaceDecompressed = true;
        int missingDependencies = 0;
        seek(8);
        int tableOffset = i32f();
        seek(tableOffset);
        int dependencyCount = i32f();
        if (dependencies == null || dependencyCount != dependencies.length)
            dependencies = new FileEntry[dependencyCount];
        resources = new ResourceDescriptor[dependencyCount];
        for (int i = 0; i < dependencyCount; i++) {
            resources[i] = new ResourceDescriptor();
            switch (i8()) {
                case 1:
                    byte[] hash = bytes(20);
                    resources[i].hash = hash;
                    dependencies[i] = Globals.findEntry(hash);
                    break;
                case 2:
                    long GUID = u32f();
                    resources[i].GUID = GUID;
                    dependencies[i] = Globals.findEntry(GUID);
                    break;
            }
            if (dependencies[i] == null) missingDependencies++;
            resources[i].type = RType.getValue(i32f());
            if (dependencies[i] != null && entry != null && recursive && !self.equals(resources[i])) {
                byte[] data = Globals.extractFile(dependencies[i].SHA1);
                if (data != null) {
                    Resource resource = new Resource(data);
                    if (resource.magic.equals("FSHb")) continue;
                    resource.getDependencies(dependencies[i]);
                    dependencies[i].dependencies = resource.dependencies;
                }
            }
        }
        seek(0);
        return missingDependencies;
    }

    public byte[] decompress() {
        return decompress(false);
    }
    
    public byte[] decompress(boolean set) {
        if (type == CompressionType.STATIC_MESH) {
            seek(0x8);
            int dep = i32f();
            i8();
            byte[] data = bytes(dep - offset);
            if (set)
                this.setData(data);
            return data;
        }

        switch (type) {
            case LEGACY_TEXTURE:
                seek(6);
                break;
            case GTF_TEXTURE:
            case GXT_SIMPLE_TEXTURE:
                seek(30);
                break;
            case GXT_EXTENDED_TEXTURE:
                seek(50);
                break;
            case CUSTOM_COMPRESSION:
                if (data[16] == 1) seek(19);
                else seek(20);
                break;
            case CUSTOM_COMPRESSION_LEGACY:
                if (revision <= 0x188) seek(14);
                else seek(15);
                break;
            default:
                return null;
        }

        short chunks = i16();

        if (chunks == 0) {
            int old = offset;
            seek(8);
            int tableOffset = i32f();
            seek(old);
            byte[] data = bytes(tableOffset - offset);
            if (set) setData(data);
            return data;
        }

        int[] compressed = new int[chunks];
        int[] decompressed = new int[chunks];
        int decompressedSize = 0;
        for (int i = 0; i < chunks; i++) {
            compressed[i] = u16();
            decompressed[i] = u16();
            decompressedSize += decompressed[i];
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream(decompressedSize);
        try {
            for (int j = 0; j < chunks; j++) {
                if (compressed[j] == decompressed[j]) {
                    stream.write(bytes(compressed[j]));
                    continue;
                }
                Inflater decompressor = new Inflater();
                decompressor.setInput(bytes(compressed[j]));
                byte[] chunk = new byte[decompressed[j]];
                decompressor.inflate(chunk);
                decompressor.end();
                stream.write(chunk);
            }
        } catch (IOException | java.util.zip.DataFormatException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, (String) null, ex);
            setData(null);
            return null;
        }
        byte[] data = stream.toByteArray();
        if (set) setData(stream.toByteArray());
        return data;
    }
}
