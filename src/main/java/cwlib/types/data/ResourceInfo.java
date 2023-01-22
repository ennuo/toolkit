package cwlib.types.data;

import java.util.HashSet;

import cwlib.enums.Branch;
import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.streams.MemoryInputStream;
import cwlib.resources.RFontFace;
import cwlib.resources.RStaticMesh;
import cwlib.resources.RTexture;
import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.databases.FileEntry;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.util.Bytes;
import cwlib.util.Compressor;
import cwlib.util.Nodes;
import cwlib.util.Resources;

public class ResourceInfo {
    private static final int MAX_DEPENDENCY_DEPTH = 2;

    private Object resource;
    private Revision revision;
    private ResourceType type = ResourceType.INVALID;
    private SerializationType method = SerializationType.UNKNOWN;
    private byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
    private ResourceDescriptor[] dependencies = new ResourceDescriptor[0];
    private boolean isMissingDependencies;
    private FileModel model = new FileModel(new FileNode("DEPENDENCIES", null, null, null));

    public <T extends Compressable> ResourceInfo(String name, byte[] source) {
        if (source == null || source.length < 4) return;

        int magic = Bytes.toIntegerBE(source);
        
        // PNG, JPG, DDS
        if (magic == 0x89504e47 || magic == 0xFFD8FFE0 || magic == 0x44445320) {
            this.type = ResourceType.TEXTURE;
            this.method = SerializationType.COMPRESSED_TEXTURE;
            this.resource = new RTexture(source);
            return;
        }

        if (name.endsWith(".fpo") || name.endsWith(".vpo") || name.endsWith(".gpo") || name.endsWith(".sbu")) {
            ResourceSystem.println("Assuming resource is compressed object from extension");
            
            this.type = ResourceType.VERTEX_SHADER;
            if (name.endsWith(".fpo"))
                this.type = ResourceType.PIXEL_SHADER;
            if (name.endsWith(".sbu"))
                this.type = ResourceType.SPU_ELF;
            
            try {
                // Only decompressing the data to see if it's valid data,
                // might be better to just check for zlib flags, but it'll do.
                Compressor.decompressData(new MemoryInputStream(source), source.length);
                this.method = SerializationType.BINARY;
            } catch (Exception ex) {
                ResourceSystem.println("Failed to decompress resource, marking as invalid.");
                this.type = ResourceType.INVALID;
            }
            return;
        }

        if (name.endsWith(".trans")) {
            ResourceSystem.println("Assuming resource is translation table from extension");
            this.type = ResourceType.TRANSLATION;
            try { this.resource = new RTranslationTable(source); }
            catch (Exception ex) {
                ResourceSystem.println("Failed to process RTranslationTable, marking resource as invalid");
                this.type = ResourceType.INVALID;
            }
            return;
        }

        ResourceType type = ResourceType.fromMagic(new String(new byte[] { source[0], source[1], source[2] }));
        SerializationType method = SerializationType.fromValue(Character.toString((char) source[3]));
        if (type == ResourceType.INVALID || method == SerializationType.UNKNOWN) return;

        if (type == ResourceType.FONTFACE) {

            if (method != SerializationType.BINARY) {
                ResourceSystem.println("RFontFace only supports binary serialization!");
                return;
            }
            this.type = ResourceType.FONTFACE;
            try { this.resource = new RFontFace(source); }
            catch (Exception ex) {
                ResourceSystem.println("Failed to process RFontFace, marking resource as invalid");
                this.type = ResourceType.INVALID;
            }
            return;
        }

        Resource resource = new Resource(source);
        this.type = resource.getResourceType();
        this.method = resource.getSerializationType();
        this.revision = resource.getRevision();
        this.compressionFlags = resource.getCompressionFlags();
        this.dependencies = resource.getDependencies();

        if (method == SerializationType.BINARY || method == SerializationType.ENCRYPTED_BINARY) {
            ResourceSystem.println("Resource Type: " + this.type.name());
            ResourceSystem.println(this.revision);
            short branchID = this.revision.getBranchID();
            if (branchID != 0) {
                Branch branch = Branch.fromID(branchID);
                ResourceSystem.println("Branch: " + (branch == null ? "UNRESOLVED" : branch.name()));
            }
            if (this.compressionFlags != 0)
                ResourceSystem.println(String.format("Compression Flags: %s (%d)", CompressionFlags.toString(this.compressionFlags), this.getCompressionFlags()));
            if (this.type != ResourceType.STATIC_MESH) {
                Class<? extends Serializable> clazz = this.type.getCompressable();
                if (clazz != null) {
                    ResourceSystem.DISABLE_LOGS = true;
                    try { this.resource = resource.loadResource(clazz); } 
                    catch (SerializationException ex) { 
                        ResourceSystem.DISABLE_LOGS = false;
                        ResourceSystem.println("Encountered error while deserializing resource, received message:");
                        ResourceSystem.println(ex.getMessage());
                        this.resource = null; 
                    }
                    catch (Exception ex) {
                        ResourceSystem.DISABLE_LOGS = false;
                        ResourceSystem.println("An unknown error occurred while processing resource, printing stacktrace:");
                        ex.printStackTrace();
                    }
                    ResourceSystem.DISABLE_LOGS = false;
                } else ResourceSystem.println(this.type.name() + " is unregistered!");
            }
            if (this.type == ResourceType.STATIC_MESH)
                this.resource = new RStaticMesh(resource);
        } else if (method == SerializationType.TEXT)
            ResourceSystem.println("Gathering variables of text based resources is currently unsupported.");

        if (this.type == ResourceType.GTF_TEXTURE || this.type == ResourceType.TEXTURE) {
            RTexture texture = new RTexture(resource);
            this.resource = texture;
        }

        
        boolean isSlowResource = 
            this.type == ResourceType.PACKS ||
            this.type == ResourceType.SLOT_LIST ||
            this.type == ResourceType.LEVEL ||
            this.type == ResourceType.ADVENTURE_CREATE_PROFILE ||
            this.type == ResourceType.PALETTE ||
            this.type == ResourceType.CACHED_LEVEL_DATA ||
            this.type == ResourceType.CACHED_COSTUME_DATA ||
            this.type == ResourceType.LOCAL_PROFILE ||
            this.type == ResourceType.BIG_PROFILE;
        
        int depth = isSlowResource ? ResourceInfo.MAX_DEPENDENCY_DEPTH : 0;
        if (this.dependencies.length != 0)
            this.populateDependencyModel(this.dependencies, new HashSet<>(), depth);
    }

