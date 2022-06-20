package cwlib.types;

import cwlib.enums.ResourceType;
import cwlib.io.streams.MemoryInputStream;
import cwlib.types.data.ResourceReference;
import cwlib.enums.SerializationType;
import cwlib.types.data.Revision;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RPlan;
import cwlib.structs.staticmesh.StaticMeshInfo;
import cwlib.io.serializer.Serializer;
import cwlib.types.databases.FileEntry;
import cwlib.util.Bytes;
import cwlib.util.Compressor;
import toolkit.utilities.ResourceSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resource {
    public ResourceType type = ResourceType.INVALID;
    public SerializationType method = SerializationType.UNKNOWN;
    public CellGcmTexture textureInfo;
    public StaticMeshInfo meshInfo;
    public Revision revision;
    private boolean isCompressed = true;
    public byte compressionFlags = 0;
    public MemoryInputStream handle = null;
    public ArrayList<ResourceReference> dependencies = new ArrayList<>();
    
    public Resource(){}
    
    public Resource(MemoryOutputStream output) {
        output.shrink();
        this.revision = output.revision;
        this.dependencies = new ArrayList<>(output.dependencies);
        this.method = SerializationType.BINARY;
        this.compressionFlags = output.compressionFlags;
        this.handle = new MemoryInputStream(output.buffer, output.revision);
        this.handle.compressionFlags = output.compressionFlags;
    }
    
    public Resource(String path) {
        this.handle = new MemoryInputStream(path);
        this.process();
    }
    
    public Resource(byte[] data) {
        this.handle = new MemoryInputStream(data);
        this.process();
    }
    
    private void process() {
        if (this.handle == null || this.handle.length < 0xb) return;
        this.type = ResourceType.fromMagic(this.handle.str(3));
        if (this.type == ResourceType.INVALID) { this.handle.seek(0); return; }
        this.method = SerializationType.fromValue(this.handle.str(1));
        switch (this.method) {
            case UNKNOWN:
                this.handle.seek(0);
                return;
            case BINARY:
            case ENCRYPTED_BINARY:
                this.revision = new Revision(this.handle.i32f());
                this.handle.revision = this.revision;
                int dependencyTableOffset = -1;
                if (this.revision.head >= 0x109) {
                    dependencyTableOffset = this.getDependencies();
                    if (this.revision.head >= 0x189) {
                        if (this.type != ResourceType.STATIC_MESH) {
                            if (this.revision.head >= 0x271) { 
                                // NOTE(Aidan): Were they actually added on 0x27a, but how can it be on 0x272 then?!
                                this.revision.branchID = this.handle.i16();
                                this.revision.branchRevision = this.handle.i16();
                                this.handle.revision = revision;
                            }
                            if (this.revision.head >= 0x297 || (this.revision.head == 0x272 && this.revision.branchID != 0)) {
                                this.compressionFlags = this.handle.i8();
                                this.handle.compressionFlags = this.compressionFlags;
                            }
                            this.isCompressed = this.handle.bool();
                        } else 
                            this.meshInfo = new Serializer(this.handle).struct(null, StaticMeshInfo.class);
                    }
                }
                
                if (this.method == SerializationType.ENCRYPTED_BINARY) {
                    int size = this.handle.i32f(), padding = 0;
                    if (size % 4 != 0)
                        padding = 4 - (size % 4);
                    this.handle.setData(TEA.decrypt(this.handle.bytes(size + padding)));
                    this.handle.offset += padding;
                }
                
                if (this.isCompressed)
                    Compressor.decompressData(this.handle, dependencyTableOffset);
                else if (dependencyTableOffset != -1)
                    this.handle.setData(this.handle.bytes(dependencyTableOffset - this.handle.offset));
                else this.handle.setData(this.handle.bytes(this.handle.length - this.handle.offset));
                
                break;
            case TEXT:
                this.handle.setData(this.handle.bytes(this.handle.length - 4));
                break;
            case TEXTURE:
            case GXT_SIMPLE:
            case GXT_EXTENDED:
                if (this.type != ResourceType.TEXTURE)
                    this.textureInfo = new CellGcmTexture(this.handle, this.method);
                Compressor.decompressData(this.handle, this.handle.length);
                break;
        }
    }
    
    public int registerDependencies(boolean recursive) {
        if (this.method != SerializationType.BINARY) return 0;
        int missingDependencies = 0;
        for (ResourceReference dependency : this.dependencies) {
            FileEntry entry = ResourceSystem.findEntry(dependency);
            if (entry == null) {
                missingDependencies++;
                continue;
            }
            if (recursive && this.type != ResourceType.SCRIPT) {
                byte[] data = ResourceSystem.extractFile(dependency);
                if (data != null) {
                    Resource resource = new Resource(data);
                    if (resource.method == SerializationType.BINARY) {
                        entry.hasMissingDependencies = resource.registerDependencies(recursive) != 0;
                        entry.canReplaceDecompressed = true;
                        entry.dependencies = resource.dependencies;
                    }
                }
            }
        }
        return missingDependencies;
    }
    
    public void replaceDependency(ResourceReference oldDescriptor, ResourceReference newDescriptor) {
        if (oldDescriptor.equals(newDescriptor)) return;
        int index = this.dependencies.indexOf(oldDescriptor);
        if (index == -1) return;
        
        if (this.type != ResourceType.STATIC_MESH) {
            ResourceType type = oldDescriptor.type;
            boolean isFSB = type.equals(ResourceType.FILENAME);
            byte[] oldDescBuffer, newDescBuffer;

            // Music dependencies are actually the GUID dependencies of a script,
            // so they don't have the same structure for referencing.
            if (type.equals(ResourceType.MUSIC_SETTINGS) || type.equals(ResourceType.FILE_OF_BYTES) || type.equals(ResourceType.SAMPLE) || isFSB) {
                oldDescBuffer = Bytes.createGUID(oldDescriptor.GUID, this.compressionFlags);
                newDescBuffer = Bytes.createGUID(newDescriptor.GUID, this.compressionFlags);
            } else {
                oldDescBuffer = Bytes.getResourceReference(oldDescriptor, this.revision, this.compressionFlags);
                newDescBuffer = Bytes.getResourceReference(newDescriptor, this.revision, this.compressionFlags);
            }


            if (this.type == ResourceType.PLAN) {
                RPlan plan = new RPlan(this);
                MemoryInputStream thingData = new MemoryInputStream(plan.thingData, this.revision);
                Bytes.replace(thingData, oldDescBuffer, newDescBuffer);
                plan.thingData = thingData.data;

                if (isFSB && plan.details != null) {
                    if (oldDescriptor.GUID == plan.details.highlightSound)
                        plan.details.highlightSound = newDescriptor.GUID;
                }

                this.handle.setData(plan.build(this.revision, this.compressionFlags, false));
            }
            Bytes.replace(this.handle, oldDescBuffer, newDescBuffer);
        } else {
            if (this.meshInfo.fallmap.equals(oldDescriptor))
                this.meshInfo.fallmap = newDescriptor;
            if (this.meshInfo.lightmap.equals(oldDescriptor))
                this.meshInfo.lightmap = newDescriptor;
            if (this.meshInfo.risemap.equals(oldDescriptor))
                this.meshInfo.risemap = newDescriptor;
            for (StaticPrimitive primitive : this.meshInfo.primitives)
                if (primitive.gmat.equals(oldDescriptor))
                    primitive.gmat = newDescriptor;
        }
        
        // Remove the dependency from the array if it's effectively null.
        if (newDescriptor == null || newDescriptor.GUID == 0)
            this.dependencies.remove(index);
        else this.dependencies.set(index, newDescriptor);
    }
    
    private int getDependencies() {
        int dependencyTableOffset = this.handle.i32f();
        int originalOffset = this.handle.offset;
        this.handle.offset = dependencyTableOffset;
        
        int size = this.handle.i32f();
        this.dependencies = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            ResourceReference descriptor = new ResourceReference();
            switch (this.handle.i8()) {
                case 1:
                    descriptor.hash = this.handle.sha1();
                    break;
                case 2:
                    descriptor.GUID = this.handle.u32f();
                    break;
            }
            descriptor.type = ResourceType.fromType(this.handle.i32f());
            this.dependencies.add(descriptor);
        }
        
        this.handle.offset = originalOffset;
        
        return dependencyTableOffset;
    }
    
    public static byte[] compressToResource(byte[] data, Revision revision, byte compressionFlags, ResourceType type, ArrayList<ResourceReference> dependencies) {
        Resource resource = new Resource();
        resource.handle = new MemoryInputStream(data, revision);
        resource.compressionFlags = compressionFlags;
        resource.revision = revision;
        resource.type = type;
        if (resource.type == ResourceType.LOCAL_PROFILE)
            resource.method = SerializationType.ENCRYPTED_BINARY;
        else resource.method = SerializationType.BINARY;
        resource.dependencies = dependencies;
        return resource.compressToResource();
    }
    
    public static byte[] compressToResource(MemoryOutputStream data, StaticMeshInfo info) {
        Resource resource = new Resource(data);
        resource.meshInfo = info;
        resource.type = ResourceType.STATIC_MESH;
        return resource.compressToResource();
    }
    
    public static byte[] compressToResource(MemoryOutputStream data, ResourceType type) {
        Resource resource = new Resource(data);
        resource.type = type;
        if (type == ResourceType.LOCAL_PROFILE)
            resource.method = SerializationType.ENCRYPTED_BINARY;
        return resource.compressToResource();
    }
    
    public byte[] compressToResource() {
        MemoryOutputStream output = new MemoryOutputStream(this.dependencies.size() * 0x1c + this.handle.length + 0x50);
        
        if (this.method == SerializationType.TEXT) {
            output.str(this.type.header + this.method.value + '\n');
            output.bytes(this.handle.data);
            output.shrink();
            return output.buffer;
        }
        
        output.str(this.type.header + this.method.value);
        output.i32f(this.revision.head);
        if (this.revision.head >= 0x109) {
            output.i32f(0); // Dummy value for dependency table offset.
            if (this.revision.head >= 0x189) {
                if (this.type == ResourceType.STATIC_MESH)
                    new Serializer(output).struct(this.meshInfo, StaticMeshInfo.class);
                else {
                    if (this.revision.head >= 0x271) {
                        output.i16(this.revision.branchID);
                        output.i16(this.revision.branchRevision);
                    }
                    if (this.revision.head >= 0x297 || (this.revision.head == 0x272 && this.revision.branchID != 0))
                        output.i8(this.compressionFlags);
                    output.bool(this.isCompressed);
                }
            }
            
            byte[] data = this.handle.data;
            if (this.isCompressed) data = Compressor.getCompressedStream(this.handle.data);
            if (this.method == SerializationType.ENCRYPTED_BINARY) {
                int size = data.length;
                if (size % 4 != 0) {
                    int padding = 4 - (size % 4);
                    byte[] paddedData = new byte[padding + size];
                    System.arraycopy(data, 0, paddedData, padding, size);
                    data = paddedData;
                }
                data = TEA.encrypt(data);
                output.i32f(size);
            }
            output.bytes(data);
            
            int dependencyTableOffset = output.offset;
            output.offset = 0x8;
            output.i32f(dependencyTableOffset);
            output.offset = dependencyTableOffset;
            
            output.i32f(this.dependencies.size());
            for (ResourceReference dependency : this.dependencies) {
                if (dependency.GUID != -1) {
                    output.i8((byte) 2);
                    output.u32f(dependency.GUID);
                } else if (dependency.hash != null) {
                    output.i8((byte) 1);
                    output.sha1(dependency.hash);
                }
                output.i32f(dependency.type.value);
            }
        }
        
        output.shrink();
        return output.buffer;
    }
}
