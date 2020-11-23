package ennuo.craftworld.memory;

import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.enums.Metadata;
import ennuo.craftworld.resources.enums.Metadata.CompressionType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.things.InventoryMetadata;
import ennuo.craftworld.things.Serializer;
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

    public ResourcePtr[] resources = null;
    public FileEntry[] dependencies = null;

    public Resource(byte[] data) {
        super(data);
        if (data != null) {
            magic = str(4);
            revision = int32f();
            type = Metadata.getType(magic, revision);
            seek(0);
        }
    }


    // TODO: Actually finish this function, I can serialize the metadata, yes, but //
    // I also need to make sure no dependencies are duplicated in the dependency table, //
    // but I also need to make sure none are removed if they do exist elsewhere. //
    public void replaceMetadata(InventoryMetadata data, boolean compressed) {
        if (!magic.equals("PLNb")) return;

        if (compressed)
            decompress(true);

        if (revision < 0x272) {
            System.out.println("lol fuck off");
            return;
        }

        InventoryMetadata oldData = new Serializer(this).DeserializeItem().metadata;

        Output output = new Output(InventoryMetadata.MAX_SIZE);
        Serializer serializer = new Serializer(output);

        if (revision <= 0x272) serializer.serializeLegacyMetadata(data, true);
        else serializer.serializeMetadata(data, true);

        output.shrinkToFit();
    }

    public void removePlanDescriptors(long GUID, boolean compressed) {
        if (!magic.equals("PLNb")) return;
        if (compressed)
            decompress(true);


        if (peek() == 1 || peek() == 0) bool();
        int32();

        int start = offset;
        seek(0);

        byte[] left = bytes(start);

        int size = int32();

        Data thingData = new Data(bytes(size), revision);

        byte[] right = bytes(length - offset);

        Output output = new Output(0x8, revision);
        output.uint32(GUID);
        output.shrinkToFit();

        Bytes.ReplaceAll(thingData, Bytes.createResourceReference(new ResourcePtr(GUID, RType.PLAN), revision), new byte[] { 00 });
        Bytes.ReplaceAll(thingData, output.buffer, new byte[] { 00 });

        Output sb = new Output(6, revision);
        sb.int32(thingData.data.length);
        sb.shrinkToFit();

        setData(Bytes.Combine(
            left,
            sb.buffer,
            thingData.data,
            right
        ));

        if (compressed)
            setData(Compressor.Compress(data, magic, revision, resources));
    }

    public void replaceDependency(int index, ResourcePtr replacement, boolean compressed) {
        ResourcePtr dependency = resources[index];
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
            data.int32();

            int start = data.offset;

            data.seek(0);

            byte[] left = data.bytes(start);

            int size = data.int32();

            Data thingData = new Data(data.bytes(size), revision);

            byte[] right = data.bytes(data.length - data.offset);


            Bytes.ReplaceAll(thingData, oldRes, newRes);

            Output output = new Output(6, revision);
            output.int32(thingData.data.length);
            output.shrinkToFit();


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

        ResourcePtr self = new ResourcePtr();
        if (entry.GUID != -1) self.GUID = entry.GUID;
        else self.hash = entry.hash;

        entry.canReplaceDecompressed = true;
        int missingDependencies = 0;
        seek(8);
        int tableOffset = int32f();
        seek(tableOffset);
        int dependencyCount = int32f();
        if (dependencies == null || dependencyCount != dependencies.length)
            dependencies = new FileEntry[dependencyCount];
        resources = new ResourcePtr[dependencyCount];
        for (int i = 0; i < dependencyCount; i++) {
            resources[i] = new ResourcePtr();
            switch (int8()) {
                case 1:
                    byte[] hash = bytes(20);
                    resources[i].hash = hash;
                    dependencies[i] = Globals.findEntry(hash);
                    break;
                case 2:
                    long GUID = uint32f();
                    resources[i].GUID = GUID;
                    dependencies[i] = Globals.findEntry(GUID);
                    break;
            }
            if (dependencies[i] == null) missingDependencies++;
            resources[i].type = RType.getValue(int32f());
            if (dependencies[i] != null && entry != null && recursive && !self.equals(resources[i])) {
                byte[] data = Globals.extractFile(dependencies[i].hash);
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
            int dep = int32f();
            int8();
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
                seek(20);
                break;
            case CUSTOM_COMPRESSION_LEGACY:
                if (revision <= 0x188) seek(14);
                else seek(15);
                break;
            default:
                return null;
        }

        short chunks = int16();

        if (chunks == 0) {
            int old = offset;
            seek(8);
            int tableOffset = int32f();
            seek(old);
            byte[] data = bytes(tableOffset - offset);
            if (set) setData(data);
            return data;
        }

        int[] compressed = new int[chunks];
        int[] decompressed = new int[chunks];
        int decompressedSize = 0;
        for (int i = 0; i < chunks; i++) {
            compressed[i] = int16LE();
            decompressed[i] = int16LE();
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