    private void populateDependencyModel(ResourceDescriptor[] dependencies, HashSet<ResourceDescriptor> unique, int depth) {
        if (dependencies == null) return;
        if (depth > ResourceInfo.MAX_DEPENDENCY_DEPTH) return;

        for (ResourceDescriptor descriptor : dependencies) {
            if (unique.contains(descriptor)) continue;
            unique.add(descriptor);
            
            FileEntry entry = ResourceSystem.get(descriptor);
            if (entry == null) continue;

            Nodes.addNode((FileNode) this.model.getRoot(), entry);

            if ((depth + 1) <= ResourceInfo.MAX_DEPENDENCY_DEPTH) {
                byte[] data = ResourceSystem.extract(entry);
                if (data != null) {
                    this.populateDependencyModel(
                        Resources.getDependencyTable(data).toArray(ResourceDescriptor[]::new), 
                        unique, 
                        (depth + 1)
                    );
                }
            }
        }
    }

    @SuppressWarnings("unchecked") public <T> T getResource() { return (T) this.resource; }
    public Revision getRevision() { return this.revision; }
    public ResourceType getType() { return this.type; }
    public SerializationType getMethod() { return this.method; }
    public byte getCompressionFlags() { return this.compressionFlags; }
    public ResourceDescriptor[] getDependencies() { return this.dependencies; }
    public boolean isMissingDependencies() { return this.isMissingDependencies; }
    public FileModel getModel() { return this.model; }
    public boolean isResource() { return this.type != ResourceType.INVALID; }
    public boolean isCompressedResource() { 
        return this.type != ResourceType.INVALID && 
        (this.method == SerializationType.BINARY || this.method == SerializationType.ENCRYPTED_BINARY); 
    }
}
